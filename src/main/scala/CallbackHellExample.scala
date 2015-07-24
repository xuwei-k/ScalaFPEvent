package com.taisukeoe

object CallbackHellExample extends App {

  //Callback HELL!!
  SNSClient.getProfileAsync("https://facebook.com/xxx", new SimpleCallback[String, Exception] {
    override def onSuccess(json: String): Unit =
      SNSJSONParser.extractProfileUrlAsync(json, new SimpleCallback[String, Exception] {
        override def onSuccess(profileUrl: String): Unit =
          SNSClient.getImageAsync(profileUrl, new SimpleCallback[Array[Byte], Exception] {
            override def onSuccess(t: Array[Byte]): Unit = ???

            override def onFailure(e: Exception): Unit = e.printStackTrace()
          })

        override def onFailure(e: Exception): Unit = e.printStackTrace()
      })

    override def onFailure(e: Exception): Unit = e.printStackTrace()
  })

}
