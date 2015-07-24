package com.taisukeoe

import scalaz.concurrent.Task
import scalaz.{-\/, \/-}

object ScalazTaskExample extends App {

  def onClick(button: Button): Task[Button] =
    Task.async[Button] { f =>
      button.setOnClickListener(new LoggingOnClickListener {
        override def onClick(b: Button): Unit = {
          super.onClick(b)
          f(\/-(b))
        }
      })
    }

  def profileImg(imgUrl: String): Task[Array[Byte]] =
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

  def profileJson(url: String): Task[String] =
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

  def parse(json: String): Task[String] =
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
    _ <- onClick(Button)
    json <- Task.fork(profileJson("https://facebook.com/xxx"))
    imgUrl <- Task.fork(parse(json))
    data <- Task.fork(profileImg(imgUrl))
  } yield data

  dataTask.runAsync {
    case \/-(data) => println(data)
    case -\/(e) => e.printStackTrace()
  }

  Button.click()
  Thread.sleep(2000)
  Button.click()
}
