package org.stellar.reference.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.logging.*
import mu.KotlinLogging
import org.stellar.reference.ClientException
import org.stellar.reference.NotFoundException
import org.stellar.reference.data.ErrorResponse
import org.stellar.reference.sep31.CustomerService

private val log = KotlinLogging.logger {}

fun Route.testSep31(customerService: CustomerService) {

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
}
