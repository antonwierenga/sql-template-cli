/*
 * Copyright 2017 Anton Wierenga
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.antonwierenga.sqltemplatecli.command

import collection.JavaConversions._
import com.antonwierenga.sqltemplatecli.Application
import com.antonwierenga.sqltemplatecli.domain.Database
import com.antonwierenga.sqltemplatecli.util.Console._
import com.antonwierenga.sqltemplatecli.util.Encryption
import com.antonwierenga.sqltemplatecli.util.PrintStackTraceExecutionProcessor
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.text.SimpleDateFormat
import java.util.Date
import org.apache.log4j.Logger
import java.util.Properties
import java.util.UUID

import org.springframework.shell.table.ArrayTableModel
import org.springframework.shell.table.BorderStyle
import org.springframework.shell.table.TableBuilder

import scala.tools.jline.console.ConsoleReader

abstract class Commands extends PrintStackTraceExecutionProcessor with Encryption {

  val logger = Logger.getLogger(this.getClass.getName)

  val numberFormatter = java.text.NumberFormat.getIntegerInstance

  def parseBoolean(string: String, default: Boolean): Boolean = {
    if (Option(string).isDefined && List("true", "t", "1", "yes", "y").contains(string.toLowerCase)) { true }
    else if (Option(string).isDefined && List("false", "f", "0", "no", "n").contains(string.toLowerCase)) { false }
    else if (!Option(string).isDefined) { default }
    else { throw new IllegalArgumentException(s"Failed to convert '$string' to type Boolean") }
  }

  def format(number: Long): String = {
    numberFormatter.format(number)
  }

  def formatDuration(duration: Long): String = {
    val formatTimeUnit = (l: Long, timeUnit: String) ⇒ if (l == 0) None else if (l == 1) s"$l $timeUnit" else s"$l ${timeUnit}s"
    List(
      formatTimeUnit((duration / (1000 * 60 * 60)) % 24, "hour"),
      formatTimeUnit((duration / (1000 * 60)) % 60, "minute"),
      formatTimeUnit((duration / 1000) % 60, "second")
    ).filter(_ != None).mkString(" ")
  }

  def confirm(force: String = "no", promptString: Option[String]): Unit = {
    force match {
      case "yes" ⇒ // skip confirmation
      case _ ⇒
        if (!List("Y", "y").contains(new ConsoleReader().readLine(prompt(s"${promptString.getOrElse("Are you sure?")} (Y/N): ")))) {
          throw new IllegalArgumentException("Command aborted")
        }
    }
  }

  def renderTable(header: Array[Object], data: Array[Array[Object]], tableBorder: Boolean, headerRow: Boolean): Object = {
    var tableData: Array[Array[Object]] = Array[Array[Object]]()
    if (headerRow) tableData = (tableData :+ header)
    tableData = (tableData ++ data)
    var tableBuilder = new TableBuilder(new ArrayTableModel(tableData))
    tableBuilder = if (tableBorder) { tableBuilder.addFullBorder(BorderStyle.oldschool) }
    else if (!tableBorder && headerRow) { tableBuilder.addHeaderAndVerticalsBorders(BorderStyle.air) }
    else { tableBuilder }

    tableBuilder.build.render(jline.TerminalFactory.get().getWidth())
  }

  def withConnection(callback: (Connection, Database) ⇒ String, pDatabase: Option[String] = None): String = {
    var connection: Option[Connection] = None
    try {
      if (!Application.database.isDefined && !pDatabase.isDefined) {
        throw new IllegalArgumentException("you should be connected to a database or specify the --database option for this command")
      }

      val database = if (pDatabase.isDefined) getDatabase(pDatabase.get) else Application.database.get
      val connectionProps = new Properties()
      connectionProps.put("user", database.username)
      connectionProps.put("password", database.password)
      connection = Some(DriverManager.getConnection(database.url, connectionProps))
      callback(connection.get, database)
    } catch {
      case e: Exception ⇒ {
        //e.printStackTrace
        logger.error("ERROR ", e)
        error(if (Option(e.getMessage).isDefined && e.getMessage.startsWith("ERROR:")) e.getMessage else s"ERROR: ${e.getMessage}")
      }
    } finally {
      if (connection.isDefined) {
        connection.get.close()
      }
    }
  }

  def getDatabase(pAlias: String): Database = {
    if (!Application.Config.hasPath(s"database.${pAlias}")) throw new IllegalArgumentException(s"database '${pAlias}' not found in ${new File(System.getProperty("config.file")).getCanonicalPath}") //scalastyle:ignore

    val username = if (Application.Config.hasPath(s"database.${pAlias}.username")) {
      Application.Config.getString(s"database.${pAlias}.username")
    } else {
      new ConsoleReader().readLine(prompt("Enter username: "))
    }

    val password = if (Application.Config.hasPath(s"database.${pAlias}.password.encrypted")) {
      decrypt(Application.Config.getString(s"database.${pAlias}.password.encrypted"))
    } else if (Application.Config.hasPath(s"database.${pAlias}.password")) {
      Application.Config.getString(s"database.${pAlias}.password")
    } else {
      new ConsoleReader().readLine(prompt("Enter password: "), new Character('*'))
    }

    new Database(pAlias, Application.Config.getString(s"database.${pAlias}.url"), Application.Config.getString(s"database.${pAlias}.url"), username, password)
  }
}
