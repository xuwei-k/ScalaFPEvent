package com.taisukeoe

import org.slf4j.LoggerFactory

trait OnClickListener{
  def onClick(b:Button):Unit
}
trait LoggingOnClickListener extends OnClickListener{
  lazy val logger = LoggerFactory.getLogger(classOf[LoggingOnClickListener])
  override def onClick(b: Button): Unit = {
    logger.debug(s"onClick ${Thread.currentThread().getName}")
  }
}
object Button extends Button
trait Button{
  protected var listener:OnClickListener = _
  def setOnClickListener(l: OnClickListener): Unit = listener = l

  //Assuming click() will be called by process/user.
  def click():Unit = if(null!=listener) listener.onClick(this)
}
