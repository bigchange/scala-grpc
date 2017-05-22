package com.bigchange.grpc.impl

import com.bgfurfeature.hello.rpc.hello.{GreeterGrpc, HelloReply, HelloRequest}
import com.bigchange.model.TFIDF

import scala.concurrent.Future

/**
  * Created by Jerry on 2017/5/21.
  */
class GreeterServerImpl  extends  GreeterGrpc.Greeter { self =>

  override def sayHello(request: HelloRequest): Future[HelloReply] = {

    val txt = request.name
    println("got msg:" + txt)
    TFIDF.getInstance.cosSimilarity(txt)
    val reply = HelloReply.apply("hello, welcome using scalaPb " + txt)
    Future.successful(reply)
  }

  override def sayHelloAgain(request: HelloRequest): Future[HelloReply] = {
    println("got msg:" + request.name)
    val reply = HelloReply.apply("hello again, welcome using scalaPb " + request.name)
    Future.successful(reply)
  }

}
