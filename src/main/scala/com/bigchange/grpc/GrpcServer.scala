package com.bigchange.grpc

import com.bigchange.grpc.server.GreeterServer
import grpc.server.GreeterServer

/**
  * Created by Jerry on 2017/5/21.
  */
object GrpcServer {

  def main(args: Array[String]): Unit = {

    val port = 30399
    val server = GreeterServer.apply(port)
    server.start
    server.blockUntilShutdown
  }
}
