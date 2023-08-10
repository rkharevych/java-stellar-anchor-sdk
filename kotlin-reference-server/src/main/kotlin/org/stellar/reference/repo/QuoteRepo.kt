package org.stellar.reference.repo

import org.stellar.anchor.util.GsonUtils
import org.stellar.reference.data.Quote

class QuoteRepo() {
  private val data = mutableMapOf<String, Quote>()
  private val gson = GsonUtils.getInstance()

  fun save(quote: Quote) {
    data[quote.id] = gson.fromJson(gson.toJson(quote), Quote::class.java)
  }

  fun findById(id: String): Quote? {
    return if (data[id] != null) {
      gson.fromJson(gson.toJson(data[id]), Quote::class.java)
    } else {
      null
    }
  }
}
