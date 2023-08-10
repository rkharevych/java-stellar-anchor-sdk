package org.stellar.reference.plugins

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.logging.*
import mu.KotlinLogging
import org.stellar.anchor.util.GsonUtils
import org.stellar.reference.ClientException
import org.stellar.reference.data.ErrorResponse
import org.stellar.reference.data.GetFeeRequest
import org.stellar.reference.data.GetRateRequest
import org.stellar.reference.data.PutCustomerRequest
import org.stellar.reference.sep31.CustomerService
import org.stellar.reference.sep31.FeeService
import org.stellar.reference.sep31.RateService
import org.stellar.reference.sep31.UniqueAddressService

private val log = KotlinLogging.logger {}

fun Route.sep31(
  feeService: FeeService,
  uniqueAddressService: UniqueAddressService,
  customerService: CustomerService,
  rateService: RateService
) {
  val gson = GsonUtils.getInstance()

  route("/fee") {
    get {
      try {
        call.respond(
          feeService.getFee(gson.fromJson(call.receive<String>(), GetFeeRequest::class.java))
        )
      } catch (e: ClientException) {
        log.error(e)
        call.respond(ErrorResponse(e.message!!))
      } catch (e: Exception) {
        log.error(e)
        call.respond(
          ErrorResponse("Error occurred: ${e.message}"),
        )
      }
    }
  }

  route("/unique_address") {
    get {
      try {
        call.respond(
          uniqueAddressService.getUniqueAddress(call.request.queryParameters["transaction_id"]!!)
        )
      } catch (e: ClientException) {
        log.error(e)
        call.respond(ErrorResponse(e.message!!))
      } catch (e: Exception) {
        log.error(e)
        call.respond(
          ErrorResponse("Error occurred: ${e.message}"),
        )
      }
    }
  }

  route("/customer") {
    get {
      try {
        call.respond(
          customerService.upsertCustomer(
            gson.fromJson(call.receive<String>(), PutCustomerRequest::class.java)
          )
        )
      } catch (e: ClientException) {
        log.error(e)
        call.respond(ErrorResponse(e.message!!))
      } catch (e: Exception) {
        log.error(e)
        call.respond(
          ErrorResponse("Error occurred: ${e.message}"),
        )
      }
    }
  }

  route("/customer") {
    put {
      try {
        call.respond(
          customerService.getCustomer(
            gson.fromJson(call.receive<String>(), PutCustomerRequest::class.java)
          )
        )
      } catch (e: ClientException) {
        log.error(e)
        call.respond(ErrorResponse(e.message!!))
      } catch (e: Exception) {
        log.error(e)
        call.respond(
          ErrorResponse("Error occurred: ${e.message}"),
        )
      }
    }
  }

  route("/customer/{id}") {
    delete {
      try {
        customerService.delete(call.parameters["id"]!!)
      } catch (e: ClientException) {
        log.error(e)
        call.respond(ErrorResponse(e.message!!))
      } catch (e: Exception) {
        log.error(e)
        call.respond(
          ErrorResponse("Error occurred: ${e.message}"),
        )
      }
    }
  }

  route("/rate") {
    get {
      try {
        call.respond(
          rateService.getRate(gson.fromJson(call.receive<String>(), GetRateRequest::class.java))
        )
      } catch (e: ClientException) {
        log.error(e)
        call.respond(ErrorResponse(e.message!!))
      } catch (e: Exception) {
        log.error(e)
        call.respond(
          ErrorResponse("Error occurred: ${e.message}"),
        )
      }
    }
  }
}
