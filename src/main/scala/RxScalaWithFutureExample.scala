package com.taisukeoe

import rx.lang.scala.schedulers.ExecutionContextScheduler
import rx.lang.scala.{Observable, Subscription}

import scala.concurrent.ExecutionContext.Implicits.global

object RxScalaWithFutureExample extends App {

  def onClick(button: Button): Observable[Button] =
    Observable.create[Button] { obs =>
      button.setOnClickListener(new LoggingOnClickListener {
        override def onClick(b: Button): Unit = {
          super.onClick(b)
          obs.onNext(b)
        }
      })
      new Subscription {}
    }

  import com.taisukeoe.ScalaStdFutureExample._

  val dataObservable: Observable[Array[Byte]] = for {
    _ <- onClick(Button)
    json <- Observable.from(profileJson("https://facebook.com/xxx")).subscribeOn(ExecutionContextScheduler(global)).doOnError(t => println(t.getMessage))
    imgUrl <- Observable.from(parse(json))
    data <- Observable.from(profileImg(imgUrl))
  } yield data

  dataObservable.foreach(println(_))

  Button.click()
  Thread.sleep(2000)
  Button.click()
}
