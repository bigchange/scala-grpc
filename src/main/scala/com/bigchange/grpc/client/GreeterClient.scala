package com.bigchange.grpc.client

import java.util.concurrent.TimeUnit

import com.bgfurfeature.hello.rpc.hello.{GreeterGrpc, HelloRequest}
import io.grpc.ManagedChannelBuilder

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

/**
  * Created by Jerry on 2017/5/21.
  */
class GreeterClient (host: String, port: Int) { self =>

  private val channel = ManagedChannelBuilder
    .forAddress(host, port)
    .usePlaintext(true)
    .build()

  private val blockingStub = GreeterGrpc.blockingStub(channel)

  def shutdown(): Unit = {
    channel.shutdown.awaitTermination(5, TimeUnit.SECONDS)
  }

  // using blcoking stub call
  def greeter (msg:String) = {

    val request = HelloRequest.apply(msg)

    val response = Try {
      blockingStub.sayHello(request)
    }

    response match {
      case Success(s) =>
        println("reply is : " + s.message)
      case Failure(exception) =>
        this.shutdown()
        println("Gprc failed!! " + exception.getMessage)
    }
  }
  // Async call - 异步调用
  def greeterAsync (msg: String) = {
    implicit val executionContext = ExecutionContext.global
    val request = HelloRequest(msg)
    val stub = GreeterGrpc.stub(channel)
    val f = stub.sayHello(request)
    f onComplete { x =>
      x match {
        case Success(s) =>
          println("reply is :" + s.message)
        case Failure(e) =>
          println("Gprc failed!!" + e.getMessage)
      }
    }
  }

}
object GreeterClient {
  def apply(host: String, port: Int): GreeterClient = new GreeterClient(host, port)
}
