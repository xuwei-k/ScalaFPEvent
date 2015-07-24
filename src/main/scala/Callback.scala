package com.taisukeoe

trait SimpleCallback[-T, -E <: Throwable] {
  def onSuccess(t: T): Unit

  def onFailure(e: E): Unit
}

trait ComplicatedCallback[A, B, C, D] {
  def onStateA(a: A)

  def onStateB(b: B)

  def onStateC(c: C)

  def onStateD(d: D)
}

object SNSClient extends SNSClient

trait SNSClient {
  def getProfileAsync(url: String, callback: SimpleCallback[String, Exception]): Unit = {
    //Simulate GET over the network
    Thread.sleep(200)
//    callback.onSuccess( """{"profile_url":"http://example.com/"}""")
    callback.onFailure(new RuntimeException("timeout"))
  }

  def getImageAsync(url: String, callback: SimpleCallback[Array[Byte], Exception]): Unit = {
    Thread.sleep(1000)
    callback.onSuccess(Array(16.toByte))
  }
}

object SNSJSONParser extends SNSJSONParser

trait SNSJSONParser {
  def extractProfileUrlAsync(content: String, callback: SimpleCallback[String, Exception]): Unit = {
    //Simulate to parse large JSON
    Thread.sleep(100)
    callback.onSuccess("http://example.com/")
  }
}