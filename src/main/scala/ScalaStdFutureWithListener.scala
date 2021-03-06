package com.taisukeoe

import scala.concurrent.{Promise, Future}
import scala.concurrent.ExecutionContext.Implicits.global

object ScalaStdFutureWithListener extends App{

  def onClick(button:Button): Future[Button] = {
    val p = Promise[Button]()
    val f = p.future
    button.setOnClickListener(new OnClickListener {
      override def onClick(b: Button): Unit = p.success(b)
    })
    f
  }

  val clickFuture:Future[Button] = onClick(Button)

  //clickFuture succeeds
  Button.click()

  /*
     clickFuture failed since Promise cannot be written twice, as follows.

     [error] (run-main-2) java.lang.IllegalStateException: Promise already completed.
     java.lang.IllegalStateException: Promise already completed.
        at scala.concurrent.Promise$class.complete(Promise.scala:55)
        at scala.concurrent.impl.Promise$DefaultPromise.complete(Promise.scala:153)
        ...
   */
  Button.click()
}
