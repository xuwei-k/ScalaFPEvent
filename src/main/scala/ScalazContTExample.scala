package com.taisukeoe

import scala.concurrent.{Promise, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scalaz._

object ScalazContTExample extends App {
  def onClick(button: Button): ContT[Future, Unit, Button] =
    ContT[Future, Unit, Button] { f =>
      Future.successful(button.setOnClickListener(new LoggingOnClickListener {
        override def onClick(b: Button): Unit = {
          super.onClick(b)
          f(b)
        }
      }))
    }

  def profileImg(imgUrl: String): ContT[Future, Unit, Array[Byte]] =
    ContT { f =>
      val p = Promise[Array[Byte]]()
      val future = p.future
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
      future.flatMap(f)
    }

  def profileJson(url: String): ContT[Future, Unit, String] =
    ContT {
      f =>
        val p = Promise[String]()
        val future = p.future
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
        future.flatMap(f)
    }

  def parse(json: String):ContT[Future, Unit, String] =
    ContT {
      f =>
        val p = Promise[String]()
        val future = p.future
        SNSJSONParser.extractProfileUrlAsync(json, new LoggingSimpleCallback[String, Exception] {
          override def onSuccess(url: String): Unit = {
            super.onSuccess(url)
            p.success(url)
          }
          override def onFailure(e: Exception): Unit = {
            super.onFailure(e)
            p.failure(e)
          }
        })
        future.flatMap(f)
    }


  ContT
  val dataCont = for {
    b <- onClick(Button)
    json <- profileJson("https://facebook.com/xxx")
    imgUrl <- parse(json)
    data <- profileImg(imgUrl)
  } yield data

  dataCont.run{ba =>
    println(ba)
    Future.successful(Unit)
  }

  Button.click()
  Thread.sleep(2000)
  Button.click()
}
