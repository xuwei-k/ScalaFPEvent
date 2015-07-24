package com.taisukeoe

trait OnClickListener{
  def onClick(b:Button):Unit
}
trait Button{
  def setOnClickListener(l:OnClickListener):Unit
}
object Button extends Button{
  private var listener:OnClickListener = _
  override def setOnClickListener(l: OnClickListener): Unit = listener = l

  //Assuming click() will be called by process/user.
  def click():Unit = if(null!=listener) listener.onClick(this)
}
