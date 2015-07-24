package com.taisukeoe

import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global

trait SimpleCallback[-T, -E <: Throwable] {
  def onSuccess(t: T): Unit

  def onFailure(e: E): Unit
}

trait LoggingSimpleCallback[-T, -E <: Throwable] extends SimpleCallback[T, E] {
  lazy val logger = LoggerFactory.getLogger(classOf[LoggingSimpleCallback[T, E]])

  override def onSuccess(t: T): Unit = {
    logger.debug(s"onSuccess ${Thread.currentThread().getName}")
  }

  override def onFailure(e: E): Unit = {
    logger.debug(s"onFailure ${e.getMessage} ${Thread.currentThread().getName}")
  }
}

trait ComplicatedCallback[A, B, C, D] {
  def onStateA(a: A)

  def onStateB(b: B)

  def onStateC(c: C)

  def onStateD(d: D)
}

object SNSClient extends SNSClient

trait SNSClient extends AsyncHandler{
  def getProfileAsync(url: String, callback: SimpleCallback[String, Exception]): Unit = {
    pool.execute(new Runnable {
      override def run(): Unit = {
        logger.debug(s"getProfileAsync ${Thread.currentThread().getName}")
        //Simulate GET over the network
        Thread.sleep(200)
        if (url.contains("twitter"))
          callback.onSuccess( """{"profile_url":"http://example.com/"}""")
        else
          callback.onFailure(new RuntimeException("timeout"))
      }
    })
  }

  def getImageAsync(url: String, callback: SimpleCallback[Array[Byte], Exception]): Unit = {
    pool.execute(new Runnable {
      override def run(): Unit = {
        logger.debug(s"getImageAsync ${Thread.currentThread().getName}")
        Thread.sleep(1000)
        callback.onSuccess(Array(16.toByte))
      }
    })
  }
}

object SNSJSONParser extends SNSJSONParser

trait SNSJSONParser extends AsyncHandler {
  def extractProfileUrlAsync(content: String, callback: SimpleCallback[String, Exception]): Unit = {
    pool.execute(new Runnable {
      override def run(): Unit = {
        logger.debug(s"extractProfileUrlAsync ${Thread.currentThread().getName}")
        //Simulate to parse large JSON
        Thread.sleep(100)
        callback.onSuccess("http://example.com/")
      }
    })
  }
}

trait AsyncHandler {
  lazy val pool = global
  lazy val logger = LoggerFactory.getLogger(getClass)
}