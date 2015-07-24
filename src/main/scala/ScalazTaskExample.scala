package com.taisukeoe

import scalaz.concurrent.Task
import scalaz.{-\/, \/-}

object ScalazTaskExample extends App {

  def onClick(button: Button): Task[Button] =
    Task.async[Button] { f =>
      button.setOnClickListener(new OnClickListener {
        override def onClick(b: Button): Unit = f(\/-(b))
      })
    }

  def profileImg(imgUrl: String): Task[Array[Byte]] =
    Task.async[Array[Byte]] {
      f =>
        SNSClient.getImageAsync(imgUrl, new SimpleCallback[Array[Byte], Exception] {
          override def onSuccess(imgData: Array[Byte]): Unit = f(\/-(imgData))

          override def onFailure(e: Exception): Unit = f(-\/(e))
        })
    }

  def profileJson(url: String): Task[String] =
    Task.async[String] {
      f =>
        SNSClient.getProfileAsync(url, new SimpleCallback[String, Exception] {
          override def onSuccess(json: String): Unit = f(\/-(json))

          override def onFailure(e: Exception): Unit = f(-\/(e))
        })
    }

  def parse(json: String): Task[String] =
    Task.async[String] {
      f =>
        SNSJSONParser.extractProfileUrlAsync(json, new SimpleCallback[String, Exception] {
          override def onSuccess(json: String): Unit = f(\/-(json))

          override def onFailure(e: Exception): Unit = f(-\/(e))
        })
    }


  val dataTask: Task[Array[Byte]] = for {
    _ <- onClick(Button)
    json <- profileJson("https://facebook.com/xxx")
    imgUrl <- parse(json)
    data <- profileImg(imgUrl)
  } yield data

  dataTask.runAsync {
    case \/-(data) => println(data)
    case -\/(e) => e.printStackTrace()
  }

  Button.click()
  Thread.sleep(2000)
  Button.click()
}
