package com.taisukeoe

import scalaz.concurrent.Task
import scalaz._
import scalaz.Free.liftFC
import scalaz.Free.FreeC
import scalaz.syntax.id._

sealed abstract class Program[A] extends Product with Serializable

final case class OnClick(button: Button) extends Program[Button]

final case class ProfileImage(imageUrl: String) extends Program[Array[Byte]]

final case class ProfileJson(url: String) extends Program[String]

final case class ProfileJsonE(url: String) extends Program[Throwable \/ String]

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
        case OnClick(button) => onClickTask(button)

        case ProfileImage(imageUrl) => profileImgTask(imageUrl)

        case ProfileJson(url) => profileJsonTask(url)

        case ParseJson(json) => parseTask(json)
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

  def getTaskFrom: FreeC[Program, Array[Byte]] =
    (for {
      _    <- liftFC(OnClick(Button))
      fb   <- liftFC(ProfileJsonE("https://facebook.com/xxx"))
      json <- fb.fold(
        t => liftFC(ProfileJson("https://twitter.com/xxx").unsafeTap(_ => t.printStackTrace())),
        v => Free.point[({type l[a] = Coyoneda[Program, a]})#l, String](v)
      )
      url  <- liftFC(ParseJson(json))
      data <- liftFC(ProfileImage(url))
    } yield data)

  Free.runFC(getTaskFrom)(interpreter1).runAsync {
    case \/-(data) => println(data)
    case -\/(e) => e.printStackTrace()
  }

  Button.click()
  Thread.sleep(2000)
  Button.click()
}
