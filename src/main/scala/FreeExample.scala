package com.taisukeoe

import scalaz.concurrent.Task
import scalaz._
import scalaz.Free.liftFC

sealed abstract class Program[A] extends Product with Serializable

final case class OnClick(button: Button) extends Program[Button]

final case class ProfileImage(imageUrl: String) extends Program[Array[Byte]]

final case class ProfileJson(url: String) extends Program[String]

final case class ParseJson(json: String) extends Program[String]

object FreeExample extends App {

  // url はここの引数で取るべきなのかはよくわからなかった
  def program(url: String) = for {
    _ <- liftFC(OnClick(Button))
    json <- liftFC(ProfileJson(url))
    imgUrl <- liftFC(ParseJson(json))
    data <- liftFC(ProfileImage(imgUrl))
  } yield data


  import ScalazTaskExample._

  val interpreter1: Program ~> Task =
    new (Program ~> Task) {
      override def apply[A](fa: Program[A]) = fa match {
        case OnClick(button) => onClick(button)

        case ProfileImage(imageUrl) => profileImg(imageUrl)

        case ProfileJson(url) => profileJson(url)

        case ParseJson(json) => parse(json)
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

  def getTaskFrom(interpreter: Program ~> Task): Task[Array[Byte]] =
    for {
      json <-
      Free.runFC(
        for {
          _ <- liftFC(OnClick(Button))
          json <- liftFC(ProfileJson("https://facebook.com/xxx"))
        } yield json
      )(interpreter).handleWith { case t =>
        t.printStackTrace()
        profileJson("https://twitter.com/xxx")
      }
      data <-
      Free.runFC(
        for {
          imgUrl <- liftFC(ParseJson(json))
          dt <- liftFC(ProfileImage(imgUrl))
        } yield dt
      )(interpreter)
    } yield data

  getTaskFrom(interpreter1).runAsync {
    case \/-(data) => println(data)
    case -\/(e) => e.printStackTrace()
  }

  Button.click()
  Thread.sleep(2000)
  Button.click()
}
