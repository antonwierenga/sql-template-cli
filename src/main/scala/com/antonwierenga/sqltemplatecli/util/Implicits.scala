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

package com.antonwierenga.sqltemplatecli.util

import com.antonwierenga.sqltemplatecli.Application
import com.typesafe.config.Config

import java.text.ParseException

import scala.collection.JavaConversions._

object Implicits {

  implicit class RichConfig(val underlying: Config) extends AnyVal {
    def getOptionalString(path: String): Option[String] = if (underlying.hasPath(path)) {
      Some(underlying.getString(path))
    } else {
      None
    }
  }

  /** Replicate Groovy Truth */
  implicit def stringToBoolean(s: String): Boolean = {
    !Option(s).getOrElse("").isEmpty
  }

  /** Replicate Groovy Truth */
  implicit def intToBoolean(i: Int): Boolean = {
    Option(i).getOrElse(0) != 0
  }

  /** Replicate Groovy Truth */
  implicit def optionStringToBoolean(o: Option[String]): Boolean = {
    !o.getOrElse("").isEmpty
  }

  implicit def stringToDate(s: String): java.sql.Date = {
    // format the parsed date and check that it equals the input string for more strict checking
    if (s.equals(Application.DateFormatter.format(Application.DateFormatter.parse(s)))) {
      new java.sql.Date(Application.DateFormatter.parse(s).getTime)
    } else {
      throw new ParseException(s"Invalid date: ${s}", -1)
    }
  }

  implicit def stringToTimestamp(s: String): java.sql.Timestamp = {
    // format the parsed date and check that it equals the input string for more strict checking
    if (s.equals(Application.DateTimeFormatter.format(Application.DateTimeFormatter.parse(s)))) {
      new java.sql.Timestamp(Application.DateTimeFormatter.parse(s).getTime)
    } else {
      throw new ParseException(s"Invalid timestamp: ${s}", -1)
    }
  }

  implicit def stringToTime(s: String): java.sql.Time = {
    new java.sql.Time(Application.DateTimeFormatter.parse(s).getTime)
  }
}
