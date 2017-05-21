package com.bigchange.grpc

import com.bigchange.grpc.client.GreeterClient

/**
  * Created by Jerry on 2017/5/21.
  */
object GrpcMainClient {

  def main(args: Array[String]): Unit = {

    val port = 50051
    val host = "127.0.0.1"
    val client = GreeterClient.apply(host, port)
    client.greeter("i am jerry! ")
    client.greeterAsync("java高级")

  }
}
