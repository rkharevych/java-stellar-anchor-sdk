package org.stellar.reference.sep31

import java.util.*
import mu.KotlinLogging
import org.apache.commons.lang3.StringUtils
import org.stellar.anchor.util.MemoHelper
import org.stellar.anchor.util.MemoHelper.memoTypeAsString
import org.stellar.anchor.util.StringHelper.isEmpty
import org.stellar.anchor.util.StringHelper.isNotEmpty
import org.stellar.reference.ClientException
import org.stellar.reference.data.*
import org.stellar.sdk.KeyPair
import org.stellar.sdk.xdr.MemoType

private val log = KotlinLogging.logger {}

class UniqueAddressService(private val cfg: Config) {

  suspend fun getUniqueAddress(transactionId: String): GetUniqueAddressResponse {
    // transactionId may be used to query the transaction information if the anchor would like to
    // return a transaction-dependent unique address.

    log.debug { "Getting a unique address for transaction[id=$transactionId]" }

    validateAddressAndMemo()

    val memo: String
    val memoType: String

    if (
      isEmpty(cfg.sep31.distributionWalletMemo) || isEmpty(cfg.sep31.distributionWalletMemoType)
    ) {
      var generatedMemo = StringUtils.abbreviate(transactionId, 32)
      generatedMemo = StringUtils.leftPad(generatedMemo, 32, '0')
      memo = String(Base64.getEncoder().encode(generatedMemo.toByteArray()))
      memoType = memoTypeAsString(MemoType.MEMO_HASH)
    } else {
      memo = cfg.sep31.distributionWalletMemo
      memoType = cfg.sep31.distributionWalletMemoType
    }

    val uniqueAddress = UniqueAddress(cfg.sep31.distributionWallet, memo, memoType)
    log.info {
      "Got the unique address for transaction[id=$transactionId]. memo=${uniqueAddress.memo}, " +
        "memoType=${uniqueAddress.memoType}"
    }

    return GetUniqueAddressResponse(uniqueAddress)
  }

  private suspend fun validateAddressAndMemo() {
    if (Objects.toString(cfg.sep31.distributionWallet, "").isEmpty()) {
      throw ClientException("distributionWallet is empty")
    }

    try {
      KeyPair.fromAccountId(cfg.sep31.distributionWallet)
    } catch (ex: Exception) {
      throw ClientException(
        String.format("Invalid distributionWallet: [%s]", cfg.sep31.distributionWallet)
      )
    }

    if (
      isNotEmpty(cfg.sep31.distributionWalletMemo) &&
        isNotEmpty(cfg.sep31.distributionWalletMemoType)
    ) {
      // check if memo and memoType are valid
      MemoHelper.makeMemo(cfg.sep31.distributionWalletMemo, cfg.sep31.distributionWalletMemoType)
    }
  }
}
