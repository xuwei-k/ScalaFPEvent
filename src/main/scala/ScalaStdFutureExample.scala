package com.taisukeoe

import scala.concurrent.{Promise, Future}
import scala.concurrent.ExecutionContext.Implicits.global

object ScalaStdFutureExample extends App {

  val dataFuture: Future[Array[Byte]] =
    for {
      json <- profileJson("https://facebook.com/xxx")
      imgUrl <- parse(json)
      data <- profileImg(imgUrl)
    } yield data

  def profileImg(imgUrl: String): Future[Array[Byte]] = {
    val p = Promise[Array[Byte]]()
    val f = p.future
    SNSClient.getImageAsync(imgUrl, new SimpleCallback[Array[Byte], Exception] {
      override def onSuccess(imgData: Array[Byte]): Unit = p.success(imgData)
      override def onFailure(e: Exception): Unit = p.failure(e)
    })
    f
  }

  def profileJson(url: String): Future[String] = {
    val p = Promise[String]()
    val f = p.future
    SNSClient.getProfileAsync(url, new SimpleCallback[String, Exception] {
      override def onSuccess(json: String): Unit = p.success(json)
      override def onFailure(e: Exception): Unit = p.failure(e)
    })
    f
  }

  def parse(json: String): Future[String] = {
    val p = Promise[String]()
    val f = p.future
    SNSJSONParser.extractProfileUrlAsync(json, new SimpleCallback[String, Exception] {
      override def onSuccess(imgUrl: String): Unit = p.success(imgUrl)
      override def onFailure(e: Exception): Unit = p.failure(e)
    })
    f
  }
}
