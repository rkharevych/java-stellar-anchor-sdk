package org.stellar.reference.sep31

import org.stellar.anchor.util.MathHelper.decimal
import org.stellar.reference.ClientException
import org.stellar.reference.data.Amount
import org.stellar.reference.data.GetFeeRequest
import org.stellar.reference.data.GetFeeResponse
import org.stellar.reference.repo.CustomerRepo

class FeeService(private val customerRepo: CustomerRepo) {
  private val feePercent = decimal("0.02") // fixed 2% fee.
  private val feeFixed = decimal("0.1")

  suspend fun getFee(request: GetFeeRequest): GetFeeResponse {
    if (request.sendAsset == null) {
      throw ClientException("send_asset cannot be empty.")
    }

    if (request.receiveAsset == null) {
      throw ClientException("receive_asset cannot be empty.")
    }

    if (request.clientId == null) {
      throw ClientException("client_id cannot be empty.")
    }

    if (request.sendAmount == null && request.receiveAmount == null) {
      throw ClientException("sender_amount or receiver_amount must be present.")
    }

    if (request.senderId == null) {
      throw ClientException("sender_id cannot be empty.")
    }
    customerRepo.findById(request.senderId) ?: throw ClientException("sender_id was not found.")

    if (request.receiverId == null) {
      throw ClientException("receiver_id cannot be empty.")
    }
    customerRepo.findById(request.receiverId) ?: throw ClientException("receiver_id was not found.")

    val amount = decimal(request.sendAmount)
    // fee = feeFixed + feePercent * sendAmount
    val fee = amount.multiply(feePercent).add(feeFixed)
    return GetFeeResponse(Amount(fee.toString(), request.sendAsset))
  }
}
