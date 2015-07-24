package com.taisukeoe

import rx.lang.scala.{Observable, Subscription}

import scala.concurrent.ExecutionContext.Implicits.global

object RxScalaWithFutureExample extends App {
  def onClick(button: Button): Observable[Button] =
    Observable { asSubscriber =>
      button.setOnClickListener(new LoggingOnClickListener {
        override def onClick(b: Button): Unit = {
          super.onClick(b)
          asSubscriber.onNext(b)
        }
      })
    }

  import com.taisukeoe.ScalaStdFutureExample._

  val dataObservable: Observable[Array[Byte]] = for {
    _ <- onClick(Button)
    json <- Observable.from(profileJson("https://facebook.com/xxx").recoverWith { case t =>
      t.printStackTrace()
      profileJson("https://twitter.com/xxx")
    })
    imgUrl <- Observable.from(parse(json))
    data <- Observable.from(profileImg(imgUrl))
  } yield data

  dataObservable.foreach(println(_))

  Button.click()
  Thread.sleep(2000)
  Button.click()
}
