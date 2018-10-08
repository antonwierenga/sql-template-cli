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

package com.antonwierenga.sqltemplatecli.util;

import java.io.File

import java.util.Date

import javax.activation.DataHandler
import javax.activation.FileDataSource
import javax.mail.Message
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMultipart
import javax.mail.Authenticator
import javax.mail.PasswordAuthentication

import com.antonwierenga.sqltemplatecli.Application

class Email {}

object Email extends Encryption {

  /**
   * Utility method to send simple HTML email
   * @param session
   * @param toEmail
   * @param subject
   * @param body
   */
  def send(to: String, subject: String, content: String, file: Option[File] = None): Unit = {

    val props = System.getProperties()
    props.put("mail.smtp.host", Application.Config.getString("mail.smtp.host"))
    props.put("mail.smtp.port", Application.Config.getString("mail.smtp.port"))

    props.put("mail.smtp.starttls.enable", Application.Config.getString("mail.smtp.starttls.enable"))

    val authenticator = if (Application.Config.hasPath("mail.smtp.username")) {
      props.put("mail.smtp.auth", "true");
      new Authenticator() {
        override def getPasswordAuthentication(): PasswordAuthentication = {
          val password = if (Application.Config.hasPath("mail.smtp.password.encrypted")) {
            decrypt(Application.Config.getString("mail.smtp.password.encrypted"))
          } else {
            Application.Config.getString("mail.smtp.password")
          }
          new PasswordAuthentication(Application.Config.getString("mail.smtp.username"), password)
        }
      }
    } else {
      null
    }

    val session = Session.getInstance(props, authenticator);
    val msg = new MimeMessage(session)
    msg.addHeader("Content-type", "text/HTML; charset=UTF-8")
    msg.addHeader("format", "flowed")
    msg.addHeader("Content-Transfer-Encoding", "8bit")
    msg.setFrom(new InternetAddress(Application.Config.getString("mail.from.address"), Application.Config.getString("mail.from.name")))
    if (Application.Config.hasPath("mail.smtp.replyto.address")) {
      msg.setReplyTo(InternetAddress.parse(Application.Config.getString("mail.replyto.address"), false).asInstanceOf[Array[javax.mail.Address]])
    }
    msg.setSubject(subject, "UTF-8")

    val multipart = new MimeMultipart()

    val messageBodyPart = new MimeBodyPart()
    messageBodyPart.setContent(content, "text/html; charset=utf-8")
    multipart.addBodyPart(messageBodyPart)

    file match {
      case Some(f) ⇒
        val attachmentBodyPart = new MimeBodyPart()
        attachmentBodyPart.setDataHandler(new DataHandler(new FileDataSource(f.getCanonicalPath())))
        attachmentBodyPart.setFileName(f.getName)
        multipart.addBodyPart(attachmentBodyPart)
      case None ⇒ None
    }

    msg.setContent(multipart);
    msg.setSentDate(new Date())
    msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false).asInstanceOf[Array[javax.mail.Address]])
    Transport.send(msg);
  }
}
