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

import javax.crypto.spec.{PBEParameterSpec, PBEKeySpec}
import javax.crypto.{Cipher, SecretKey, SecretKeyFactory}
import javax.xml.bind.DatatypeConverter

trait Encryption {

  val algorithm: String = "PBEWithMD5AndDES"
  val salt: Array[Byte] = Array(0xde.toByte, 0x33.toByte, 0x10.toByte, 0x12.toByte, 0xde.toByte, 0x33.toByte, 0x10.toByte, 0x12.toByte)
  val keyFactory: SecretKeyFactory = SecretKeyFactory.getInstance(algorithm)
  val key: SecretKey = keyFactory.generateSecret(new PBEKeySpec("rqoeihfeadfsadbfads".toCharArray))
  val pbeCipherEncryptor: Cipher = Cipher.getInstance(algorithm)
  val pbeCipherDecryptor: Cipher = Cipher.getInstance(algorithm)
  val PBEParameterSpecValue = 20

  pbeCipherEncryptor.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(salt, PBEParameterSpecValue))
  pbeCipherDecryptor.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(salt, PBEParameterSpecValue))

  def encrypt(value: String): String = {
    DatatypeConverter.printBase64Binary(pbeCipherEncryptor.doFinal(value.getBytes("UTF-8")))
  }

  def decrypt(value: String): String = {
    new String(pbeCipherDecryptor.doFinal(DatatypeConverter.parseBase64Binary(value)))
  }
}
