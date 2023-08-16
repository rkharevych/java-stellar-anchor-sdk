package org.stellar.anchor.platform.test

import com.google.gson.reflect.TypeToken
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.fail
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.skyscreamer.jsonassert.JSONAssert
import org.stellar.anchor.api.event.AnchorEvent
import org.stellar.anchor.api.sep.SepTransactionStatus
import org.stellar.anchor.api.sep.sep12.Sep12PutCustomerRequest
import org.stellar.anchor.api.sep.sep31.Sep31PostTransactionRequest
import org.stellar.anchor.apiclient.PlatformApiClient
import org.stellar.anchor.auth.AuthHelper
import org.stellar.anchor.platform.*
import org.stellar.anchor.util.GsonUtils
import org.stellar.anchor.util.Log.info
import org.stellar.anchor.util.MemoHelper
import org.stellar.anchor.util.Sep1Helper
import org.stellar.anchor.util.StringHelper.json
import org.stellar.reference.client.AnchorReferenceServerClient
import org.stellar.walletsdk.ApplicationConfiguration
import org.stellar.walletsdk.StellarConfiguration
import org.stellar.walletsdk.Wallet
import org.stellar.walletsdk.anchor.MemoType
import org.stellar.walletsdk.anchor.TransactionStatus.*
import org.stellar.walletsdk.asset.IssuedAssetId
import org.stellar.walletsdk.asset.StellarAssetId
import org.stellar.walletsdk.horizon.SigningKeyPair
import org.stellar.walletsdk.horizon.sign

class Sep31CustodyActionsEnd2EndTests(
  config: TestConfig,
  val toml: Sep1Helper.TomlContent,
  val jwt: String
) {
  private val gson = GsonUtils.getInstance()
  private val walletSecretKey = System.getenv("WALLET_SECRET_KEY") ?: CLIENT_WALLET_SECRET
  private val keypair = SigningKeyPair.fromSecret(walletSecretKey)
  private val wallet =
    Wallet(
      StellarConfiguration.Testnet,
      ApplicationConfiguration { defaultRequest { url { protocol = URLProtocol.HTTP } } }
    )
  private val client = HttpClient {
    install(HttpTimeout) {
      requestTimeoutMillis = 300000
      connectTimeoutMillis = 300000
      socketTimeoutMillis = 300000
    }
  }
  private val maxTries = 60
  private val anchorReferenceServerClient =
    AnchorReferenceServerClient(Url(config.env["reference.server.url"]!!))
  private val sep12Client = Sep12Client(toml.getString("KYC_SERVER"), jwt)
  private val sep31Client = Sep31Client(toml.getString("DIRECT_PAYMENT_SERVER"), jwt)
  private val sep38Client = Sep38Client(toml.getString("ANCHOR_QUOTE_SERVER"), jwt)
  private val platformApiClient =
    PlatformApiClient(AuthHelper.forNone(), config.env["platform.server.url"]!!)

  private fun compareAndAssertEvents(
    asset: StellarAssetId,
    expectedEvents: List<AnchorEvent>,
    actualEvents: List<AnchorEvent>
  ) {
    expectedEvents.forEachIndexed { index, expectedEvent ->
      actualEvents[index].let { actualEvent ->
        expectedEvent.id = actualEvent.id
        expectedEvent.transaction.id = actualEvent.transaction.id
        expectedEvent.transaction.startedAt = actualEvent.transaction.startedAt
        expectedEvent.transaction.updatedAt = actualEvent.transaction.updatedAt
        expectedEvent.transaction.transferReceivedAt = actualEvent.transaction.transferReceivedAt
        expectedEvent.transaction.completedAt = actualEvent.transaction.completedAt
        expectedEvent.transaction.stellarTransactions = actualEvent.transaction.stellarTransactions
        expectedEvent.transaction.memo = actualEvent.transaction.memo
        expectedEvent.transaction.destinationAccount = actualEvent.transaction.destinationAccount
        expectedEvent.transaction.customers = actualEvent.transaction.customers
        expectedEvent.transaction.quoteId = actualEvent.transaction.quoteId
        actualEvent.transaction.amountIn?.let {
          expectedEvent.transaction.amountIn.amount = actualEvent.transaction.amountIn.amount
          expectedEvent.transaction.amountIn.asset = asset.sep38
          //          expectedEvent.transaction.amountIn.asset =
          // actualEvent.transaction.amountIn.asset
        }
        actualEvent.transaction.amountOut?.let {
          expectedEvent.transaction.amountOut.amount = actualEvent.transaction.amountOut.amount
          expectedEvent.transaction.amountOut.asset = FIAT_USD
          //          expectedEvent.transaction.amountOut.asset =
          // actualEvent.transaction.amountOut.asset
        }
        actualEvent.transaction.amountFee?.let {
          expectedEvent.transaction.amountFee.amount = actualEvent.transaction.amountFee.amount
          expectedEvent.transaction.amountFee.asset = asset.sep38
          //          expectedEvent.transaction.amountFee.asset =
          // actualEvent.transaction.amountFee.asset
        }
        actualEvent.transaction.amountExpected?.let {
          expectedEvent.transaction.amountExpected.amount =
            actualEvent.transaction.amountExpected.amount
          expectedEvent.transaction.amountExpected.asset = asset.sep38
          //          expectedEvent.transaction.amountExpected.asset =
          actualEvent.transaction.amountExpected.asset
        }
      }
    }
    JSONAssert.assertEquals(json(expectedEvents), gson.toJson(actualEvents), true)
  }

  private fun `test typical receive end-to-end flow`(asset: StellarAssetId, amount: String) =
    runBlocking {
      val senderCustomerRequest =
        gson.fromJson(testCustomer1Json, Sep12PutCustomerRequest::class.java)
      val senderCustomer = sep12Client.putCustomer(senderCustomerRequest)

      // Create receiver customer
      val receiverCustomerRequest =
        gson.fromJson(testCustomer2Json, Sep12PutCustomerRequest::class.java)
      val receiverCustomer = sep12Client.putCustomer(receiverCustomerRequest)

      val quote = sep38Client.postQuote(asset.sep38, amount, FIAT_USD)

      // POST Sep31 transaction
      val txnRequest = gson.fromJson(postSep31TxnRequest, Sep31PostTransactionRequest::class.java)
      txnRequest.senderId = senderCustomer!!.id
      txnRequest.receiverId = receiverCustomer!!.id
      txnRequest.quoteId = quote.id
      val postTxResponse = sep31Client.postTransaction(txnRequest)

      anchorReferenceServerClient.processSep31Receive(postTxResponse.id)

      // Get transaction status and make sure it is PENDING_SENDER
      val transaction = platformApiClient.getTransaction(postTxResponse.id)
      assertEquals(SepTransactionStatus.PENDING_SENDER, transaction.status)

      val memoType: MemoType =
        when (postTxResponse.stellarMemoType) {
          MemoHelper.memoTypeAsString(org.stellar.sdk.xdr.MemoType.MEMO_ID) -> {
            MemoType.ID
          }
          MemoHelper.memoTypeAsString(org.stellar.sdk.xdr.MemoType.MEMO_HASH) -> {
            MemoType.HASH
          }
          else -> {
            MemoType.TEXT
          }
        }

      // Submit transfer transaction
      val transfer =
        wallet
          .stellar()
          .transaction(keypair)
          .transfer(postTxResponse.stellarAccountId, asset, amount)
          .addMemo(Pair(memoType, postTxResponse.stellarMemo))
          .build()
      transfer.sign(keypair)
      wallet.stellar().submitTransaction(transfer)

      // Wait for the status to change to COMPLETED
      waitStatus(postTxResponse.id, SepTransactionStatus.COMPLETED)

      // Check the events sent to the reference server are recorded correctly
      val actualEvents = waitForEvents(postTxResponse.id, 3)
      assertNotNull(actualEvents)
      actualEvents?.let { assertEquals(3, it.size) }
      val expectedEvents: List<AnchorEvent> =
        gson.fromJson(expectedReceiveEventJson, object : TypeToken<List<AnchorEvent>>() {}.type)
      compareAndAssertEvents(asset, expectedEvents, actualEvents!!)
    }

  private suspend fun waitForEvents(txnId: String, count: Int): List<AnchorEvent>? {
    var retries = 5
    while (retries > 0) {
      val events = anchorReferenceServerClient.getEvents(txnId)
      if (events.size == count) {
        return events
      }
      delay(1.seconds)
      retries--
    }
    return null
  }

  private suspend fun waitStatus(id: String, expectedStatus: SepTransactionStatus) {
    var status: SepTransactionStatus? = null

    for (i in 0..maxTries) {
      // Get transaction info
      val transaction = platformApiClient.getTransaction(id)

      val current = transaction.status
      info("Expected: $expectedStatus. Current: $current")
      if (status != transaction.status) {
        status = transaction.status
        info("Deposit transaction status changed to $status. Message: ${transaction.message}")
      }

      delay(1.seconds)

      if (transaction.status == expectedStatus) {
        return
      }
    }

    fail("Transaction wasn't $expectedStatus in $maxTries tries, last status: $status")
  }

  fun testAll() {
    info("Running SEP-31 USDC end-to-end tests...")
    `test typical receive end-to-end flow`(USDC, "5")
  }

  companion object {
    private val USDC =
      IssuedAssetId("USDC", "GBBD47IF6LWK7P7MDEVSCWR7DPUWV3NY3DTQEVFL4NAT4AQH3ZLLFLA5")
    private const val FIAT_USD = "iso4217:USD"
  }

  private val expectedReceiveEventJson =
    """
    [
      {
        "type": "TRANSACTION_CREATED",
        "id": "ad47b460-e974-4ab7-b583-230025789743",
        "sep": "31",
        "transaction": {
          "id": "96bafcb5-7e7e-49f6-9ec0-9cad00f2ac83",
          "sep": "31",
          "kind": "receive",
          "status": "pending_sender",
          "amount_expected": {
            "amount": "10",
            "asset": "stellar:USDC:GDQOE23CFSUMSVQK4Y5JHPPYK73VYCNHZHA7ENKCV37P6SUEO6XQBKPP"
          },
          "amount_in": {
            "amount": "10",
            "asset": "stellar:USDC:GDQOE23CFSUMSVQK4Y5JHPPYK73VYCNHZHA7ENKCV37P6SUEO6XQBKPP"
          },
          "amount_out": {
            "amount": "8.5714",
            "asset": "iso4217:USD"
          },
          "amount_fee": {
            "amount": "1.00",
            "asset": "stellar:USDC:GDQOE23CFSUMSVQK4Y5JHPPYK73VYCNHZHA7ENKCV37P6SUEO6XQBKPP"
          },
          "quote_id": "e1d660b6-b215-4afa-a962-6294c4ae97a2",
          "started_at": "2023-08-10T17:57:09.723910Z",
          "updated_at": "2023-08-10T17:57:09.725800Z",
          "memo": "OTZiYWZjYjUtN2U3ZS00OWY2LTllYzAtOWNhZDAwZjI=",
          "customers": {
            "sender": {
              "id": "bfd16df2-b704-4089-a224-68abb23e305a"
            },
            "receiver": {
              "id": "bfd16df2-b704-4089-a224-68abb23e305a"
            }
          },
          "creator": {
            "account": "GDJLBYYKMCXNVVNABOE66NYXQGIA5AC5D223Z2KF6ZEYK4UBCA7FKLTG"
          }
        }
      },
      {
        "type": "TRANSACTION_STATUS_CHANGED",
        "id": "090716f5-3634-453a-9474-b30ca2dd08e8",
        "sep": "31",
        "transaction": {
          "id": "96bafcb5-7e7e-49f6-9ec0-9cad00f2ac83",
          "sep": "31",
          "kind": "receive",
          "status": "pending_receiver",
          "amount_expected": {
            "amount": "10",
            "asset": "stellar:USDC:GDQOE23CFSUMSVQK4Y5JHPPYK73VYCNHZHA7ENKCV37P6SUEO6XQBKPP"
          },
          "amount_in": {
            "amount": "5.0000000",
            "asset": "stellar:USDC:GDQOE23CFSUMSVQK4Y5JHPPYK73VYCNHZHA7ENKCV37P6SUEO6XQBKPP"
          },
          "amount_out": {
            "amount": "8.5714",
            "asset": "iso4217:USD"
          },
          "amount_fee": {
            "amount": "1.00",
            "asset": "stellar:USDC:GDQOE23CFSUMSVQK4Y5JHPPYK73VYCNHZHA7ENKCV37P6SUEO6XQBKPP"
          },
          "quote_id": "e1d660b6-b215-4afa-a962-6294c4ae97a2",
          "started_at": "2023-08-10T17:57:09.723910Z",
          "updated_at": "2023-08-10T17:57:21.872398Z",
          "transfer_received_at": "2023-08-10T17:57:16Z",
          "message": "Received an incoming payment",
          "stellar_transactions": [
            {
              "id": "b5bb4afdb71d1ed11fa164e0abfbd83cd385084cd2e2a041c0834e4717568330",
              "memo": "OTZiYWZjYjUtN2U3ZS00OWY2LTllYzAtOWNhZDAwZjI=",
              "memo_type": "hash",
              "created_at": "2023-08-10T17:57:16Z",
              "envelope": "AAAAAgAAAADSsOMKYK7a1aALie83F4GQDoBdHrW86UX2SYVygRA+VQAAAGQAABUwAAAOVgAAAAEAAAAAAAAAAAAAAABk1SWrAAAAAzk2YmFmY2I1LTdlN2UtNDlmNi05ZWMwLTljYWQwMGYyAAAAAQAAAAAAAAABAAAAAFvGtEMyXcvbioU2IKCSomxahpl7lUyef7ftEPxWcD4bAAAAAVVTREMAAAAA4OJrYiyoyVYK5jqTvfhX91wJp8nB8jVCrv7/SoR3rwAAAAAAAvrwgAAAAAAAAAABgRA+VQAAAEBbLCmsueIix43gR7UUjqtddtQx2OALv10ApoCz09Gc7dSa0tefYChcNvATNgfeev6qMl5Eu9N5Gd1Pcu3BDRsL",
              "payments": [
                {
                  "id": "4051498484903937",
                  "amount": {
                    "amount": "5.0000000",
                    "asset": "USDC:GDQOE23CFSUMSVQK4Y5JHPPYK73VYCNHZHA7ENKCV37P6SUEO6XQBKPP"
                  },
                  "payment_type": "payment",
                  "source_account": "GDJLBYYKMCXNVVNABOE66NYXQGIA5AC5D223Z2KF6ZEYK4UBCA7FKLTG",
                  "destination_account": "GBN4NNCDGJO4XW4KQU3CBIESUJWFVBUZPOKUZHT7W7WRB7CWOA7BXVQF"
                }
              ]
            }
          ],
          "memo": "OTZiYWZjYjUtN2U3ZS00OWY2LTllYzAtOWNhZDAwZjI=",
          "customers": {
            "sender": {
              "id": "bfd16df2-b704-4089-a224-68abb23e305a"
            },
            "receiver": {
              "id": "bfd16df2-b704-4089-a224-68abb23e305a"
            }
          },
          "creator": {
            "account": "GDJLBYYKMCXNVVNABOE66NYXQGIA5AC5D223Z2KF6ZEYK4UBCA7FKLTG"
          }
        }
      },
      {
        "type": "TRANSACTION_STATUS_CHANGED",
        "id": "23e3a9d3-062f-41bc-9eb3-d7a93c5b7579",
        "sep": "31",
        "transaction": {
          "id": "96bafcb5-7e7e-49f6-9ec0-9cad00f2ac83",
          "sep": "31",
          "kind": "receive",
          "status": "completed",
          "amount_expected": {
            "amount": "10",
            "asset": "stellar:USDC:GDQOE23CFSUMSVQK4Y5JHPPYK73VYCNHZHA7ENKCV37P6SUEO6XQBKPP"
          },
          "amount_in": {
            "amount": "5.0000000",
            "asset": "stellar:USDC:GDQOE23CFSUMSVQK4Y5JHPPYK73VYCNHZHA7ENKCV37P6SUEO6XQBKPP"
          },
          "amount_out": {
            "amount": "8.5714",
            "asset": "iso4217:USD"
          },
          "amount_fee": {
            "amount": "1.00",
            "asset": "stellar:USDC:GDQOE23CFSUMSVQK4Y5JHPPYK73VYCNHZHA7ENKCV37P6SUEO6XQBKPP"
          },
          "quote_id": "e1d660b6-b215-4afa-a962-6294c4ae97a2",
          "started_at": "2023-08-10T17:57:09.723910Z",
          "updated_at": "2023-08-10T17:57:26.107961Z",
          "completed_at": "2023-08-10T17:57:26.107956Z",
          "transfer_received_at": "2023-08-10T17:57:16Z",
          "message": "external transfer sent",
          "stellar_transactions": [
            {
              "id": "b5bb4afdb71d1ed11fa164e0abfbd83cd385084cd2e2a041c0834e4717568330",
              "memo": "OTZiYWZjYjUtN2U3ZS00OWY2LTllYzAtOWNhZDAwZjI=",
              "memo_type": "hash",
              "created_at": "2023-08-10T17:57:16Z",
              "envelope": "AAAAAgAAAADSsOMKYK7a1aALie83F4GQDoBdHrW86UX2SYVygRA+VQAAAGQAABUwAAAOVgAAAAEAAAAAAAAAAAAAAABk1SWrAAAAAzk2YmFmY2I1LTdlN2UtNDlmNi05ZWMwLTljYWQwMGYyAAAAAQAAAAAAAAABAAAAAFvGtEMyXcvbioU2IKCSomxahpl7lUyef7ftEPxWcD4bAAAAAVVTREMAAAAA4OJrYiyoyVYK5jqTvfhX91wJp8nB8jVCrv7/SoR3rwAAAAAAAvrwgAAAAAAAAAABgRA+VQAAAEBbLCmsueIix43gR7UUjqtddtQx2OALv10ApoCz09Gc7dSa0tefYChcNvATNgfeev6qMl5Eu9N5Gd1Pcu3BDRsL",
              "payments": [
                {
                  "id": "4051498484903937",
                  "amount": {
                    "amount": "5.0000000",
                    "asset": "USDC:GDQOE23CFSUMSVQK4Y5JHPPYK73VYCNHZHA7ENKCV37P6SUEO6XQBKPP"
                  },
                  "payment_type": "payment",
                  "source_account": "GDJLBYYKMCXNVVNABOE66NYXQGIA5AC5D223Z2KF6ZEYK4UBCA7FKLTG",
                  "destination_account": "GBN4NNCDGJO4XW4KQU3CBIESUJWFVBUZPOKUZHT7W7WRB7CWOA7BXVQF"
                }
              ]
            }
          ],
          "memo": "OTZiYWZjYjUtN2U3ZS00OWY2LTllYzAtOWNhZDAwZjI=",
          "customers": {
            "sender": {
              "id": "bfd16df2-b704-4089-a224-68abb23e305a"
            },
            "receiver": {
              "id": "bfd16df2-b704-4089-a224-68abb23e305a"
            }
          },
          "creator": {
            "account": "GDJLBYYKMCXNVVNABOE66NYXQGIA5AC5D223Z2KF6ZEYK4UBCA7FKLTG"
          }
        }
      }
    ]
  """
      .trimIndent()

  private val postSep31TxnRequest =
    """{
    "amount": "5",
    "asset_code": "USDC",
    "asset_issuer": "GBBD47IF6LWK7P7MDEVSCWR7DPUWV3NY3DTQEVFL4NAT4AQH3ZLLFLA5",
    "receiver_id": "MOCK_RECEIVER_ID",
    "sender_id": "MOCK_SENDER_ID",
    "fields": {
        "transaction": {
            "receiver_routing_number": "r0123",
            "receiver_account_number": "a0456",
            "type": "SWIFT"
        }
    }
}"""
}
