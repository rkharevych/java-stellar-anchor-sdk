package org.stellar.reference.repo

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import org.stellar.reference.model.Customer
import org.stellar.reference.util.H2Database

class CustomerRepo(private val database: H2Database) {

  fun save(customer: Customer) {
    customer.id?.let { deleteById(it) }

    val query =
      "INSERT INTO customer (id, stellar_account, memo, memo_type, first_name, last_name, email, " +
        "bank_account_number, bank_account_type, bank_routing_number, clabe_number) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"

    val connection: Connection = database.getConnection()
    val preparedStatement: PreparedStatement = connection.prepareStatement(query)

    preparedStatement.setObject(1, customer.id)
    preparedStatement.setObject(2, customer.stellarAccount)
    preparedStatement.setObject(3, customer.memo)
    preparedStatement.setObject(4, customer.memoType)
    preparedStatement.setObject(5, customer.firstName)
    preparedStatement.setObject(6, customer.lastName)
    preparedStatement.setObject(7, customer.email)
    preparedStatement.setObject(8, customer.bankAccountNumber)
    preparedStatement.setObject(9, customer.bankAccountType)
    preparedStatement.setObject(10, customer.bankRoutingNumber)
    preparedStatement.setObject(11, customer.clabeNumber)

    preparedStatement.executeUpdate()

    preparedStatement.close()
    connection.close()
  }

  fun findById(customerId: String): Customer? {
    val query = "SELECT * FROM customer WHERE id = ?"

    val connection: Connection = database.getConnection()
    val preparedStatement: PreparedStatement = connection.prepareStatement(query)

    preparedStatement.setObject(1, customerId)

    val resultSet: ResultSet = preparedStatement.executeQuery()

    var customer: Customer? = null

    if (resultSet.next()) {
      val id: String = resultSet.getString("id")
      val stellarAccount: String? = resultSet.getString("stellar_account")
      val memo: String? = resultSet.getString("memo")
      val memoType: String? = resultSet.getString("memo_type")
      val firstName: String = resultSet.getString("first_name")
      val lastName: String? = resultSet.getString("last_name")
      val email: String? = resultSet.getString("email")
      val bankAccountNumber: String? = resultSet.getString("bank_account_number")
      val bankAccountType: String? = resultSet.getString("bank_account_type")
      val bankRoutingNumber: String? = resultSet.getString("bank_routing_number")
      val clabeNumber: String? = resultSet.getString("clabe_number")
      customer =
        Customer(
          id,
          stellarAccount,
          memo,
          memoType,
          firstName,
          lastName,
          email,
          bankAccountNumber,
          bankAccountType,
          bankRoutingNumber,
          clabeNumber
        )
    }

    resultSet.close()
    preparedStatement.close()
    connection.close()

    return customer
  }

  fun findByStellarAccountAndMemoAndMemoType(
    customerStellarAccount: String?,
    customerMemo: String?,
    customerMemoType: String?
  ): Customer? {
    val query =
      "SELECT * FROM customer WHERE (stellar_account = ? OR (stellar_account IS NULL AND ? IS NULL)) " +
        "AND (memo = ? OR (memo IS NULL AND ? IS NULL))" +
        "AND (memo_type = ? OR (memo_type IS NULL AND ? IS NULL))"

    val connection: Connection = database.getConnection()
    val preparedStatement: PreparedStatement = connection.prepareStatement(query)

    preparedStatement.setObject(1, customerStellarAccount)
    preparedStatement.setObject(2, customerStellarAccount)
    preparedStatement.setObject(3, customerMemo)
    preparedStatement.setObject(4, customerMemo)
    preparedStatement.setObject(5, customerMemoType)
    preparedStatement.setObject(6, customerMemoType)

    val resultSet: ResultSet = preparedStatement.executeQuery()

    var customer: Customer? = null

    if (resultSet.next()) {
      val id: String = resultSet.getString("id")
      val stellarAccount: String? = resultSet.getString("stellar_account")
      val memo: String? = resultSet.getString("memo")
      val memoType: String? = resultSet.getString("memo_type")
      val firstName: String = resultSet.getString("first_name")
      val lastName: String? = resultSet.getString("last_name")
      val email: String? = resultSet.getString("email")
      val bankAccountNumber: String? = resultSet.getString("bank_account_number")
      val bankAccountType: String? = resultSet.getString("bank_account_type")
      val bankRoutingNumber: String? = resultSet.getString("bank_routing_number")
      val clabeNumber: String? = resultSet.getString("clabe_number")
      customer =
        Customer(
          id,
          stellarAccount,
          memo,
          memoType,
          firstName,
          lastName,
          email,
          bankAccountNumber,
          bankAccountType,
          bankRoutingNumber,
          clabeNumber
        )
    }

    resultSet.close()
    preparedStatement.close()
    connection.close()

    return customer
  }

  fun deleteById(customerId: String) {
    val query = "DELETE FROM customer WHERE id = ?"

    val connection: Connection = database.getConnection()
    val preparedStatement: PreparedStatement = connection.prepareStatement(query)

    preparedStatement.setObject(1, customerId)

    preparedStatement.executeUpdate()

    preparedStatement.close()
    connection.close()
  }
}
