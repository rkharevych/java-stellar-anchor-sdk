package org.stellar.reference

import com.sksamuel.hoplite.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.routing.*
import mu.KotlinLogging
import org.stellar.reference.data.Config
import org.stellar.reference.data.LocationConfig
import org.stellar.reference.event.EventService
import org.stellar.reference.plugins.*
import org.stellar.reference.repo.CustomerRepo
import org.stellar.reference.repo.QuoteRepo
import org.stellar.reference.service.SepHelper
import org.stellar.reference.service.sep24.DepositService
import org.stellar.reference.service.sep24.WithdrawalService
import org.stellar.reference.service.sep31.*
import org.stellar.reference.util.H2Database

val log = KotlinLogging.logger {}
lateinit var referenceKotlinSever: NettyApplicationEngine

fun main(args: Array<String>) {
  startServer(null, args.getOrNull(0)?.toBooleanStrictOrNull() ?: true)
}

fun startServer(envMap: Map<String, String>?, wait: Boolean) {
  log.info { "Starting Kotlin reference server" }

  // read config
  val cfg = readCfg(envMap)

  // start server
  referenceKotlinSever =
    embeddedServer(Netty, port = cfg.port) {
        install(ContentNegotiation) { json() }
        configureRouting(cfg)
        install(CORS) {
          anyHost()
          allowHeader(HttpHeaders.Authorization)
          allowHeader(HttpHeaders.ContentType)
        }
      }
      .start(wait)
}

fun readCfg(envMap: Map<String, String>?): Config {
  // Load location config
  val locationCfg =
    ConfigLoaderBuilder.default()
      .addPropertySource(PropertySource.environment())
      .build()
      .loadConfig<LocationConfig>()

  val cfgBuilder = ConfigLoaderBuilder.default()
  // Add environment variables as a property source.
  cfgBuilder.addPropertySource(PropertySource.environment())
  envMap?.run { cfgBuilder.addMapSource(this) }
  // Add config file as a property source if valid
  locationCfg.fold({}, { cfgBuilder.addFileSource(it.ktReferenceServerConfig) })
  // Add default config file as a property source.
  cfgBuilder.addResourceSource("/default-config.yaml")

  return cfgBuilder.build().loadConfigOrThrow<Config>()
}

fun stopServer() {
  if (::referenceKotlinSever.isInitialized) (referenceKotlinSever).stop(1000, 1000)
}

fun Application.configureRouting(cfg: Config) {
  routing {
    val helper = SepHelper(cfg)
    val depositService = DepositService(cfg)
    val withdrawalService = WithdrawalService(cfg)
    val eventService = EventService()
    val database = H2Database()
    val customerRepo = CustomerRepo(database)
    val quoteRepo = QuoteRepo(database)
    val feeService = FeeService(customerRepo)
    val uniqueAddressService = UniqueAddressService(cfg)
    val customerService = CustomerService(customerRepo)
    val rateService = RateService(quoteRepo)
    val receiveService = ReceiveService(cfg)

    sep24(helper, depositService, withdrawalService, cfg.sep24.interactiveJwtKey)
    sep31(feeService, uniqueAddressService, customerService, rateService)
    event(eventService)

    if (cfg.enableTest) {
      testSep24(helper, depositService, withdrawalService, cfg.sep24.interactiveJwtKey)
      testSep31(customerService, receiveService)
    }
  }
}
