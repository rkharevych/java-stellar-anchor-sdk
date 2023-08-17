package org.stellar.anchor.platform.custody

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.stellar.anchor.apiclient.PlatformApiClient
import org.stellar.anchor.platform.config.RpcConfig
import org.stellar.anchor.platform.data.JdbcCustodyTransaction
import org.stellar.anchor.platform.data.JdbcCustodyTransactionRepo
import org.stellar.anchor.util.FileUtil
import org.stellar.anchor.util.GsonUtils

class Sep31CustodyPaymentHandlerTest {

  @MockK(relaxed = true) private lateinit var custodyTransactionRepo: JdbcCustodyTransactionRepo

  @MockK(relaxed = true) private lateinit var platformApiClient: PlatformApiClient

  @MockK(relaxed = true) private lateinit var paymentReceivedCounter: Counter

  @MockK(relaxed = true) private lateinit var rpcConfig: RpcConfig

  private lateinit var sep31CustodyPaymentHandler: Sep31CustodyPaymentHandler

  private val gson = GsonUtils.getInstance()

  @BeforeEach
  fun setup() {
    MockKAnnotations.init(this, relaxUnitFun = true)
    sep31CustodyPaymentHandler =
      Sep31CustodyPaymentHandler(custodyTransactionRepo, platformApiClient, rpcConfig)
  }

  @Test
  fun test_handleEvent_onReceived_payment() {
    val txn =
      gson.fromJson(
        FileUtil.getResourceFileAsString(
          "custody/fireblocks/webhook/handler/custody_transaction_input_sep31_receive_payment.json"
        ),
        JdbcCustodyTransaction::class.java
      )
    val payment =
      gson.fromJson(
        FileUtil.getResourceFileAsString(
          "custody/fireblocks/webhook/handler/custody_payment_with_id.json"
        ),
        CustodyPayment::class.java
      )

    val custodyTxCapture = slot<JdbcCustodyTransaction>()
    mockkStatic(Metrics::class)

    every { rpcConfig.customMessages.incomingPaymentReceived } returns "payment received"
    every { custodyTransactionRepo.save(capture(custodyTxCapture)) } returns txn
    every { Metrics.counter("payment.received", "asset", "testAmountInAsset") } returns
      paymentReceivedCounter

    sep31CustodyPaymentHandler.onReceived(txn, payment)

    verify(exactly = 1) { paymentReceivedCounter.increment(1.0000000) }
    verify(exactly = 1) {
      platformApiClient.notifyOnchainFundsReceived(
        txn.id,
        payment.transactionHash,
        payment.amount,
        "payment received"
      )
    }

    JSONAssert.assertEquals(
      FileUtil.getResourceFileAsString(
        "custody/fireblocks/webhook/handler/custody_transaction_db_sep31_receive_payment.json"
      ),
      gson.toJson(custodyTxCapture.captured),
      JSONCompareMode.STRICT
    )
  }

  @Test
  fun test_handleEvent_onReceived_refund() {
    val txn =
      gson.fromJson(
        FileUtil.getResourceFileAsString(
          "custody/fireblocks/webhook/handler/custody_transaction_input_sep31_receive_refund.json"
        ),
        JdbcCustodyTransaction::class.java
      )
    val payment =
      gson.fromJson(
        FileUtil.getResourceFileAsString(
          "custody/fireblocks/webhook/handler/custody_payment_with_id.json"
        ),
        CustodyPayment::class.java
      )

    val custodyTxCapture = slot<JdbcCustodyTransaction>()
    mockkStatic(Metrics::class)

    every { rpcConfig.customMessages.incomingPaymentReceived } returns "payment received"
    every { custodyTransactionRepo.save(capture(custodyTxCapture)) } returns txn

    sep31CustodyPaymentHandler.onReceived(txn, payment)

    verify(exactly = 0) { Metrics.counter("payment.sent", any(), any()) }
    verify(exactly = 1) {
      platformApiClient.notifyRefundSent(
        txn.id,
        payment.transactionHash,
        payment.amount,
        txn.amountFee,
        txn.asset
      )
    }

    JSONAssert.assertEquals(
      FileUtil.getResourceFileAsString(
        "custody/fireblocks/webhook/handler/custody_transaction_db_sep31_receive_refund.json"
      ),
      gson.toJson(custodyTxCapture.captured),
      JSONCompareMode.STRICT
    )
  }

  @Test
  fun test_handleEvent_onReceived_payment_error() {
    val txn =
      gson.fromJson(
        FileUtil.getResourceFileAsString(
          "custody/fireblocks/webhook/handler/custody_transaction_input_sep31_receive_payment.json"
        ),
        JdbcCustodyTransaction::class.java
      )
    val payment =
      gson.fromJson(
        FileUtil.getResourceFileAsString(
          "custody/fireblocks/webhook/handler/custody_payment_with_id_error.json"
        ),
        CustodyPayment::class.java
      )

    val custodyTxCapture = slot<JdbcCustodyTransaction>()
    mockkStatic(Metrics::class)

    every { rpcConfig.customMessages.custodyTransactionFailed } returns "payment failed"
    every { custodyTransactionRepo.save(capture(custodyTxCapture)) } returns txn

    sep31CustodyPaymentHandler.onReceived(txn, payment)

    verify(exactly = 0) { Metrics.counter("payment.received", any(), any()) }
    verify(exactly = 1) { platformApiClient.notifyTransactionError(txn.id, "payment failed") }

    JSONAssert.assertEquals(
      FileUtil.getResourceFileAsString(
        "custody/fireblocks/webhook/handler/custody_transaction_db_sep31_receive_payment_error.json"
      ),
      gson.toJson(custodyTxCapture.captured),
      JSONCompareMode.STRICT
    )
  }

  @Test
  fun test_handleEvent_onSent_payment() {
    val txn =
      gson.fromJson(
        FileUtil.getResourceFileAsString(
          "custody/fireblocks/webhook/handler/custody_transaction_input_sep31_receive_payment.json"
        ),
        JdbcCustodyTransaction::class.java
      )
    val payment =
      gson.fromJson(
        FileUtil.getResourceFileAsString(
          "custody/fireblocks/webhook/handler/custody_payment_with_id.json"
        ),
        CustodyPayment::class.java
      )

    mockkStatic(Metrics::class)

    sep31CustodyPaymentHandler.onSent(txn, payment)

    verify(exactly = 0) { Metrics.counter("payment.sent", any(), any()) }
    verify(exactly = 0) { custodyTransactionRepo.save(any()) }
  }
}
