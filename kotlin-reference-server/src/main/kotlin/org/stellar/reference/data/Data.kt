package org.stellar.reference.data

import io.ktor.server.application.*
import java.time.Instant
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
data class Customer(
  var id: String?,
  @SerialName("stellar_account") var stellarAccount: String?,
  var memo: String?,
  @SerialName("memo_type") var memoType: String?,
  @SerialName("first_name") var firstName: String?,
  @SerialName("last_name") var lastName: String?,
  var email: String?,
  @SerialName("bank_account_number") var bankAccountNumber: String?,
  @SerialName("bank_account_type") var bankAccountType: String?,
  @SerialName("bank_routing_number") var bankRoutingNumber: String?,
  @SerialName("clabe_number") var clabeNumber: String?
)

@Serializable
data class GetFeeRequest(
  @SerialName("send_asset") val sendAsset: String?,
  @SerialName("receive_asset") val receiveAsset: String?,
  @SerialName("send_amount") val sendAmount: String?,
  @SerialName("receive_amount") val receiveAmount: String?,
  @SerialName("client_id") val clientId: String?,
  @SerialName("sender_id") val senderId: String?,
  @SerialName("receiver_id") val receiverId: String?,
)

@Serializable data class GetFeeResponse(val amount: Amount)

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
  val id: String?,
  val account: String,
  val memo: String,
  @SerialName("memo_type") val memoType: String,
  val type: String,
  val lang: String,
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
  val id: String?,
  val account: String,
  val memo: String,
  @SerialName("memo_type") val memoType: String,
  val type: String,
  @SerialName("first_name") val firstName: String?,
  @SerialName("last_name") val lastName: String?,
  @SerialName("additional_name") val additionalName: String,
  @SerialName("address_country_code") val addressCountryCode: String,
  @SerialName("state_or_province") val stateOrProvince: String,
  val city: String,
  @SerialName("postal_code") val postalCode: String,
  val address: String,
  @SerialName("mobile_number") val mobileNumber: String,
  @SerialName("email_address") val emailAddress: String?,
  @SerialName("birth_date") val birthDate: String,
  @SerialName("birth_place") val birthPlace: String,
  @SerialName("birth_country_code") val birthCountryCode: String,
  @SerialName("bank_account_number") val bankAccountNumber: String?,
  @SerialName("bank_account_type") val bankAccountType: String?,
  @SerialName("bank_number") val bankNumber: String?,
  @SerialName("bank_phone_number") val bankPhoneNumber: String,
  @SerialName("bank_branch_number") val bankBranchNumber: String,
  @SerialName("clabe_number") val clabeNumber: String?,
  @SerialName("cbu_number") val cbuNumber: String,
  @SerialName("cbu_alias") val cbuAlias: String,
  @SerialName("tax_id") val taxId: String,
  @SerialName("tax_id_name") val taxIdName: String,
  val occupation: String,
  @SerialName("employer_name") val employerName: String,
  @SerialName("employer_address") val employerAddress: String,
  @SerialName("language_code") val languageCode: String,
  @SerialName("id_type") val idType: String,
  @SerialName("id_country_code") val idCountryCode: String,
  @Contextual @SerialName("id_issue_date") val idIssueDate: Instant,
  @Contextual @SerialName("id_expiration_date") val idExpirationDate: Instant,
  @SerialName("id_number") val idNumber: String,
  @SerialName("ip_address") val ip_address: String,
  val sex: String
)

@Serializable data class PutCustomerResponse(val id: String)

enum class Type(private val type: String) {
  SEP31_SENDER("sep31-sender"),
  SEP31_RECEIVER("sep31-receiver");

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
