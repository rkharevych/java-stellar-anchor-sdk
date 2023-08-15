package org.stellar.reference.data

import org.stellar.anchor.util.StringHelper.isNotEmpty
import org.stellar.sdk.KeyPair

data class LocationConfig(val ktReferenceServerConfig: String)

data class Config(
  val port: Int,
  val enableTest: Boolean,
  val anchorPlatformUrl: String,
  val custodyEnabled: Boolean,
  val rpcActionsEnabled: Boolean,
  val sep24: Sep24,
  val sep31: Sep31
)

data class Sep24(val horizonUrl: String, val secret: String, val interactiveJwtKey: String) {
  val keyPair: KeyPair? = if (isNotEmpty(secret)) KeyPair.fromSecretSeed(secret) else null
}

data class Sep31(
  val distributionWallet: String,
  val distributionWalletMemo: String,
  val distributionWalletMemoType: String
)
