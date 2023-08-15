package org.stellar.reference.util

import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement

class H2Database(
  private val dbName: String = "testdb",
  private val username: String = "as",
  private val password: String = "",
  private val scriptPath: String = "/init.sql"
) {

  init {
    initializeDatabase()
  }

  private fun initializeDatabase() {
    // Open an H2 in-memory database connection
    val connection: Connection =
      DriverManager.getConnection(
        "jdbc:h2:mem:$dbName;DB_CLOSE_DELAY=-1;USER=$username;PASSWORD=$password"
      )

    // Initialize database using SQL script
    val script = this::class.java.getResource(scriptPath)?.readText()
    val statement: Statement = connection.createStatement()
    statement.execute(script)

    // Close the resources
    statement.close()
    connection.close()
  }

  fun getConnection(): Connection {
    return DriverManager.getConnection("jdbc:h2:mem:$dbName;USER=$username;PASSWORD=$password")
  }
}
