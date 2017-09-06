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

import com.antonwierenga.sqltemplatecli.Application
import com.antonwierenga.sqltemplatecli.Application.ApplicationOutputPath
import com.antonwierenga.sqltemplatecli.OutputFormat
import com.antonwierenga.sqltemplatecli.domain.Database
import com.antonwierenga.sqltemplatecli.domain.Template
import com.antonwierenga.sqltemplatecli.domain.Table
import com.antonwierenga.sqltemplatecli.util.Console._
import com.antonwierenga.sqltemplatecli.util.Implicits._
import com.antonwierenga.sqltemplatecli.util.Encryption

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.FileOutputStream
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.PreparedStatement
import java.sql.Statement
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Properties

import org.apache.commons.io.FilenameUtils
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.IndexedColors._
import org.apache.poi.hssf.usermodel._
import org.apache.poi.hssf.util._
import org.apache.poi.ss.usermodel.BorderStyle

import org.springframework.shell.core.annotation.CliAvailabilityIndicator
import org.springframework.shell.core.annotation.CliCommand
import org.springframework.shell.core.annotation.CliOption
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._
import scala.tools.jline.console.ConsoleReader
import scala.util.Failure
import scala.util.Success
import scala.util.Try

@Component
class DatabaseCommands extends Commands with Encryption {

  val MaxRowsHSSF = 65535
  val MaxRowsParameterUndefined = -2

  @CliAvailabilityIndicator(Array("info", "disconnect"))
  def isDatabaseAvailable: Boolean = Application.database.isDefined

  @CliCommand(value = Array("disconnect"), help = "Disconnect from the database")
  def disconnect(): String = {
    Application.database = None
    info(s"Disconnected from database")
  }

  @CliCommand(value = Array("execute"), help = "Executes a SQL statement")
  def execute( //scalastyle:ignore 
    // The number of parameters should not exceed 8
    @CliOption(key = Array("sql"), mandatory = false, help = "The sql") pSql: String,
    @CliOption(key = Array("template"), mandatory = false, help = "The template") pTemplate: Template,
    @CliOption(key = Array("database"), mandatory = false, help = "The database") pDatabase: Database,
    @CliOption(key = Array("max-rows"), mandatory = false, unspecifiedDefaultValue = "-2",
      help = "The maximum number of rows to fetch (0 fetches all rows)") pMaxRows: Int = MaxRowsParameterUndefined,
    @CliOption(key = Array("timeout"), mandatory = false, unspecifiedDefaultValue = "0",
      help = "Number of seconds before the statement will timeout") pTimeout: Int = 0,
    @CliOption(key = Array("table-border"), mandatory = false, help = "Table border for the query results") pTableBorder: String = null, //scalastyle:ignore
    @CliOption(key = Array("header-row"), mandatory = false, help = "Table border for the query results") pHeaderRow: String = null, //scalastyle:ignore
    @CliOption(key = Array("columns"), mandatory = false, help = "The columns to include in the query results") pColumns: String = null, //scalastyle:ignore
    @CliOption(key = Array("output-path"), mandatory = false, specifiedDefaultValue = "specified",
      help = "The name of the file to write the query results to") pOutputPath: String = null, //scalastyle:ignore
    @CliOption(key = Array("output-format"), mandatory = false, unspecifiedDefaultValue = "console",
      help = "The output format") pOutputFormat: OutputFormat = OutputFormat.console,
    @CliOption(key = Array("p1"), mandatory = false, help = "Parameter 1") p1: String = null, //scalastyle:ignore
    @CliOption(key = Array("p2"), mandatory = false, help = "Parameter 2") p2: String = null, //scalastyle:ignore
    @CliOption(key = Array("p3"), mandatory = false, help = "Parameter 3") p3: String = null, //scalastyle:ignore
    @CliOption(key = Array("p4"), mandatory = false, help = "Parameter 4") p4: String = null, //scalastyle:ignore
    @CliOption(key = Array("p5"), mandatory = false, help = "Parameter 5") p5: String = null, //scalastyle:ignore
    @CliOption(key = Array("p6"), mandatory = false, help = "Parameter 6") p6: String = null, //scalastyle:ignore
    @CliOption(key = Array("p7"), mandatory = false, help = "Parameter 7") p7: String = null, //scalastyle:ignore
    @CliOption(key = Array("p8"), mandatory = false, help = "Parameter 8") p8: String = null, //scalastyle:ignore
    @CliOption(key = Array("p9"), mandatory = false, help = "Parameter 9") p9: String = null, //scalastyle:ignore
    @CliOption(key = Array("p10"), mandatory = false, help = "Parameter 10") p10: String = null, //scalastyle:ignore
    @CliOption(key = Array("p11"), mandatory = false, help = "Parameter 11") p11: String = null, //scalastyle:ignore
    @CliOption(key = Array("p12"), mandatory = false, help = "Parameter 12") p12: String = null, //scalastyle:ignore
    @CliOption(key = Array("p13"), mandatory = false, help = "Parameter 13") p13: String = null, //scalastyle:ignore
    @CliOption(key = Array("p14"), mandatory = false, help = "Parameter 14") p14: String = null, //scalastyle:ignore
    @CliOption(key = Array("p15"), mandatory = false, help = "Parameter 15") p15: String = null, //scalastyle:ignore
    @CliOption(key = Array("p16"), mandatory = false, help = "Parameter 16") p16: String = null, //scalastyle:ignore
    @CliOption(key = Array("p17"), mandatory = false, help = "Parameter 17") p17: String = null, //scalastyle:ignore
    @CliOption(key = Array("p18"), mandatory = false, help = "Parameter 18") p18: String = null, //scalastyle:ignore
    @CliOption(key = Array("p19"), mandatory = false, help = "Parameter 19") p19: String = null, //scalastyle:ignore
    @CliOption(key = Array("p20"), mandatory = false, help = "Parameter 20") p20: String = null //scalastyle:ignore
  ): String = {

    withConnection((connection: Connection, database: Database) ⇒ {
      val start = System.currentTimeMillis
      val sql: String = if (Option(pTemplate).isDefined) getTemplate(pTemplate.name, Option(database)) else pSql
      var maxRowsReached = false
      val preparedStatement = connection.prepareStatement(sql)
      val tableBorder = parseBoolean(pTableBorder, Application.Config.getBoolean(s"console.table.border"))
      val headerRow = parseBoolean(pHeaderRow, Application.Config.getBoolean(s"console.table.header"))
      val maxRows = if (pMaxRows > 0) {
        pMaxRows
      } else if (pMaxRows == MaxRowsParameterUndefined && pOutputFormat == OutputFormat.console) {
        Application.Config.getInt(s"console.rows.max")
      } else {
        0
      }

      if (maxRows > 0) preparedStatement.setMaxRows(maxRows + 1)

      val columns: Array[String] = if (pColumns) pColumns.split(",").map(_.trim.toLowerCase) else Array()

      if (pTimeout > 0) preparedStatement.setQueryTimeout(pTimeout)
      val parameterArray = Array(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20)
      setParameters(preparedStatement, parameterArray)
      val result = if (preparedStatement.execute()) {
        val resultSet = preparedStatement.getResultSet
        val resultSetMetaData = resultSet.getMetaData
        val columnCount = resultSetMetaData.getColumnCount()

        val selectedColumms: List[Int] = if (!columns.isEmpty) {
          List.range(1, columnCount + 1)
            .map(columnIndex ⇒ (columnIndex, resultSetMetaData.getColumnLabel(columnIndex))).filter(column ⇒ columns.contains(column._2.toLowerCase)).map(_._1)
        } else {
          List.range(1, columnCount + 1)
        }

        if (!columns.isEmpty && selectedColumms.isEmpty) {
          throw new IllegalArgumentException(s"None of the specified columns '${pColumns}' appear in the query result columns: '${
            List.range(1, columnCount + 1).map(resultSetMetaData.getColumnLabel(_)).mkString(",")
          }'")
        }

        var numberOfRows = 0
        val formatResultTuple = pOutputFormat match {
          case OutputFormat.console ⇒ formatConsole(resultSetMetaData, resultSet, selectedColumms, tableBorder, headerRow, maxRows)
          case OutputFormat.excel ⇒ formatExcel(preparedStatement, resultSetMetaData, resultSet, sql, parameterArray, selectedColumms, headerRow, maxRows,
            Option(pOutputPath))
          case OutputFormat.csv ⇒ formatCSV(preparedStatement, resultSetMetaData, resultSet, sql, parameterArray, selectedColumms, headerRow, maxRows,
            Option(pOutputPath))
        }
        maxRowsReached = resultSet.next()
        formatResultTuple._2 + (if (maxRowsReached) s" (maximum number of rows reached)" else "")
      } else if (isDmlStatement(sql)) {
        s"Statement executed: ${preparedStatement.getUpdateCount} rows affected"
      } else {
        "Statement executed"
      }

      val duration = System.currentTimeMillis - start
      if (duration > 1000) {
        s"${result}. Duration: ${formatDuration(duration)}"
      } else {
        result
      }
    }, if (Option(pDatabase).isDefined) Option(pDatabase.alias) else None)
  }

  @CliCommand(value = Array("show-template"), help = "Shows the template")
  def showTemplate(
    @CliOption(key = Array("name"), mandatory = true, help = "The name of the template") pTemplate: Template
  ): String = {
    try {
      getTemplate(pTemplate.name, Application.database)
    } catch {
      case e: Exception ⇒ {
        error(e.getMessage)
      }
    }
  }

  @CliCommand(value = Array("connect"), help = "Connects to a database")
  def connect(
    @CliOption(key = Array("database"), mandatory = true, help = "The database alias") pAlias: Database
  ): String = {
    withConnection((connection: Connection, database: Database) ⇒ {
      Application.database = Option(database)
      Application.database.get.tables = try {
        val preparedStatement = connection.prepareStatement(scala.io.Source.fromFile(getProvidedScriptFile(database, "tables")).mkString)
        preparedStatement.setString(1, "")
        preparedStatement.execute()
        val resultSet = preparedStatement.getResultSet
        var tables = List[String]()
        while (resultSet.next()) {
          tables = tables :+ resultSet.getString(1)
        }
        tables
      } catch {
        case e: Exception ⇒ {
          List[String]()
        }
      }

      info(s"Connected to database '${Application.database.get.alias}'") +
        Application.getTemplateDirs().filter(file ⇒ !file.exists).map(file ⇒
          warn(s"\nWARNING: configured template dir '${file}' does not exist!")).mkString("") +
        Application.getTemplateDirs().filter(file ⇒ file.exists && !file.isDirectory).map(file ⇒
          warn(s"\nWARNING: configured template dir '${file}' is not a directory!")).mkString("")
    }, Option(pAlias.alias))
  }

  @CliCommand(value = Array("tables"), help = "Shows the tables")
  def tables(
    @CliOption(key = Array("filter"), mandatory = false, help = "The filter") pFilter: String,
    @CliOption(key = Array("database"), mandatory = false, help = "The database alias") pDatabase: Database
  ): String = {
    provided(pDatabase, "tables", if (Option(pFilter).isDefined) pFilter else "")
  }

  @CliCommand(value = Array("columns"), help = "Shows the columns")
  def columns(
    @CliOption(key = Array("table"), mandatory = true, help = "The table name") pTable: Table,
    @CliOption(key = Array("database"), mandatory = false, help = "The database alias") pDatabase: Database
  ): String = {
    provided(pDatabase, "columns", pTable.name)
  }

  @CliCommand(value = Array("data"), help = "Lists records of a table (or view)")
  def data( //scalastyle:ignore
    // The number of parameters should not exceed 8
    @CliOption(key = Array("table"), mandatory = true, help = "The table name") pTable: Table,
    @CliOption(key = Array("max-rows"), mandatory = false, unspecifiedDefaultValue = "-2",
      help = "The maximum number of rows to fetch (0 fetches all rows)") pMaxRows: Int = MaxRowsParameterUndefined,
    @CliOption(key = Array("timeout"), mandatory = false, unspecifiedDefaultValue = "0",
      help = "Number of seconds before the statement will timeout") pTimeout: Int = 0,
    @CliOption(key = Array("table-border"), mandatory = false, help = "Table border for the query results") pTableBorder: String,
    @CliOption(key = Array("header-row"), mandatory = false, help = "Table border for the query results") pHeaderRow: String,
    @CliOption(key = Array("columns"), mandatory = false, help = "The columns to include in the query results") pColumns: String,
    @CliOption(key = Array("filter"), mandatory = false, help = "The filter") pFilter: String,
    @CliOption(key = Array("output-path"), mandatory = false, specifiedDefaultValue = "specified",
      help = "The name of the file to write the query results to") pOutputPath: String,
    @CliOption(key = Array("count"), mandatory = false, specifiedDefaultValue = "specified",
      help = "If specified the query result is the row count") pCount: String,
    @CliOption(key = Array("output-format"), mandatory = false, unspecifiedDefaultValue = "console",
      help = "The output format") pOutputFormat: OutputFormat = OutputFormat.console,
    @CliOption(key = Array("order"), mandatory = false, help = "The columns to order") pOrder: String,
    @CliOption(key = Array("database"), mandatory = false, help = "The database alias") pDatabase: Database
  ): String = {
    withConnection((connection: Connection, database: Database) ⇒ {
      execute(s"select ${if (pCount) "count(*)" else "*"} from ${pTable.name} ${if (pFilter) "where " + pFilter else ""} ${if (pOrder) "order by " + pOrder else ""}", null, pDatabase, //scalastyle:ignore
        pMaxRows = pMaxRows, pTimeout = pTimeout, pTableBorder = pTableBorder, pHeaderRow = pHeaderRow, pColumns = pColumns,
        pOutputPath = pOutputPath, pOutputFormat = pOutputFormat)
    }, if (Option(pDatabase).isDefined) Option(pDatabase.alias) else None)
  }

  @CliCommand(value = Array("version"), help = "Shows the database version")
  def version(
    @CliOption(key = Array("database"), mandatory = false, help = "The database alias") pDatabase: Database
  ): String = {
    provided(pDatabase, "version")
  }

  def provided(pDatabase: Database, pCommand: String, p1: String = null, p2: String = null): String = { //scalastyle:ignore
    withConnection((connection: Connection, database: Database) ⇒ {
      val databaseType = database.url.substring(database.url.indexOf(":") + 1, database.url.indexOf(":", database.url.indexOf(":") + 1))
      execute(null, new Template(getProvidedScriptFile(database, pCommand).getPath), pDatabase, p1 = p1, p2 = p2, pMaxRows = 0) //scalastyle:ignore
    }, if (Option(pDatabase).isDefined) Option(pDatabase.alias) else None)
  }

  def setParameters(preparedStatement: PreparedStatement, parameterArray: Array[String]): Unit = { //scalastyle:ignore

    for (i ← List.range(1, preparedStatement.getParameterMetaData().getParameterCount() + 1)) {
      if (parameterArray(i - 1) == null) throw new IllegalArgumentException(s"You should specify option --p$i for this statement") //scalastyle:ignore

      val parameterClassResult = Try(Class.forName(preparedStatement.getParameterMetaData().getParameterClassName(i)))
      parameterClassResult match {
        case Success(parameterClass) ⇒
          try {
            if (parameterArray(i - 1).toLowerCase == "null") { preparedStatement.setNull(i, preparedStatement.getParameterMetaData().getParameterType(i)) }
            else if (parameterClass == classOf[String]) { preparedStatement.setString(i, parameterArray(i - 1)) }
            else if (parameterClass == classOf[java.math.BigDecimal]) { preparedStatement.setBigDecimal(i, BigDecimal(parameterArray(i - 1)).bigDecimal) }
            else if (parameterClass == classOf[java.lang.Byte]) { preparedStatement.setByte(i, parameterArray(i - 1).toByte.byteValue) }
            else if (parameterClass == classOf[java.lang.Boolean]) { preparedStatement.setBoolean(i, parameterArray(i - 1).toBoolean.booleanValue) }
            else if (parameterClass == classOf[java.lang.Double]) { preparedStatement.setDouble(i, parameterArray(i - 1).toDouble.doubleValue) }
            else if (parameterClass == classOf[java.lang.Float]) { preparedStatement.setFloat(i, parameterArray(i - 1).toFloat.floatValue) }
            else if (parameterClass == classOf[java.lang.Long]) { preparedStatement.setLong(i, parameterArray(i - 1).toLong.longValue) }
            else if (parameterClass == classOf[java.lang.Short]) { preparedStatement.setShort(i, parameterArray(i - 1).toShort.shortValue) }
            else if (parameterClass == classOf[java.lang.Integer]) { preparedStatement.setInt(i, parameterArray(i - 1).toInt.intValue) }
            else if (parameterClass == classOf[java.sql.Time]) { preparedStatement.setTime(i, parameterArray(i - 1)) }
            else if (parameterClass == classOf[java.sql.Timestamp]) { preparedStatement.setTimestamp(i, parameterArray(i - 1)) }
            else if (parameterClass == classOf[java.sql.Date]) { preparedStatement.setDate(i, parameterArray(i - 1)) }
            else {
              throw new IllegalArgumentException(s"Parameter ${preparedStatement.getParameterMetaData().getParameterClassName(i)} not supported")
            }
          } catch {
            case e: Exception ⇒ {
              throw new IllegalArgumentException(s"Failed to convert --p$i '${parameterArray(i - 1)}' to type ${parameterClass.getSimpleName.toLowerCase}" +
                (if (parameterClass == classOf[java.sql.Date]) { s" using pattern '${Application.DateFormatter.toPattern}'" }
                else if (parameterClass == classOf[java.sql.Time] || parameterClass == classOf[java.sql.Timestamp]) {
                  s" using pattern '${Application.DateTimeFormatter.toPattern}'"
                } else if (parameterClass == classOf[java.lang.Boolean]) {
                  s". Specify 'true' or 'false'"
                } else {
                  ""
                }))
            }
          }
        case Failure(e) ⇒
          // JDBC Driver does not support getParameterClassName, fallback to setDate or setString and let the database handle the casting
          if (setTimestampParameter(preparedStatement, i, parameterArray(i - 1))) {}
          else if (setDateParameter(preparedStatement, i, parameterArray(i - 1))) {}
          else { preparedStatement.setString(i, parameterArray(i - 1)) }
      }
    }
  }

  def setTimestampParameter(preparedStatement: PreparedStatement, parameterIndex: Int, parameter: String): Boolean = {
    try {
      preparedStatement.setTimestamp(parameterIndex, parameter)
      true
    } catch {
      case pe: ParseException ⇒ {
        false
      }
    }
  }

  def setDateParameter(preparedStatement: PreparedStatement, parameterIndex: Int, parameter: String): Boolean = {
    try {
      preparedStatement.setDate(parameterIndex, parameter)
      true
    } catch {
      case pe: ParseException ⇒ {
        false
      }
    }
  }

  def getTemplate(name: String, database: Option[Database]): String = {
    val templateFile = if (new File(name).isAbsolute()) {
      if (!new File(name).exists()) {
        throw new IllegalArgumentException(s"ERROR: template $name not found!")
      } else {
        new File(name)
      }
    } else {
      val templateFileName = if (FilenameUtils.getExtension(name)) name else s"${name}.sql"
      val templateFiles = Application.getTemplateDirs(database).filter(dir ⇒ new File(dir, templateFileName).exists).map(dir ⇒ new File(dir, templateFileName))
      if (templateFiles.isEmpty) {
        throw new IllegalArgumentException(s"ERROR: template $templateFileName not found in: " + Application.getTemplateDirs(database)
          .map(file ⇒ s"\n${file}").mkString(""))
      } else {
        templateFiles.head
      }
    }
    val source = scala.io.Source.fromFile(templateFile)
    try source.mkString finally source.close()
  }

  def isDmlStatement(sql: String): Boolean = {
    var firstStatementLine: String = null //scalastyle:ignore
    val iterator = sql.split("\n").iterator
    while (iterator.hasNext && !firstStatementLine) {
      val line = iterator.next.trim.toLowerCase
      if (line && !line.startsWith("-")) {
        firstStatementLine = line
      }
    }
    firstStatementLine != null && (firstStatementLine.startsWith("update") || firstStatementLine.startsWith("delete") //scalastyle:ignore
      || firstStatementLine.startsWith("insert"))
  }

  @CliCommand(value = Array("encrypt-password"), help = "Encrypts a password for use in sql-template-cli.conf")
  def encryptString(): String = {
    "Replace [DATABASE_ALIAS] in below line and add it to conf/sql-template-cli.conf\n\ndatabase.[DATABASE_ALIAS].password.encrypted=\"" +
      encrypt(new ConsoleReader().readLine(prompt("Enter password: "), new Character('*'))) + "\""
  }

  def formatConsole(resultSetMetaData: ResultSetMetaData, resultSet: ResultSet, selectedColumms: List[Int], tableBorder: Boolean, headerRow: Boolean,
    maxRows: Int): Tuple2[Int, String] = {
    var data: Array[Array[Object]] = Array[Array[Object]]()
    var numberOfRows = 0
    while ((maxRows == 0 || numberOfRows < maxRows) && resultSet.next()) {
      val row: Array[Object] = selectedColumms.map(value ⇒ Option(resultSet.getString(value)).getOrElse("NULL")).toArray
      data = data :+ row
      numberOfRows = numberOfRows + 1
    }
    (numberOfRows, s"${
      renderTable(selectedColumms.map(resultSetMetaData.getColumnLabel(_)).toArray, data, tableBorder, headerRow)
        .toString
    }\nTotal rows: ${numberOfRows}")
  }

  def formatCSV(preparedStatement: PreparedStatement, resultSetMetaData: ResultSetMetaData, resultSet: ResultSet, sql: String, parameterArray: Array[String], //scalastyle:ignore
    selectedColumms: List[Int], headerRow: Boolean, maxRows: Int, file: Option[String]): Tuple2[Int, String] = {
    val outputFile = getOutputFile(file, "csv")
    var numberOfRows = 0
    val bufferedWriter = new BufferedWriter(new FileWriter(outputFile))
    try {
      bufferedWriter.write(selectedColumms.map(value ⇒ {
        resultSetMetaData.getColumnLabel(value)
      }).mkString(";") + "\n")
      while ((maxRows == 0 || numberOfRows < maxRows) && resultSet.next()) {
        numberOfRows = numberOfRows + 1
        bufferedWriter.write(selectedColumms.map(value ⇒ {
          "\"" + Option(resultSet.getString(value)).getOrElse("") + "\""
        }).mkString(";") + "\n")
      }
    } finally {
      bufferedWriter.close
    }
    (numberOfRows, s"$numberOfRows rows exported to ${outputFile.getCanonicalPath()}")
  }

  def formatExcel(preparedStatement: PreparedStatement, resultSetMetaData: ResultSetMetaData, resultSet: ResultSet, sql: String, parameterArray: Array[String], //scalastyle:ignore 
    selectedColumms: List[Int], headerRow: Boolean, maxRows: Int, file: Option[String]): Tuple2[Int, String] = {
    val outputFile = getOutputFile(file, "xls")
    var numberOfRows = 0
    val fileOutputStream = new FileOutputStream(outputFile)
    val workbook = new HSSFWorkbook()
    val dataSheet = workbook.createSheet("Result")
    dataSheet.setDisplayZeros(true)

    val cellStyleHeader = getHeaderCellStyle(workbook)
    val cellStyleGray = getGrayCellStyle(workbook)
    val cellStyleWhite = getDefaultCellStyle(workbook)

    val headerRow = dataSheet.createRow(numberOfRows)
    selectedColumms.map(value ⇒ {
      val headerCell = headerRow.createCell(value - 1)
      headerCell.setCellStyle(cellStyleHeader)
      val header = resultSetMetaData.getColumnLabel(value)
      headerCell.setCellValue(if (Application.Config.getBoolean(s"excel.header.uppercase")) header.toUpperCase else header)
    })

    while ((maxRows == 0 || numberOfRows < maxRows) && resultSet.next()) {
      numberOfRows = numberOfRows + 1
      if (numberOfRows > MaxRowsHSSF) {
        fileOutputStream.close()
        outputFile.delete()
        throw new IllegalArgumentException(s"ERROR: number of rows exceeds the maximum number of rows supported for output-format 'excel' ($MaxRowsHSSF)")
      }

      val dataRow = dataSheet.createRow(numberOfRows)

      selectedColumms.map(value ⇒ {
        val dataCell = dataRow.createCell(value - 1)
        if (numberOfRows % 2 == 0) {
          dataCell.setCellStyle(cellStyleGray)
        } else {
          dataCell.setCellStyle(cellStyleWhite)
        }
        dataCell.setCellValue(Option(resultSet.getString(value)).getOrElse("NULL"))
      })
    }

    val sqlSheet = workbook.createSheet("SQL Statement")
    val sqlRow = sqlSheet.createRow(0)
    val sqlTitleCell = sqlRow.createCell(0)
    sqlTitleCell.setCellStyle(cellStyleHeader)
    sqlTitleCell.setCellValue("SQL Statement")
    val sqlCell = sqlRow.createCell(1)
    sqlCell.setCellStyle(cellStyleWhite)
    sqlCell.setCellValue(sql)

    for (i ← List.range(1, preparedStatement.getParameterMetaData().getParameterCount() + 1)) {
      val parameterRow = sqlSheet.createRow(i)
      val parameterTitleCell = parameterRow.createCell(0)
      parameterTitleCell.setCellStyle(cellStyleHeader)
      parameterTitleCell.setCellValue(s"Parameter $i")
      val parameterCell = parameterRow.createCell(1)
      parameterCell.setCellStyle(cellStyleWhite)
      parameterCell.setCellValue(parameterArray(i - 1))
    }

    List(0, 1).map(sqlSheet.autoSizeColumn(_))
    selectedColumms.map(value ⇒ dataSheet.autoSizeColumn(value - 1))
    workbook.write(fileOutputStream)
    fileOutputStream.close()
    (numberOfRows, s"$numberOfRows rows exported to ${outputFile.getCanonicalPath()}")
  }

  def getHeaderCellStyle(workbook: HSSFWorkbook): CellStyle = {
    val cellStyleHeader = getDefaultCellStyle(workbook)
    val fontHeader = workbook.createFont()
    fontHeader.setColor(HSSFColor.WHITE.index)
    cellStyleHeader.setFont(fontHeader)
    cellStyleHeader.setFillForegroundColor(HSSFColor.DARK_GREEN.index)
    cellStyleHeader.setFillPattern(CellStyle.SOLID_FOREGROUND)
    cellStyleHeader.setVerticalAlignment(CellStyle.VERTICAL_TOP)
    cellStyleHeader
  }

  def getGrayCellStyle(workbook: HSSFWorkbook): CellStyle = {
    val cellStyleGray = getDefaultCellStyle(workbook)
    cellStyleGray.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index)
    cellStyleGray.setFillPattern(CellStyle.SOLID_FOREGROUND)
    cellStyleGray
  }

  def getDefaultCellStyle(workbook: HSSFWorkbook): CellStyle = {
    val cellStyle = workbook.createCellStyle()
    cellStyle.setBorderBottom(BorderStyle.HAIR)
    cellStyle.setBorderTop(BorderStyle.HAIR)
    cellStyle.setBorderRight(BorderStyle.HAIR)
    cellStyle.setBorderLeft(BorderStyle.HAIR)
    cellStyle
  }

  def getOutputFile(outputFile: Option[String], extension: String): File = {
    val generatedName = s"export_${new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date())}.${extension}"
    var resultFileName = outputFile.getOrElse(generatedName)
    resultFileName = resultFileName.replaceFirst("^~", System.getProperty("user.home"));
    if (new File(resultFileName).isDirectory()) { resultFileName += s"/$generatedName" }
    //if (!resultFileName.contains('.')) { resultFileName += s".$extension" }

    val resultFile = Option(new File(resultFileName).getParent) match {
      case Some(parent) ⇒ new File(resultFileName)
      case _            ⇒ new File(ApplicationOutputPath, resultFileName)
    }

    if (resultFile.exists) {
      confirm(promptString = Option(s"File '$resultFile' already exists. Do you want to overwrite it?"))
    }
    resultFile
  }

  def getProvidedScriptFile(database: Database, provided: String): File = {
    val databaseType = database.url.substring(database.url.indexOf(":") + 1, database.url.indexOf(":", database.url.indexOf(":") + 1))
    val providedScriptFile = new File(s"${Application.ApplicationPath}/template/provided/${databaseType}/${provided}.sql")
    if (!providedScriptFile.exists()) {
      throw new RuntimeException(s"no default implementation found for command '${provided}' for database type '${databaseType}'.\n" +
        s"Provide your own implementation by creating '${providedScriptFile}' with the appropriate sql")
    }
    providedScriptFile
  }
}
