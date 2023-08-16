package org.stellar.reference.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
