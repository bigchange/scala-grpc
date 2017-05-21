package com.bigchange.grpc.server

import com.bgfurfeature.hello.rpc.hello.GreeterGrpc
import com.bigchange.grpc.impl.GreeterServerImpl
import com.bigchange.model.TFIDF
import io.grpc.{Server, ServerBuilder}

import scala.concurrent.ExecutionContext

/**
  * Created by Jerry on 2017/5/21.
  */

class GreeterServer(port: Int, executionContext: ExecutionContext) { self =>

  private var server: Server = null

  private val service:GreeterServerImpl = new GreeterServerImpl

  def start = {

    server  = ServerBuilder.forPort(port)
      .addService(GreeterGrpc.bindService(service , executionContext)).build().start()

    println("server listen port:" + port)

    sys.addShutdownHook {
      println("shutdown grpc since jvm is shutting down")
      this.stop
      println("server stop")
    }
  }

  private def stop = {
    if (server != null ) {
      server.shutdown()
    }
  }

  def blockUntilShutdown = {
    if (server != null) {
      server.awaitTermination()
    }
  }

}
object GreeterServer {

  private [this] var gserver :GreeterServer = null

  def apply(port: Int, executionContext: ExecutionContext): GreeterServer = {
    if (gserver == null) {
      new GreeterServer(port, executionContext)
    } else {
      gserver
    }
  }

  def getInstance =
    if (gserver != null)
      gserver
    else {
      println("pls init server first")
      sys.exit(-1)
    }
}

