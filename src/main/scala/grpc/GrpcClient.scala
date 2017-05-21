package grpc

import grpc.client.GreeterClient
import grpc.server.GreeterServer

/**
  * Created by Jerry on 2017/5/21.
  */
object GrpcClient {

  def main(args: Array[String]): Unit = {

    val port = 30399
    val host = "localhost"
    val client = GreeterClient.apply(host, port)
    client.greeter("i am jerry! ")

  }
}
