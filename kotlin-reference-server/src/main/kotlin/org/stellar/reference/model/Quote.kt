package org.stellar.reference.model

import java.time.Instant
import java.util.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.stellar.reference.data.GetRateRequest
import org.stellar.reference.data.GetRateResponse
import org.stellar.reference.data.Rate
import org.stellar.reference.data.RateFee

@Serializable
data class Quote(
  val id: String,
  var price: String?,
  var totalPrice: String?,
  @Contextual var expiresAt: Instant?,
  @Contextual val createdAt: Instant,
  val sellAsset: String?,
  var sellAmount: String?,
  val sellDeliveryMethod: String?,
  val buyAsset: String?,
  var buyAmount: String?,
  val buyDeliveryMethod: String?,
  val countryCode: String?,

  // used to store the stellar account
  val clientId: String?,
  val transactionId: String?,
  var fee: RateFee?
) {

  companion object {
    fun of(request: GetRateRequest): Quote {
      return Quote(
        UUID.randomUUID().toString(),
        null,
        null,
        null,
        Instant.now(),
        request.sellAsset,
        request.sellAmount,
        request.sellDeliveryMethod,
        request.buyAsset,
        request.buyAmount,
        request.buyDeliveryMethod,
        request.countryCode,
        request.clientId,
        null,
        null
      )
    }
  }

  fun toGetRateResponse(): GetRateResponse {
    return GetRateResponse(Rate(id, price, sellAmount, buyAmount, expiresAt?.toString(), fee))
  }
}
