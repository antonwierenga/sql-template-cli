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

package com.antonwierenga.sqltemplatecli.plugin

import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.shell.plugin.support.DefaultBannerProvider
import org.springframework.stereotype.Component
import com.antonwierenga.sqltemplatecli.Application

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class BannerProvider extends DefaultBannerProvider {

  override def getBanner: String = {
    """   _____ ____    __       ______                     __      __          ________    ____
      |  / ___// __ \  / /      /_  __/__  ____ ___  ____  / /___ _/ /____     / ____/ /   /  _/
      |  \__ \/ / / / / /        / / / _ \/ __ `__ \/ __ \/ / __ `/ __/ _ \   / /   / /    / /
      | ___/ / /_/ / / /___     / / /  __/ / / / / / /_/ / / /_/ / /_/  __/  / /___/ /____/ /
      |/____/\___\_\/_____/    /_/  \___/_/ /_/ /_/ .___/_/\__,_/\__/\___/   \____/_____/___/
      |                                          /_/""".stripMargin
  }

  override def getVersion: String = Application.ReleaseNotes.keysIterator.next

  override def getWelcomeMessage: String = s"Welcome to SQL Template CLI $getVersion"

  override def getProviderName: String = "SQL Template CLI"
}
