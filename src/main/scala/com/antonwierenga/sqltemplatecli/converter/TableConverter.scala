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

package com.antonwierenga.sqltemplatecli.converter

import com.antonwierenga.sqltemplatecli.Application
import com.antonwierenga.sqltemplatecli.domain.Table

import java.io.File

import org.springframework.shell.core.Completion
import org.springframework.shell.core.Converter
import org.springframework.shell.core.MethodTarget
import org.springframework.stereotype.Component
import scala.collection.JavaConversions._

@Component
class TableConverter extends Converter[Table] {

  @Override
  def convertFromText(text: String, requiredType: Class[_], optionContext: String): Table = {
    val tableName = if (!text.contains('.')) {
      val tables = Application.database.get.tables.filter(_.endsWith(s".$text"))
      if (tables.size() == 1) {
        tables(0)
      } else {
        text
      }
    } else {
      text
    }
    new Table(tableName)
  }

  @Override
  def supports(requiredType: Class[_], optionContext: String): Boolean = {
    classOf[Table].isAssignableFrom(requiredType)
  }

  @Override
  def getAllPossibleValues(completions: java.util.List[Completion], requiredType: Class[_],
    existingData: String, optionContext: String, target: MethodTarget): Boolean = {
    if (Application.database.isDefined) {
      Application.database.get.tables.map { table ⇒
        completions.add(new Completion(table))
        if (Application.database.get.tables.filter(_.endsWith(s".${table.substring(table.lastIndexOf('.') + 1)}")).size() == 1) {
          completions.add(new Completion(table.substring(table.lastIndexOf('.') + 1)))
        }
      }
    }
    true
  }
}
