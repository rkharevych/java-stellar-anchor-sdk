package org.stellar.reference.repo

import com.google.gson.reflect.TypeToken
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.time.Instant
import org.stellar.anchor.util.GsonUtils
import org.stellar.reference.data.RateFee
import org.stellar.reference.model.Quote
import org.stellar.reference.util.H2Database

class QuoteRepo(private val database: H2Database) {
  private val gson = GsonUtils.getInstance()

  fun save(quote: Quote) {
    val query =
      "INSERT INTO quote (id, price, total_price, expires_at, created_at, sell_asset, " +
        "sell_amount, sell_delivery_method, buy_asset, buy_amount, buy_delivery_method, country_code, " +
        "client_id, transaction_id, fee) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"

    val connection: Connection = database.getConnection()
    val preparedStatement: PreparedStatement = connection.prepareStatement(query)

    preparedStatement.setObject(1, quote.id)
    preparedStatement.setObject(2, quote.price)
    preparedStatement.setObject(3, quote.totalPrice)
    preparedStatement.setObject(4, quote.expiresAt.toString())
    preparedStatement.setObject(5, quote.createdAt.toString())
    preparedStatement.setObject(6, quote.sellAsset)
    preparedStatement.setObject(7, quote.sellAmount)
    preparedStatement.setObject(8, quote.sellDeliveryMethod)
    preparedStatement.setObject(9, quote.buyAsset)
    preparedStatement.setObject(10, quote.buyAmount)
    preparedStatement.setObject(11, quote.buyDeliveryMethod)
    preparedStatement.setObject(12, quote.countryCode)
    preparedStatement.setObject(13, quote.clientId)
    preparedStatement.setObject(14, quote.transactionId)
    preparedStatement.setObject(15, if (quote.fee == null) null else gson.toJson(quote.fee))

    preparedStatement.executeUpdate()

    preparedStatement.close()
    connection.close()
  }

  fun findById(quoteId: String): Quote? {
    val query = "SELECT * FROM quote WHERE id = ?"

    val connection: Connection = database.getConnection()
    val preparedStatement: PreparedStatement = connection.prepareStatement(query)

    preparedStatement.setObject(1, quoteId)

    val resultSet: ResultSet = preparedStatement.executeQuery()

    var quote: Quote? = null

    if (resultSet.next()) {
      val feeType = object : TypeToken<RateFee>() {}.type
      val id: String = resultSet.getString("id")
      val price: String? = resultSet.getString("price")
      val totalPrice: String? = resultSet.getString("total_price")
      val expiresAt: Instant? = Instant.parse(resultSet.getString("expires_at"))
      val createdAt: Instant = Instant.parse(resultSet.getString("created_at"))
      val sellAsset: String? = resultSet.getString("sell_asset")
      val sellAmount: String? = resultSet.getString("sell_amount")
      val sellDeliveryMethod: String? = resultSet.getString("sell_delivery_method")
      val buyAsset: String? = resultSet.getString("buy_asset")
      val buyAmount: String? = resultSet.getString("buy_amount")
      val buyDeliverMethod: String? = resultSet.getString("buy_delivery_method")
      val countryCode: String? = resultSet.getString("country_code")
      val clientId: String? = resultSet.getString("client_id")
      val transactionId: String? = resultSet.getString("transaction_id")
      val fee: RateFee? = gson.fromJson(resultSet.getString("fee"), feeType)
      quote =
        Quote(
          id,
          price,
          totalPrice,
          expiresAt,
          createdAt,
          sellAsset,
          sellAmount,
          sellDeliveryMethod,
          buyAsset,
          buyAmount,
          buyDeliverMethod,
          countryCode,
          clientId,
          transactionId,
          fee
        )
    }

    resultSet.close()
    preparedStatement.close()
    connection.close()

    return quote
  }
}
