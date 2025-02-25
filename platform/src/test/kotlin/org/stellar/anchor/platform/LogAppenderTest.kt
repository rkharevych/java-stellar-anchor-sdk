package org.stellar.anchor.platform

import io.mockk.*
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.Appender
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.config.LoggerConfig
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.stellar.anchor.util.*

class LogAppenderTest {
  private lateinit var appender: Appender
  private lateinit var capturedLogEvent: CapturingSlot<LogEvent>
  private lateinit var loggerContext: LoggerContext
  private lateinit var rootLoggerConfig: LoggerConfig
  private lateinit var lastLevel: Level

  @BeforeEach
  fun setup() {
    appender = mockk()
    capturedLogEvent = slot()
    loggerContext = LogManager.getContext(false) as LoggerContext
    rootLoggerConfig = loggerContext.configuration.getLoggerConfig("org.stellar")
    lastLevel = rootLoggerConfig.level

    every { appender.name } returns "mock appender"
    every { appender.isStarted } returns true
    every { appender.append(capture(capturedLogEvent)) }

    rootLoggerConfig.addAppender(appender, Level.ALL, null)
    rootLoggerConfig.level = Level.TRACE
  }

  @AfterEach
  fun tearDown() {
    clearAllMocks()
    unmockkAll()
    rootLoggerConfig.removeAppender("mock appender")
    rootLoggerConfig.level = lastLevel
  }

  @ParameterizedTest
  @CsvSource(
    value =
      [
        "error,error_message,ERROR",
        "warn,warn_message,WARN",
        "info,info_message,INFO",
        "debug,debug_message,DEBUG",
        "trace,trace_message,TRACE",
      ]
  )
  fun test_logger_outputAccurateLoggerName(
    methodName: String,
    wantMessage: String,
    wantLevelName: String
  ) {
    when (methodName) {
      "error" -> Log.error(wantMessage)
      "warn" -> Log.warn(wantMessage)
      "info" -> Log.info(wantMessage)
      "debug" -> Log.debug(wantMessage)
      "trace" -> Log.trace(wantMessage)
    }

    // Verify that the append method was called, which ensures that the event was captured
    verify { appender.append(any()) }

    assertEquals(LogAppenderTest::class.qualifiedName, capturedLogEvent.captured.loggerName)
    assertEquals(wantLevelName, capturedLogEvent.captured.level.toString())
    assertEquals(wantMessage, capturedLogEvent.captured.message.toString())
    capturedLogEvent.clear()
  }
}
