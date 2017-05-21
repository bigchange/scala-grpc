package com.bigchange.grpc.server

import com.bgfurfeature.hello.rpc.hello.GreeterGrpc
import com.bigchange.grpc.impl.GreeterServerImpl
import io.grpc.{Server, ServerBuilder}
import io.vertx.core.logging.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext

/**
  * Created by Jerry on 2017/5/21.
  */

class GreeterServer(port: Int) { self =>

  private var server: Server = null

  def start = {

    server  = ServerBuilder.forPort(port)
      .addService(GreeterGrpc.bindService(new GreeterServerImpl, executionContext =
        ExecutionContext.global))
      .build().start()

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

  def apply(port: Int): GreeterServer = {
    if (gserver == null) {
      new GreeterServer(port)
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

