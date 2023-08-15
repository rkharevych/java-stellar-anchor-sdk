package org.stellar.reference.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.logging.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.stellar.reference.ClientException
import org.stellar.reference.NotFoundException
import org.stellar.reference.data.ErrorResponse
import org.stellar.reference.data.MessageResponse
import org.stellar.reference.data.Success
import org.stellar.reference.service.sep31.CustomerService
import org.stellar.reference.service.sep31.ReceiveService

private val log = KotlinLogging.logger {}

fun Route.testSep31(customerService: CustomerService, receiveService: ReceiveService) {

  route("/invalidate_clabe/{id}") {
    get {
      try {
        customerService.invalidateCustomerClabe(call.parameters["id"]!!)
        call.respond(HttpStatusCode.OK)
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

  route("/sep31/transactions/{transactionId}/process") {
    post {
      try {
        val transactionId =
          call.parameters["transactionId"]
            ?: throw ClientException("Missing transactionId parameter")

        // Run receive processing asynchronously
        CoroutineScope(Job()).launch { receiveService.processReceive(transactionId) }

        call.respond(Success(transactionId))
      } catch (e: ClientException) {
        log.error(e)
        call.respond(HttpStatusCode.BadRequest, MessageResponse(e.message!!))
      } catch (e: Exception) {
        log.error(e)
        call.respond(
          HttpStatusCode.InternalServerError,
          MessageResponse("Error occurred: ${e.message}"),
        )
      }
    }
  }
}
