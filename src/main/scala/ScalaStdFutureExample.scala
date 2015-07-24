package com.taisukeoe

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

object ScalaStdFutureExample extends App {
  val dataFuture: Future[Array[Byte]] =
    for {
      json <- profileJson("https://facebook.com/xxx").recoverWith { case t =>
        t.printStackTrace()
        profileJson("https://twitter.com/xxx")
      }
      imgUrl <- parse(json)
      data <- profileImg(imgUrl)
    } yield data

  dataFuture.onComplete {
    case Success(ba) => println(ba)
    case Failure(t) => t.printStackTrace()
  }

  def profileImg(imgUrl: String): Future[Array[Byte]] = {
    val p = Promise[Array[Byte]]()
    val f = p.future
    SNSClient.getImageAsync(imgUrl, new LoggingSimpleCallback[Array[Byte], Exception] {
      override def onSuccess(imgData: Array[Byte]): Unit = {
        super.onSuccess(imgData)
        p.success(imgData)
      }

      override def onFailure(e: Exception): Unit = {
        super.onFailure(e)
        p.failure(e)
      }
    })
    f
  }

  def profileJson(url: String): Future[String] = {
    val p = Promise[String]()
    val f = p.future
    SNSClient.getProfileAsync(url, new LoggingSimpleCallback[String, Exception] {
      override def onSuccess(json: String): Unit = {
        super.onSuccess(json)
        p.success(json)
      }

      override def onFailure(e: Exception): Unit = {
        super.onFailure(e)
        p.failure(e)
      }
    })
    f
  }

  def parse(json: String): Future[String] = {
    val p = Promise[String]()
    val f = p.future
    SNSJSONParser.extractProfileUrlAsync(json, new LoggingSimpleCallback[String, Exception] {
      override def onSuccess(imgUrl: String): Unit = {
        super.onSuccess(imgUrl)
        p.success(imgUrl)
      }

      override def onFailure(e: Exception): Unit = {
        super.onFailure(e)
        p.failure(e)
      }
    })
    f
  }
}
