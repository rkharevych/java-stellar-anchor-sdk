package org.stellar.reference.service.sep31

import java.math.BigDecimal
import java.math.RoundingMode.HALF_DOWN
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import org.stellar.anchor.util.DateUtil
import org.stellar.anchor.util.MathHelper.decimal
import org.stellar.anchor.util.MathHelper.formatAmount
import org.stellar.anchor.util.SepHelper.validateAmount
import org.stellar.reference.ClientException
import org.stellar.reference.NotFoundException
import org.stellar.reference.data.*
import org.stellar.reference.data.RateType.FIRM
import org.stellar.reference.data.RateType.INDICATIVE
import org.stellar.reference.model.Quote
import org.stellar.reference.repo.QuoteRepo

class RateService(private val quoteRepo: QuoteRepo) {
  private val scale = 4

  suspend fun getRate(request: GetRateRequest): GetRateResponse {
    if (request.id != null) {
      val quote = quoteRepo.findById(request.id) ?: throw NotFoundException("Quote not found.")
      return quote.toGetRateResponse()
    }

    if (request.type == null) {
      throw ClientException("type cannot be empty")
    }

    if (!listOf(INDICATIVE, FIRM).contains(request.type)) {
      throw ClientException("the provided type is not supported")
    }

    if (request.sellAsset == null) {
      throw ClientException("sell_asset cannot be empty")
    }

    if (request.buyAsset == null) {
      throw ClientException("buy_asset cannot be empty")
    }

    var sellAmount = request.sellAmount
    var buyAmount = request.buyAmount
    if ((sellAmount == null && buyAmount == null) || (sellAmount != null && buyAmount != null)) {
      throw ClientException("Please provide either sell_amount or buy_amount")
    } else if (sellAmount != null) {
      validateAmount("sell_", sellAmount)
    } else {
      validateAmount("buy_", buyAmount)
    }

    // Calculate everything
    var price =
      ConversionPrice.getPrice(request.sellAsset, request.buyAsset)
        ?: throw ClientException("the price for the given pair could not be found")

    var bPrice = decimal(price, scale)
    var bSellAmount: BigDecimal? = null
    var bBuyAmount: BigDecimal? = null
    if (sellAmount != null) {
      bSellAmount = decimal(sellAmount, scale)
    } else {
      bBuyAmount = decimal(buyAmount, scale)
    }

    val fee = ConversionPrice.getFee(request.sellAsset, request.buyAsset)
    val bFee = decimal(fee.total)

    // sell_amount - fee = price * buy_amount     // when `fee` is in `sell_asset`
    if (bSellAmount != null) {
      // buy_amount = (sell_amount - fee) / price
      bBuyAmount = (bSellAmount.subtract(bFee)).divide(bPrice, HALF_DOWN)
      if (bBuyAmount < BigDecimal.ZERO) {
        throw ClientException("sell amount must be greater than " + fee.total)
      }
      buyAmount = formatAmount(bBuyAmount, scale)
    } else {
      // sell_amount = (buy_amount * price) + fee
      bSellAmount = (bBuyAmount?.setScale(10, HALF_DOWN)?.multiply(bPrice))?.add(bFee)
      sellAmount = formatAmount(bSellAmount, scale)
    }
    // recalibrate price to guarantee the formula is true up to the required decimals
    bPrice = (bSellAmount?.setScale(10, HALF_DOWN)?.subtract(bFee))?.divide(bBuyAmount, HALF_DOWN)
    price = formatAmount(bPrice, 10)

    // total_price = sell_amount / buy_amount
    val bTotalPrice = bSellAmount?.divide(bBuyAmount, 10, HALF_DOWN)
    val totalPrice = formatAmount(bTotalPrice, 10)

    if (request.type == INDICATIVE) {
      return GetRateResponse.indicativePrice(price, sellAmount, buyAmount, fee)
    }

    val quote = createQuote(request, price, totalPrice, sellAmount, buyAmount, fee)
    return quote.toGetRateResponse()
  }

  private suspend fun createQuote(
    request: GetRateRequest,
    price: String,
    totalPrice: String,
    sellAmount: String?,
    buyAmount: String?,
    fee: RateFee
  ): Quote {
    val quote: Quote = Quote.of(request)
    quote.price = price
    quote.totalPrice = totalPrice
    quote.sellAmount = sellAmount
    quote.buyAmount = buyAmount
    quote.fee = fee

    // "calculate" expiresAt
    val strExpiresAfter = request.expireAfter
    val expiresAfter: Instant =
      if (strExpiresAfter == null) {
        Instant.now()
      } else {
        DateUtil.fromISO8601UTC(strExpiresAfter)
      }
    val expiresAt =
      ZonedDateTime.ofInstant(expiresAfter, ZoneId.of("UTC"))
        .plusDays(1)
        .withHour(12)
        .withMinute(0)
        .withSecond(0)
        .withNano(0)
    quote.expiresAt = expiresAt.toInstant()
    quoteRepo.save(quote)
    return quote
  }

  class ConversionPrice {
    companion object {
      private const val fiatUSD = "iso4217:USD"
      private const val stellarCircleUSDCtest =
        "stellar:USDC:GBBD47IF6LWK7P7MDEVSCWR7DPUWV3NY3DTQEVFL4NAT4AQH3ZLLFLA5"
      private const val stellarUSDCtest =
        "stellar:USDC:GDQOE23CFSUMSVQK4Y5JHPPYK73VYCNHZHA7ENKCV37P6SUEO6XQBKPP"
      private const val stellarUSDCprod =
        "stellar:USDC:GA5ZSEJYB37JRC5AVCIA5MOP4RHTM335X2KGX3IHOJAPP5RE34K4KZVN"
      private const val stellarJPYC =
        "stellar:JPYC:GDQOE23CFSUMSVQK4Y5JHPPYK73VYCNHZHA7ENKCV37P6SUEO6XQBKPP"
      private const val stellarNative = "stellar:native"

      private val hardcodedPrices =
        mapOf(
          Pair(fiatUSD, stellarUSDCtest) to "1.02",
          Pair(stellarUSDCtest, fiatUSD) to "1.05",
          Pair(fiatUSD, stellarCircleUSDCtest) to "1.02",
          Pair(stellarCircleUSDCtest, fiatUSD) to "1.05",
          Pair(fiatUSD, stellarUSDCprod) to "1.02",
          Pair(stellarUSDCprod, fiatUSD) to "1.05",
          Pair(fiatUSD, stellarJPYC) to "0.0083333",
          Pair(stellarJPYC, fiatUSD) to "122",
          Pair(stellarUSDCtest, stellarJPYC) to "0.0084",
          Pair(stellarJPYC, stellarUSDCtest) to "120",
          Pair(stellarCircleUSDCtest, stellarJPYC) to "0.0084",
          Pair(stellarJPYC, stellarCircleUSDCtest) to "120",
          Pair(stellarUSDCprod, stellarJPYC) to "0.0084",
          Pair(stellarJPYC, stellarUSDCprod) to "120",
          Pair(fiatUSD, stellarNative) to "1.02",
          Pair(stellarNative, fiatUSD) to "1.05",
        )

      /*
       * getPrice returns the price without fees
       */
      fun getPrice(sellAsset: String, buyAsset: String): String? {
        return hardcodedPrices[Pair(sellAsset, buyAsset)]
      }

      suspend fun getFee(sellAsset: String, buyAsset: String): RateFee {
        val rateFee = RateFee("0", sellAsset, null)
        if (getPrice(sellAsset, buyAsset) == null) {
          return rateFee
        }
        val sellAssetFeeDetail =
          RateFeeDetail("Sell fee", "Fee related to selling the asset.", "1.00")
        rateFee.addFeeDetail(sellAssetFeeDetail)
        return rateFee
      }
    }
  }
}
