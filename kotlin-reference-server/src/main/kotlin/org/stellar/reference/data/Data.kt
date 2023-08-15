package org.stellar.reference.data

import io.ktor.server.application.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.time.Instant
import java.util.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
  val id: String,
  val status: String,
  val kind: String,
  val message: String? = null,
  @SerialName("amount_in") val amountIn: Amount? = null,
  @SerialName("amount_out") val amountOut: Amount? = null,
  @SerialName("amount_fee") val amountFee: Amount? = null,
  @SerialName("amount_expected") val amountExpected: Amount? = null,
  @SerialName("memo") val memo: String? = null,
  @SerialName("memo_type") val memoType: String? = null,
  @SerialName("source_account") var sourceAccount: String? = null,
  @SerialName("destination_account") var destinationAccount: String? = null,
  @SerialName("stellar_transactions") val stellarTransactions: List<StellarTransaction>? = null
)

@Serializable data class PatchTransactionsRequest(val records: List<PatchTransactionRecord>)

@Serializable data class PatchTransactionRecord(val transaction: PatchTransactionTransaction)

@Serializable
data class PatchTransactionTransaction(
  val id: String,
  val status: String,
  val message: String? = null,
  @SerialName("amount_in") val amountIn: Amount? = null,
  @SerialName("amount_out") val amountOut: Amount? = null,
  @SerialName("amount_fee") val amountFee: Amount? = null,
  @SerialName("stellar_transactions") val stellarTransactions: List<StellarTransaction>? = null,
  val memo: String? = null,
  @SerialName("memo_type") val memoType: String? = null,
)

@Serializable
data class RpcResponse(
  val id: String,
  val jsonrpc: String,
  @Contextual val result: Any?,
  @Contextual val error: Any?
)

@Serializable
data class RpcRequest(
  val id: String,
  val jsonrpc: String,
  val method: String,
  val params: RpcActionParamsRequest
)

@Serializable
sealed class RpcActionParamsRequest {
  @SerialName("transaction_id") abstract val transactionId: String
  abstract val message: String?
}

@Serializable
data class RequestOffchainFundsRequest(
  @SerialName("transaction_id") override val transactionId: String,
  override val message: String,
  @SerialName("amount_in") val amountIn: AmountAssetRequest,
  @SerialName("amount_out") val amountOut: AmountAssetRequest,
  @SerialName("amount_fee") val amountFee: AmountAssetRequest,
  @SerialName("amount_expected") val amountExpected: AmountRequest? = null
) : RpcActionParamsRequest()

@Serializable
data class RequestOnchainFundsRequest(
  @SerialName("transaction_id") override val transactionId: String,
  override val message: String,
  @SerialName("amount_in") val amountIn: AmountAssetRequest,
  @SerialName("amount_out") val amountOut: AmountAssetRequest,
  @SerialName("amount_fee") val amountFee: AmountAssetRequest,
  @SerialName("amount_expected") val amountExpected: AmountRequest? = null
) : RpcActionParamsRequest()

@Serializable
data class NotifyOnchainFundsSentRequest(
  @SerialName("transaction_id") override val transactionId: String,
  override val message: String? = null,
  @SerialName("stellar_transaction_id") val stellarTransactionId: String? = null
) : RpcActionParamsRequest()

@Serializable
data class NotifyOffchainFundsReceivedRequest(
  @SerialName("transaction_id") override val transactionId: String,
  override val message: String,
  @SerialName("funds_received_at") val fundsReceivedAt: String? = null,
  @SerialName("external_transaction_id") val externalTransactionId: String? = null,
  @SerialName("amount_in") val amountIn: AmountAssetRequest? = null,
  @SerialName("amount_out") val amountOut: AmountAssetRequest? = null,
  @SerialName("amount_fee") val amountFee: AmountAssetRequest? = null
) : RpcActionParamsRequest()

@Serializable
data class NotifyOffchainFundsSentRequest(
  @SerialName("transaction_id") override val transactionId: String,
  override val message: String,
  @SerialName("funds_sent_at") val fundsReceivedAt: String? = null,
  @SerialName("external_transaction_id") val externalTransactionId: String? = null
) : RpcActionParamsRequest()

@Serializable
data class DoStellarPaymentRequest(
  @SerialName("transaction_id") override val transactionId: String,
  override val message: String? = null,
) : RpcActionParamsRequest()

@Serializable
data class NotifyTransactionErrorRequest(
  @SerialName("transaction_id") override val transactionId: String,
  override val message: String? = null,
) : RpcActionParamsRequest()

@Serializable data class AmountAssetRequest(val asset: String, val amount: String)

@Serializable data class AmountRequest(val amount: String)

@Serializable data class Amount(val amount: String? = null, val asset: String? = null)

class JwtToken(
  val transactionId: String,
  var expiration: Long, // Expiration Time
  var data: Map<String, String>,
)

@Serializable
data class DepositRequest(
  val amount: String,
  val name: String,
  val surname: String,
  val email: String
)

@Serializable
data class WithdrawalRequest(
  val amount: String,
  val name: String,
  val surname: String,
  val email: String,
  val bank: String,
  val account: String
)

@Serializable
data class StellarTransaction(
  val id: String,
  val memo: String? = null,
  @SerialName("memo_type") val memoType: String? = null,
  val payments: List<StellarPayment>
)

@Serializable data class StellarPayment(val id: String, val amount: Amount)

@Serializable
data class GetFeeRequest(
  @SerialName("send_asset") val sendAsset: String? = null,
  @SerialName("receive_asset") val receiveAsset: String? = null,
  @SerialName("send_amount") val sendAmount: String? = null,
  @SerialName("receive_amount") val receiveAmount: String? = null,
  @SerialName("client_id") val clientId: String? = null,
  @SerialName("sender_id") val senderId: String? = null,
  @SerialName("receiver_id") val receiverId: String? = null
)

@Serializable data class GetFeeResponse(val fee: Amount)

@Serializable
data class GetUniqueAddressResponse(@SerialName("unique_address") val uniqueAddress: UniqueAddress)

@Serializable
data class UniqueAddress(
  @SerialName("stellar_address") var stellarAddress: String,
  var memo: String,
  @SerialName("memo_type") var memoType: String
)

@Serializable
data class GetCustomerRequest(
  val id: String? = null,
  val account: String? = null,
  val memo: String? = null,
  @SerialName("memo_type") val memoType: String? = null,
  val type: String? = null,
  val lang: String? = null
)

@Serializable
data class GetCustomerResponse(
  var id: String?,
  var status: String?,
  var fields: Map<String, CustomerField>?,
  @SerialName("provided_fields") var providedFields: Map<String, ProvidedCustomerField>?,
  var message: String?
)

@Serializable
data class CustomerField(
  val type: String,
  val description: String,
  val choices: List<String>?,
  val optional: Boolean
)

@Serializable
data class ProvidedCustomerField(
  val type: String,
  val description: String,
  val choices: List<String>?,
  val optional: Boolean?,
  val status: String,
  val error: String?
)

@Serializable
data class PutCustomerRequest(
  val id: String? = null,
  val account: String? = null,
  val memo: String? = null,
  @SerialName("memo_type") val memoType: String? = null,
  val type: String? = null,
  @SerialName("first_name") val firstName: String? = null,
  @SerialName("last_name") val lastName: String? = null,
  @SerialName("additional_name") val additionalName: String? = null,
  @SerialName("address_country_code") val addressCountryCode: String? = null,
  @SerialName("state_or_province") val stateOrProvince: String? = null,
  val city: String? = null,
  @SerialName("postal_code") val postalCode: String? = null,
  val address: String? = null,
  @SerialName("mobile_number") val mobileNumber: String? = null,
  @SerialName("email_address") val emailAddress: String? = null,
  @SerialName("birth_date") val birthDate: String? = null,
  @SerialName("birth_place") val birthPlace: String? = null,
  @SerialName("birth_country_code") val birthCountryCode: String? = null,
  @SerialName("bank_account_number") val bankAccountNumber: String? = null,
  @SerialName("bank_account_type") val bankAccountType: String? = null,
  @SerialName("bank_number") val bankNumber: String? = null,
  @SerialName("bank_phone_number") val bankPhoneNumber: String? = null,
  @SerialName("bank_branch_number") val bankBranchNumber: String? = null,
  @SerialName("clabe_number") val clabeNumber: String? = null,
  @SerialName("cbu_number") val cbuNumber: String? = null,
  @SerialName("cbu_alias") val cbuAlias: String? = null,
  @SerialName("tax_id") val taxId: String? = null,
  @SerialName("tax_id_name") val taxIdName: String? = null,
  val occupation: String? = null,
  @SerialName("employer_name") val employerName: String? = null,
  @SerialName("employer_address") val employerAddress: String? = null,
  @SerialName("language_code") val languageCode: String? = null,
  @SerialName("id_type") val idType: String? = null,
  @SerialName("id_country_code") val idCountryCode: String? = null,
  @Contextual @SerialName("id_issue_date") val idIssueDate: Instant? = null,
  @Contextual @SerialName("id_expiration_date") val idExpirationDate: Instant? = null,
  @SerialName("id_number") val idNumber: String? = null,
  @SerialName("ip_address") val ip_address: String? = null,
  val sex: String? = null
)

@Serializable data class PutCustomerResponse(val id: String)

@Serializable
data class GetRateRequest(
  val type: RateType? = null,
  @SerialName("sell_asset") val sellAsset: String? = null,
  @SerialName("sell_amount") val sellAmount: String? = null,
  @SerialName("sell_delivery_method") val sellDeliveryMethod: String? = null,
  @SerialName("buy_asset") val buyAsset: String? = null,
  @SerialName("buy_amount") val buyAmount: String? = null,
  @SerialName("buy_delivery_method") val buyDeliveryMethod: String? = null,
  @SerialName("country_code") val countryCode: String? = null,
  @SerialName("expire_after") val expireAfter: String? = null,
  @SerialName("client_id") val clientId: String? = null,
  val id: String? = null
)

@Serializable
data class GetRateResponse(val rate: Rate) {
  companion object {
    /**
     * Builds the response expected for the INDICATIVE_PRICE type.
     *
     * @param price the price between sell asset and buy asset, without including fees, where
     *   `sell_amount - fee = price * buy_amount` or `sell_amount = price * (buy_amount + fee)` must
     *   be true.
     * @param sellAmount the amount of sell_asset the anchor would expect to receive.
     * @param buyAmount the amount of buy_asset the anchor would trade for the sell_amount of
     *   sell_asset.
     * @param fee an object describing the fee used to calculate the conversion price.
     * @return a GET /rate response with price, total_price, sell_amount, buy_amount and fee.
     */
    fun indicativePrice(
      price: String?,
      sellAmount: String?,
      buyAmount: String?,
      fee: RateFee?
    ): GetRateResponse {
      return GetRateResponse(Rate(null, price, sellAmount, buyAmount, null, fee))
    }
  }
}

@Serializable
data class Rate(
  val id: String?,
  val price: String?,
  @SerialName("sell_amount") val sellAmount: String?,
  @SerialName("buy_amount") val buyAmount: String?,
  @SerialName("expires_at") val expiresAt: String?,
  val fee: RateFee?,
)

@Serializable
data class RateFee(
  var total: String?,
  val asset: String,
  var details: MutableList<RateFeeDetail>?
) {
  suspend fun addFeeDetail(feeDetail: RateFeeDetail?) {
    if (feeDetail?.amount == null) {
      return
    }
    val detailAmount = BigDecimal(feeDetail.amount)
    if (detailAmount.compareTo(BigDecimal.ZERO) == 0) {
      return
    }
    var total = BigDecimal(total)
    total = total.add(detailAmount)
    this.total = formatAmount(total)
    if (details == null) {
      details = mutableListOf()
    }
    details!!.add(feeDetail)
  }

  private fun formatAmount(amount: BigDecimal): String? {
    val decimals = 4
    val newAmount = amount.setScale(decimals, RoundingMode.HALF_DOWN)
    val df = DecimalFormat()
    df.maximumFractionDigits = decimals
    df.minimumFractionDigits = 2
    df.isGroupingUsed = false
    return df.format(newAmount)
  }
}

@Serializable
data class RateFeeDetail(val name: String, val description: String, val amount: String?)

enum class Sep31Type(private val type: String) {
  SEP31_SENDER("sep31-sender"),
  SEP31_RECEIVER("sep31-receiver");

  override fun toString(): String {
    return type
  }
}

enum class RateType(private val type: String) {
  @SerialName("indicative") INDICATIVE("indicative"),
  @SerialName("firm") FIRM("firm");

  override fun toString(): String {
    return type
  }
}

enum class Status(private val status: String) {
  NEEDS_INFO("NEEDS_INFO"),
  ACCEPTED("ACCEPTED"),
  PROCESSING("PROCESSING"),
  ERROR("ERROR");

  override fun toString(): String {
    return status
  }
}
