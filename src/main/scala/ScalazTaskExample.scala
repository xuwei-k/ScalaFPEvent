package com.taisukeoe

import scalaz.concurrent.Task
import scalaz._

object ScalazTaskExample extends App {

  type Action[A, B] = Kleisli[Task, A, B]

  def Action[A, B](callback: (A, (Throwable \/ B) => Unit) => Unit): Action[A, B] =
    Kleisli[Task, A, B]{ a =>
      Task.async[B] { callback.curried(a) }
    }

  def onClick(button: Button): Task[Button] =
    Task.async[Button] { f =>
      button.setOnClickListener(new OnClickListener {
        override def onClick(b: Button): Unit = f(\/-(b))
      })
    }

  val profileImg: Action[String, Array[Byte]] =
    Action{ (imgUrl, f) =>
      SNSClient.getImageAsync(imgUrl, new SimpleCallback[Array[Byte], Exception] {
        override def onSuccess(imgData: Array[Byte]): Unit = f(\/-(imgData))

        override def onFailure(e: Exception): Unit = f(-\/(e))
      })
    }

  val profileJson: Action[String, String] =
    Action { (url, f) =>
      SNSClient.getProfileAsync(url, new SimpleCallback[String, Exception] {
        override def onSuccess(json: String): Unit = f(\/-(json))

        override def onFailure(e: Exception): Unit = f(-\/(e))
      })
    }

  val parse: Action[String, String] =
    Action { (json, f) =>
      SNSJSONParser.extractProfileUrlAsync(json, new SimpleCallback[String, Exception] {
        override def onSuccess(json: String): Unit = f(\/-(json))

        override def onFailure(e: Exception): Unit = f(-\/(e))
      })
    }


  val dataTask: Task[Array[Byte]] = for{
    _ <- onClick(Button)
    data <- (profileJson >=> parse >=> profileImg).run("https://facebook.com/xxx")
  } yield data

  dataTask.runAsync {
    case \/-(data) => println(data)
    case -\/(e) => e.printStackTrace()
  }

  Button.click()
  Thread.sleep(2000)
  Button.click()
}
