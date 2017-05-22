package com.bigchange.grpc

import com.bigchange.grpc.client.GreeterClient

/**
  * Created by Jerry on 2017/5/21.
  */
object GrpcMainClient {

  def main(args: Array[String]): Unit = {

    val port = 50051
    val host = args(0)
    val client = GreeterClient.apply(host, port)
    client.greeter(args(1))
  }
}
