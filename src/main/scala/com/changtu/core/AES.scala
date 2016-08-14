package com.changtu.core

/**
  * Created by lubinsu on 8/12/2016.
  */

import javax.crypto.spec.SecretKeySpec
import javax.crypto.{Cipher, KeyGenerator, SecretKey}

import jodd.util.Base64

object AES {

  def hex2bytes(hex: String): Array[Byte] = {
    hex.replaceAll("[^0-9A-Fa-f]", "").grouped(2).toArray.map(Integer.parseInt(_, 16).toByte)
  }

  def bytes2hex(bytes: Array[Byte], sep: Option[String] = None): String = {
    sep match {
      case None => bytes.map("%02x".format(_)).mkString
      case _ => bytes.map("%02x".format(_)).mkString(sep.get)
    }
  }

  // 生成AES密鈅
  @throws(classOf[Exception])
  def genKeyAES(): String = {
    val keyGen = KeyGenerator.getInstance("AES")
    keyGen.init(128)
    val key = keyGen.generateKey()
    val base64Str = Base64.encodeToString(key.getEncoded)
    base64Str
  }

  @throws(classOf[Exception])
  def loadKeyAES(base64Key: String): SecretKey = {
    val bytes = Base64.decode(base64Key)
    val key = new SecretKeySpec(bytes, "AES")
    key
  }

  @throws(classOf[Exception])
  def encrytAES(source: Array[Byte], key: SecretKey): Array[Byte] = {
    val cipher = Cipher.getInstance("AES")
    cipher.init(Cipher.ENCRYPT_MODE, key)
    cipher.doFinal(source)
  }

  @throws(classOf[Exception])
  def decryptAES(source: Array[Byte], key: SecretKey): Array[Byte] = {
    val cipher = Cipher.getInstance("AES")
    cipher.init(Cipher.DECRYPT_MODE, key)
    cipher.doFinal(source)
  }

  def main(args: Array[String]): Unit = {
    val content = "0(s6bXju"
    val key = "mlE8Q+VsJOhWxMWT+ERnkw=="
    //val key = genKeyAES()
    println("原文: " + content)
    println("AES算法密钥: " + key)
    val keyAes = loadKeyAES(key)
    val encodeMsg = encrytAES(content.getBytes(), keyAes)
    println("AES密文字符串: " + bytes2hex(encodeMsg))

    // 解密验证
    println("解密后原文: " + new String(decryptAES(hex2bytes(bytes2hex(encodeMsg)), keyAes)))

  }
}
