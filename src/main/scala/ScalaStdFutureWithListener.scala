package com.taisukeoe

import scala.concurrent.{Future, Promise}

object ScalaStdFutureWithListener extends App{

  def onClickFuture(button:Button): Future[Button] = {
    val p = Promise[Button]()
    val f = p.future
    button.setOnClickListener(new LoggingOnClickListener {
      override def onClick(b: Button): Unit = {
        super.onClick(b)
        p.success(b)
      }
    })
    f
  }

  val clickFuture:Future[Button] = onClickFuture(Button)

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
