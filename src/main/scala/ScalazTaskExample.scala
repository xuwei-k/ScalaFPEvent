package com.taisukeoe

import scalaz.concurrent.Task
import scalaz.{-\/, \/-}

object ScalazTaskExample extends App {

  def onClickTask(button: Button): Task[Button] =
    Task.async[Button] { f =>
      button.setOnClickListener(new LoggingOnClickListener {
        override def onClick(b: Button): Unit = {
          super.onClick(b)
          f(\/-(b))
        }
      })
    }

  def profileImgTask(imgUrl: String): Task[Array[Byte]] =
    Task.async[Array[Byte]] {
      f =>
        SNSClient.getImageAsync(imgUrl, new LoggingSimpleCallback[Array[Byte], Exception] {
          override def onSuccess(imgData: Array[Byte]): Unit = {
            super.onSuccess(imgData)
            f(\/-(imgData))
          }

          override def onFailure(e: Exception): Unit = {
            super.onFailure(e)
            f(-\/(e))
          }
        })
    }

  def profileJsonTask(url: String): Task[String] =
    Task.async[String] {
      f =>
        SNSClient.getProfileAsync(url, new LoggingSimpleCallback[String, Exception] {
          override def onSuccess(json: String): Unit = {
            super.onSuccess(json)
            f(\/-(json))
          }

          override def onFailure(e: Exception): Unit = {
            super.onFailure(e)
            f(-\/(e))
          }
        })
    }

  def parseTask(json: String): Task[String] =
    Task.async[String] {
      f =>
        SNSJSONParser.extractProfileUrlAsync(json, new LoggingSimpleCallback[String, Exception] {
          override def onSuccess(json: String): Unit = {
            super.onSuccess(json)
            f(\/-(json))
          }

          override def onFailure(e: Exception): Unit = {
            super.onFailure(e)
            f(-\/(e))
          }
        })
    }


  val dataTask: Task[Array[Byte]] = for {
    _ <- onClickTask(Button)
    json <- profileJsonTask("https://facebook.com/xxx").handleWith { case t =>
      t.printStackTrace()
      profileJsonTask("https://twitter.com/xxx")
    }
    imgUrl <- parseTask(json)
    data <- profileImgTask(imgUrl)
  } yield data

  dataTask.runAsync {
    case \/-(data) => println(data)
    case -\/(e) => e.printStackTrace()
  }

  Button.click()
  Thread.sleep(2000)
  Button.click()
}
