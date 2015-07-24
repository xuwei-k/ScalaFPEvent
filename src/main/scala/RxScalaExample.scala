package com.taisukeoe

import rx.lang.scala.schedulers.ExecutionContextScheduler
import rx.lang.scala.{Observable, Subscription}

import scala.concurrent.ExecutionContext.Implicits.global

object RxScalaExample extends App {
  def onClick(button: Button): Observable[Button] =
    Observable { asSubscriber =>
      button.setOnClickListener(new LoggingOnClickListener {
        override def onClick(b: Button): Unit = {
          super.onClick(b)
          asSubscriber.onNext(b)
        }
      })
    }

  def profileJson(url: String): Observable[String] =
    Observable { asSubscriber =>
      SNSClient.getProfileAsync(url, new LoggingSimpleCallback[String, Exception] {
        override def onSuccess(t: String): Unit = {
          super.onSuccess(t)
          asSubscriber.onNext(t)
          asSubscriber.onCompleted()
        }

        override def onFailure(e: Exception): Unit = {
          super.onFailure(e)
          asSubscriber.onError(e)
          asSubscriber.onCompleted()
        }
      })
    }

  def parse(json: String): Observable[String] =
    Observable { asSubscriber =>
      SNSJSONParser.extractProfileUrlAsync(json, new LoggingSimpleCallback[String, Exception] {
        override def onSuccess(t: String): Unit = {
          super.onSuccess(t)
          asSubscriber.onNext(t)
          asSubscriber.onCompleted()
        }

        override def onFailure(e: Exception): Unit = {
          super.onFailure(e)
          asSubscriber.onError(e)
          asSubscriber.onCompleted()
        }
      })
    }

  def profileImg(imgUrl: String): Observable[Array[Byte]] =
    Observable { asSubscriber =>
      SNSClient.getImageAsync(imgUrl, new LoggingSimpleCallback[Array[Byte], Exception] {
        override def onSuccess(t: Array[Byte]): Unit = {
          super.onSuccess(t)
          asSubscriber.onNext(t)
          asSubscriber.onCompleted()
        }

        override def onFailure(e: Exception): Unit = {
          super.onFailure(e)
          asSubscriber.onError(e)
          asSubscriber.onCompleted()
        }
      })
    }


  val dataObservable: Observable[Array[Byte]] = for {
    _ <- onClick(Button)
    json <- profileJson("https://facebook.com/xxx").onErrorResumeNext { t =>
      t.printStackTrace()
      profileJson("https://twitter.com/xxx")
    }
    imgUrl <- parse(json)
    data <- profileImg(imgUrl)
  } yield data

  dataObservable.foreach(println(_))

  Button.click()
  Thread.sleep(2000)
  Button.click()
}
