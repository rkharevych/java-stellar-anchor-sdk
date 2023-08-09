package org.stellar.reference.repo

import org.stellar.reference.data.Customer

class CustomerRepo() {
  val data = mutableMapOf<String, Customer>()

  suspend fun save(customer: Customer) {
    data[customer.id!!] = customer
  }

  suspend fun findById(id: String): Customer? {
    return data[id]
  }

  suspend fun findByStellarAccountAndMemoAndMemoType(
    stellarAccount: String,
    memo: String,
    memoType: String
  ): Customer? {
    return data.values
      .stream()
      .filter { it.stellarAccount == stellarAccount && it.memo == memo && it.memoType == memoType }
      .findFirst()
      .orElse(null)
  }

  suspend fun deleteById(id: String) {
    data.remove(id)
  }
}
