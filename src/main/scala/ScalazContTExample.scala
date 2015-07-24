package com.taisukeoe

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scalaz._

object ScalazContTExample extends App {
  def onClick(button: Button): ContT[Future, Unit, Button] =
    ContT { f =>
      button.setOnClickListener(new LoggingOnClickListener {
        override def onClick(b: Button): Unit = {
          super.onClick(b)
          f(b)
        }
      })
      Future.successful(Unit)
    }

  import ScalaStdFutureExample._

  def profileImgCont(imgUrl: String): ContT[Future, Unit, Array[Byte]] =
    ContT(profileImg(imgUrl).flatMap(_))

  def profileJsonCont(url: String): ContT[Future, Unit, String] =
    ContT(profileJson(url).flatMap(_))

  def parseCont(json: String): ContT[Future, Unit, String] =
    ContT(parse(json).flatMap(_))

  def recover[T](failedCont: ContT[Future, Unit, T], recover: => Future[T]): ContT[Future, Unit, T] = ContT {
    f => failedCont.run(f).recoverWith {
      case t => t.printStackTrace()
        recover.flatMap(f)
    }
  }

  val dataCont = for {
    b <- onClick(Button)
    json <- recover(profileJsonCont("https://facebook.com/xxx"), profileJson("https://twitter.com/xxx"))
    imgUrl <- parseCont(json)
    data <- profileImgCont(imgUrl)
  } yield data

  dataCont.run { ba =>
    println(ba)
    Future.successful(Unit)
  }

  Button.click()
  Thread.sleep(2000)
  Button.click()
}
