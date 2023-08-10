package org.stellar.reference.repo

import org.stellar.anchor.util.GsonUtils
import org.stellar.reference.data.Customer

class CustomerRepo {
  private val data = mutableMapOf<String, Customer>()
  private val gson = GsonUtils.getInstance()

  fun save(customer: Customer) {
    data[customer.id!!] = gson.fromJson(gson.toJson(customer), Customer::class.java)
  }

  fun findById(id: String): Customer? {
    return if (data[id] != null) {
      gson.fromJson(gson.toJson(data[id]), Customer::class.java)
    } else {
      null
    }
  }

  fun findByStellarAccountAndMemoAndMemoType(
    stellarAccount: String?,
    memo: String?,
    memoType: String?
  ): Customer? {
    return data.values
      .stream()
      .filter { it.stellarAccount == stellarAccount && it.memo == memo && it.memoType == memoType }
      .findFirst()
      .map { gson.fromJson(gson.toJson(it), Customer::class.java) }
      .orElse(null)
  }

  fun deleteById(id: String) {
    data.remove(id)
  }
}
