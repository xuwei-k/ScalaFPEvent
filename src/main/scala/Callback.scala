package com.taisukeoe

import org.slf4j.{LoggerFactory, Logger}

trait SimpleCallback[-T, -E <: Throwable] {
  def onSuccess(t: T): Unit

  def onFailure(e: E): Unit
}
trait LoggingSimpleCallback[-T,-E <:Throwable] extends SimpleCallback[T,E]{
  lazy val logger = LoggerFactory.getLogger(classOf[LoggingSimpleCallback[T,E]])
  override def onSuccess(t: T): Unit = {
    logger.debug(s"onSuccess ${Thread.currentThread().getName}")
  }

  override def onFailure(e: E): Unit = {
    logger.debug(s"onSuccess ${Thread.currentThread().getName}")
  }
}

trait ComplicatedCallback[A, B, C, D] {
  def onStateA(a: A)

  def onStateB(b: B)

  def onStateC(c: C)

  def onStateD(d: D)
}

object SNSClient extends SNSClient

trait SNSClient {
  lazy val logger = LoggerFactory.getLogger(classOf[SNSClient])
  def getProfileAsync(url: String, callback: SimpleCallback[String, Exception]): Unit = {
    logger.debug(s"getProfileAsync ${Thread.currentThread().getName}")
    //Simulate GET over the network
    Thread.sleep(200)
    callback.onSuccess( """{"profile_url":"http://example.com/"}""")
//    callback.onFailure(new RuntimeException("timeout"))
  }

  def getImageAsync(url: String, callback: SimpleCallback[Array[Byte], Exception]): Unit = {
    logger.debug(s"getImageAsync ${Thread.currentThread().getName}")
    Thread.sleep(1000)
    callback.onSuccess(Array(16.toByte))
  }
}

object SNSJSONParser extends SNSJSONParser

trait SNSJSONParser {
  lazy val logger = LoggerFactory.getLogger(classOf[SNSJSONParser])
  def extractProfileUrlAsync(content: String, callback: SimpleCallback[String, Exception]): Unit = {
    logger.debug(s"extractProfileUrlAsync ${Thread.currentThread().getName}")
    //Simulate to parse large JSON
    Thread.sleep(100)
    callback.onSuccess("http://example.com/")
  }
}