package com.taisukeoe

import scalaz.concurrent.Task
import scalaz._
import scalaz.Free.liftFC

sealed abstract class Program[A] extends Product with Serializable
final case class OnClick(button: Button) extends Program[Button]
final case class ProfileImage(imageUrl: String) extends Program[Array[Byte]]
final case class ProfileJson(url: String) extends Program[String]
final case class ParseJson(json: String) extends Program[String]

object FreeExample extends App{

  // url はここの引数で取るべきなのかはよくわからなかった
  def program(url: String) = for{
     _ <- liftFC(OnClick(Button))
    json <- liftFC(ProfileJson(url))
    imgUrl <- liftFC(ParseJson(json))
    data <- liftFC(ProfileImage(imgUrl))
  } yield data

  val interpreter1: Program ~> Task =
    new (Program ~> Task) {
      override def apply[A](fa: Program[A]) = fa match {
        case OnClick(button) =>
          Task.async[Button] { f =>
            button.setOnClickListener(new LoggingOnClickListener {
              override def onClick(b: Button): Unit ={
                super.onClick(b)
                f(\/-(b))
              }
            })
          }
        case ProfileImage(imageUrl) =>
          Task.async[Array[Byte]] { f =>
            SNSClient.getImageAsync(imageUrl, new LoggingSimpleCallback[Array[Byte], Exception] {
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
        case ProfileJson(url) =>
          Task.async[String] { f =>
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
        case ParseJson(json) =>
          Task.async[String] { f =>
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
      }
    }

  val interpreter2: Program ~> Task =
    new (Program ~> Task) {
      override def apply[A](fa: Program[A]) = fa match {
        case _: OnClick =>
          val dummyButton = new Button {
            override def setOnClickListener(l: OnClickListener): Unit = ()
          }
          Task.now(dummyButton)
        case _: ProfileImage =>
          Task.now("テスト用のダミーのbyte列".getBytes("UTF-8"))
        case _: ProfileJson =>
          Task.now("テスト用のダミーのjson")
        case _: ParseJson =>
          Task.now("テスト用のダミーのjson")
      }
    }

  val task1: Task[Array[Byte]] =
    Free.runFC(program("https://facebook.com/xxx"))(interpreter1)

  task1.runAsync {
    case \/-(data) => println(data)
    case -\/(e) => e.printStackTrace()
  }

  Button.click()
  Thread.sleep(2000)
  Button.click()
}
