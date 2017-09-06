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
  def releaseNotes: String = Application.ReleaseNotes.keySet.toSeq.sorted.reverse.map(x ⇒ s"$x\n" + Application.ReleaseNotes(x).map(y ⇒ s"    - $y")
    .mkString("\n")).mkString("\n")

}

object Application extends App {

  lazy val ReleaseNotes = Map("v0.0.0" → List(
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
  ))

  lazy val ApplicationPath: String = s"${new File(classOf[Application].getProtectionDomain.getCodeSource.getLocation.getFile).getParentFile.getParentFile}"
  lazy val ApplicationOutputPath: File = new File(ApplicationPath, "output")

  if (!ApplicationOutputPath.exists()) ApplicationOutputPath.mkdir()

  System.setProperty("config.file", s"$ApplicationPath/conf/sql-template-cli.conf")
  System.setProperty("app_home", s"$ApplicationPath")

  lazy val Config: Config = ConfigFactory.load
  lazy val DateFormatter = new SimpleDateFormat(Application.Config.getString(s"parameter.date.pattern"))
  lazy val DateTimeFormatter = new SimpleDateFormat(Application.Config.getString(s"parameter.datetime.pattern"))

  DateFormatter.setLenient(false)
  DateTimeFormatter.setLenient(false)

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
