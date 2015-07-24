package com.taisukeoe

import scala.concurrent.Future
import scalaz._

object ScalazConstTExample extends App {
  def onClick(button: Button): ContT[Future, Unit, Button] =
    ContT[Future, Unit, Button] { f =>
      Future.successful(button.setOnClickListener(new OnClickListener {
        override def onClick(b: Button): Unit = f(b)
      }))
    }

  def profileImg(imgUrl: String): ContT[Future, Unit, Array[Byte]] =
    ContT { f =>
      Future.successful(SNSClient.getImageAsync(imgUrl, new SimpleCallback[Array[Byte], Exception] {
        override def onSuccess(imgData: Array[Byte]): Unit = f(imgData)

        override def onFailure(e: Exception): Unit = Future.failed(e)
      }))
    }

  def profileJson(url: String): ContT[Future, Unit, String] =
    ContT {
      f =>
        Future.successful(SNSClient.getProfileAsync(url, new SimpleCallback[String, Exception] {
          override def onSuccess(json: String): Unit = f(json)
          override def onFailure(e: Exception): Unit = Future.failed(e)
        }))
    }

  def parse(json: String):ContT[Future, Unit, String] =
    ContT {
      f =>
        Future.successful(SNSJSONParser.extractProfileUrlAsync(json, new SimpleCallback[String, Exception] {
          override def onSuccess(url: String): Unit = f(url)
          override def onFailure(e: Exception): Unit = Future.failed(e)
        }))
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
