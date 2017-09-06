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
import com.antonwierenga.sqltemplatecli.domain.Template

import java.io.File

import org.springframework.shell.core.Completion
import org.springframework.shell.core.Converter
import org.springframework.shell.core.MethodTarget
import org.springframework.stereotype.Component
import scala.collection.JavaConversions._

@Component
class TemplateConverter extends Converter[Template] {

  val ExtensionLength = 4

  @Override
  def convertFromText(text: String, requiredType: Class[_], optionContext: String): Template = {
    new Template(text);
  }

  @Override
  def supports(requiredType: Class[_], optionContext: String): Boolean = {
    classOf[Template].isAssignableFrom(requiredType)
  }

  @Override
  def getAllPossibleValues(completions: java.util.List[Completion], requiredType: Class[_],
    existingData: String, optionContext: String, target: MethodTarget): Boolean = {

    Application.getTemplateDirs.foreach { file ⇒
      if (file.exists() && file.isDirectory) {
        file.listFiles.filter(file ⇒ file.isFile && file.getName.endsWith(".sql"))
          .map(_.getName.toString.dropRight(ExtensionLength)).foreach(template ⇒ completions.add(new Completion(template)))
      }
    }
    true
  }
}
