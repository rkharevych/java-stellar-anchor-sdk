package org.stellar.reference.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.logging.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.stellar.reference.ClientException
import org.stellar.reference.NotFoundException
import org.stellar.reference.data.ErrorResponse
import org.stellar.reference.service.sep31.CustomerService
import org.stellar.reference.service.sep31.FeeService
import org.stellar.reference.service.sep31.RateService
import org.stellar.reference.service.sep31.UniqueAddressService

private val log = KotlinLogging.logger {}

fun Route.sep31(
  feeService: FeeService,
  uniqueAddressService: UniqueAddressService,
  customerService: CustomerService,
  rateService: RateService
) {

  route("/fee") {
    get {
      try {
        call.respond(
          feeService.getFee(
            Json.decodeFromString(
              Json.encodeToString(
                call.request.queryParameters.entries().associate { it.key to it.value.first() }
              )
            )
          )
        )
      } catch (e: ClientException) {
        log.error(e)
        call.respond(HttpStatusCode.BadRequest, ErrorResponse(e.message!!))
      } catch (e: Exception) {
        log.error(e)
        call.respond(
          HttpStatusCode.InternalServerError,
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
        call.respond(HttpStatusCode.BadRequest, ErrorResponse(e.message!!))
      } catch (e: Exception) {
        log.error(e)
        call.respond(
          HttpStatusCode.InternalServerError,
          ErrorResponse("Error occurred: ${e.message}"),
        )
      }
    }
  }

  route("/customer") {
    get {
      try {
        call.respond(
          customerService.getCustomer(
            Json.decodeFromString(
              Json.encodeToString(
                call.request.queryParameters.entries().associate { it.key to it.value.first() }
              )
            )
          )
        )
      } catch (e: ClientException) {
        log.error(e)
        call.respond(HttpStatusCode.BadRequest, ErrorResponse(e.message!!))
      } catch (e: NotFoundException) {
        log.error(e)
        call.respond(HttpStatusCode.NotFound, ErrorResponse(e.message!!))
      } catch (e: Exception) {
        log.error(e)
        call.respond(
          HttpStatusCode.InternalServerError,
          ErrorResponse("Error occurred: ${e.message}"),
        )
      }
    }
  }

  route("/customer") {
    put {
      try {
        call.respond(customerService.upsertCustomer(call.receive()))
      } catch (e: ClientException) {
        log.error(e)
        call.respond(HttpStatusCode.BadRequest, ErrorResponse(e.message!!))
      } catch (e: NotFoundException) {
        log.error(e)
        call.respond(HttpStatusCode.NotFound, ErrorResponse(e.message!!))
      } catch (e: Exception) {
        log.error(e)
        call.respond(
          HttpStatusCode.InternalServerError,
          ErrorResponse("Error occurred: ${e.message}"),
        )
      }
    }
  }

  route("/customer/{id}") {
    delete {
      try {
        customerService.delete(call.parameters["id"]!!)
        call.respond(HttpStatusCode.OK)
      } catch (e: ClientException) {
        log.error(e)
        call.respond(HttpStatusCode.BadRequest, ErrorResponse(e.message!!))
      } catch (e: Exception) {
        log.error(e)
        call.respond(
          HttpStatusCode.InternalServerError,
          ErrorResponse("Error occurred: ${e.message}"),
        )
      }
    }
  }

  route("/rate") {
    get {
      try {
        call.respond(
          rateService.getRate(
            Json.decodeFromString(
              Json.encodeToString(
                call.request.queryParameters.entries().associate { it.key to it.value.first() }
              )
            )
          )
        )
      } catch (e: ClientException) {
        log.error(e)
        call.respond(HttpStatusCode.BadRequest, ErrorResponse(e.message!!))
      } catch (e: NotFoundException) {
        log.error(e)
        call.respond(HttpStatusCode.NotFound, ErrorResponse(e.message!!))
      } catch (e: Exception) {
        log.error(e)
        call.respond(
          HttpStatusCode.InternalServerError,
          ErrorResponse("Error occurred: ${e.message}"),
        )
      }
    }
  }
}
