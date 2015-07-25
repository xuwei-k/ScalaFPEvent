package com.taisukeoe

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scalaz._

object ScalazContTExample extends App {
  type Callback[T] = ContT[Future, Unit, T]

  object Callback {
    def apply[T](f: (T => Future[Unit]) => Future[Unit]): Callback[T] = ContT.apply[Future, Unit, T](f)
  }

  def onClickCont(button: Button): Callback[Button] =
    Callback { f =>
      button.setOnClickListener(new LoggingOnClickListener {
        override def onClick(b: Button): Unit = {
          super.onClick(b)
          f(b)
        }
      })
      Future.successful(Unit)
    }

  import ScalaStdFutureExample._

  def profileImgCont(imgUrl: String): Callback[Array[Byte]] =
    Callback(profileImgFuture(imgUrl).flatMap(_))

  def profileJsonCont(url: String): Callback[String] =
    Callback(profileJsonFuture(url).flatMap(_))

  def parseCont(json: String): Callback[String] =
    Callback(parseFuture(json).flatMap(_))

  def recoverCont[T](failedCont: Callback[T], recover: => Future[T]): Callback[T] = Callback {
    f => failedCont.run(f).recoverWith {
      case t => t.printStackTrace()
        recover.flatMap(f)
    }
  }

  val dataCont = for {
    b <- onClickCont(Button)
    json <- recoverCont(profileJsonCont("https://facebook.com/xxx"), profileJsonFuture("https://twitter.com/xxx"))
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
