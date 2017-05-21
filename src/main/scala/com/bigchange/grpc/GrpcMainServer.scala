package com.bigchange.grpc

import com.bigchange.grpc.server.GreeterServer

import scala.concurrent.ExecutionContext

/**
  * Created by Jerry on 2017/5/21.
  */
object GrpcMainServer {

  def main(args: Array[String]): Unit = {

    val port = 50051
    val server = GreeterServer.apply(port, ExecutionContext.global)
    server.start
    server.blockUntilShutdown
  }
}
