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

package com.antonwierenga.sqltemplatecli

import com.antonwierenga.sqltemplatecli.domain.Database
import com.antonwierenga.sqltemplatecli.util.Console._

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

import java.io.File
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.logging.LogManager
import java.util.logging.Level

import org.springframework.shell.Bootstrap
import org.springframework.shell.core.annotation.CliCommand
import org.springframework.shell.core.CommandMarker
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._

@Component
class Application extends CommandMarker {

  @CliCommand(value = Array("release-notes"), help = "Displays release notes")
  def releaseNotes: String = Application.ReleaseNotes.keySet.toSeq.sorted.map(x ⇒ s"$x\n" + Application.ReleaseNotes(x).map(y ⇒ s"    - $y")
    .mkString("\n")).mkString("\n")
}

object Application extends App {

  lazy val ReleaseNotes = Map(
    "v0.1.0" → List(
      "New shell command 'encrypt-mail-password'",
      "Updated shell command 'data' and 'execute': added options 'email-to', 'email-subject' and 'email-body'",
      "Renamed shell command 'encrypt-password' to 'encrypt-database-password': added option 'database'",
      "Updated shell commands 'data' and 'execute': renamed option 'output-format' to 'export-format",
      "Updated shell commands 'data' and 'execute': renamed option 'output-path' to 'export-path'",
      "Updated shell commands 'data' and 'execute': added option 'export-open' to automatically open the export file after it is created",
      "Updated shell commands 'data' and 'execute': added export-format 'insert' for insert statement generation (using --export-format insert)",
      "Updated shell commands 'data' and 'execute': added export-format 'html' for html generation (using --export-format html)",
      "Updated shell commands 'data' and 'execute': added --count option"
    ),
    "v0.0.1" → List(
      "Enhanced table auto-completion (no need to specify the schema when table name is given)",
      "Fixed a bug that caused an error when the sql-template-cli installation path contains a whitespace (Windows)"
    ),
    "v0.0.0" → List(
      "New shell command 'connect'",
      "New shell command 'columns'",
      "New shell command 'data'",
      "New shell command 'disconnect'",
      "New shell command 'encrypt-password'",
      "New shell command 'execute'",
      "New shell command 'release-notes'",
      "New shell command 'show-template'",
      "New shell command 'tables'",
      "New shell command 'version'"
    )
  )

  lazy val ApplicationPath: String = s"${Paths.get(classOf[Application].getProtectionDomain.getCodeSource.getLocation.toURI).toFile.getParentFile.getParentFile}"
  lazy val ApplicationOutputPath: File = new File(ApplicationPath, "output")

  if (!ApplicationOutputPath.exists()) ApplicationOutputPath.mkdir()

  System.setProperty("config.file", s"$ApplicationPath/conf/sql-template-cli.conf")
  System.setProperty("app_home", s"$ApplicationPath")

  lazy val Config: Config = ConfigFactory.load
  lazy val DateFormatter = new SimpleDateFormat(Application.Config.getString(s"parameter.date.pattern"))
  lazy val DateTimeFormatter = new SimpleDateFormat(Application.Config.getString(s"parameter.datetime.pattern"))

  DateFormatter.setLenient(false)
  DateTimeFormatter.setLenient(false)

  val Store = scala.collection.mutable.Map[String, Seq[Any]]()

  var database: Option[Database] = None

  Bootstrap.main(args)

  def getTemplateDirs(): List[File] = getTemplateDirs(database)

  def getTemplateDirs(pDatabase: Option[Database]): List[File] = {
    List(new File(s"${Application.ApplicationPath}/template/")) ++
      (if (pDatabase.isDefined && Application.Config.hasPath(s"database.${pDatabase.get.alias}.template.dirs")) {
        Application.Config.getStringList(s"database.${pDatabase.get.alias}.template.dirs").map(new File(_))
      } else {
        List()
      })
  }
}
