name := "scala-sbt"

version := "1.0"

// scala 2.11+
scalaVersion := "2.11.8"

libraryDependencies += "org.apache.spark" % "spark-core_2.11" % "2.0.2"  exclude ("io", "netty")

libraryDependencies += "org.apache.spark" % "spark-mllib_2.11" % "2.0.2"  exclude ("io", "netty")

// https://mvnrepository.com/artifact/com.hankcs/hanlp
libraryDependencies += "com.hankcs" % "hanlp" % "portable-1.3.3" exclude ("io", "netty")

// https://mvnrepository.com/artifact/io.vertx/vertx-core
libraryDependencies += "io.vertx" % "vertx-core" % "3.4.1" exclude ("io", "netty")

// proto3 not proto2
// https://mvnrepository.com/artifact/io.grpc/grpc-stub
libraryDependencies += "io.grpc" % "grpc-stub" % "1.3.0"

// https://mvnrepository.com/artifact/io.grpc/grpc-stub
libraryDependencies += "io.grpc" % "grpc-core" % "1.3.0"

// https://mvnrepository.com/artifact/io.grpc/grpc-stub
libraryDependencies += "io.grpc" % "grpc-auth" % "1.3.0"

// https://mvnrepository.com/artifact/io.grpc/grpc-stub
libraryDependencies += "io.grpc" % "grpc-context" % "1.3.0"

// https://mvnrepository.com/artifact/io.grpc/grpc-stub
libraryDependencies += "io.grpc" % "grpc-okhttp" % "1.3.0"

// https://mvnrepository.com/artifact/io.grpc/grpc-stub
libraryDependencies += "io.grpc" % "grpc-protobuf-lite" % "1.3.0"

// https://mvnrepository.com/artifact/io.grpc/grpc-stub
libraryDependencies += "io.grpc" % "grpc-protobuf-nano" % "1.3.0"

// https://mvnrepository.com/artifact/io.grpc/grpc-stub
libraryDependencies += "io.grpc" % "grpc-protobuf" % "1.3.0"

// https://mvnrepository.com/artifact/io.grpc/grpc-netty
libraryDependencies += "io.grpc" % "grpc-netty" % "1.3.0"

// https://mvnrepository.com/artifact/io.netty/netty-all
libraryDependencies += "io.netty" % "netty-all" % "4.1.11.Final"

libraryDependencies += "com.google.protobuf" % "protobuf-java" % "3.2.0"

libraryDependencies += "com.trueaccord.scalapb" %% "scalapb-runtime" % com.trueaccord.scalapb.compiler.Version.scalapbVersion % "protobuf" exclude ("io", "netty")

libraryDependencies ++= Seq("com.trueaccord.scalapb" %% "scalapb-runtime-grpc" % com.trueaccord.scalapb.compiler.Version.scalapbVersion)

// generate scala code
PB.targets in Compile := Seq(scalapb.gen() -> (sourceManaged in Compile).value)

// protobuf version 3.2.0
PB.protocVersion := "-v320"

// By default we generate into target/src_managed. To customize:
// PB.targets in Compile := Seq(scalapb.gen() -> file("src/main/gencode"))

// Changing where to look for protos to compile (default src/main/protobuf):
// PB.protoSources in Compile := Seq(sourceDirectory.value / "src/main/protobuf")


assemblyMergeStrategy in assembly := {
  case PathList("javax", "servlet", xs @ _*) => MergeStrategy.last
  case PathList("javax", "activation", xs @ _*) => MergeStrategy.last
  case PathList("javax", "el", xs @ _*) => MergeStrategy.last
  case PathList("javax", "inject", xs @ _*) => MergeStrategy.last
  case PathList("javax", "xml", xs @ _*) => MergeStrategy.last
  case PathList("com", "fasterxml", xs @ _*) => MergeStrategy.last
  case PathList("org", "apache", xs @ _*) => MergeStrategy.last
  case PathList("org", "aopalliance", xs @ _*) => MergeStrategy.last
  case PathList("org", "glassfish.", xs @ _*) => MergeStrategy.last
  case PathList("com", "esotericsoftware", xs @ _*) => MergeStrategy.last
  case PathList("com", "codahale", xs @ _*) => MergeStrategy.last
  case PathList("com", "yammer", xs @ _*) => MergeStrategy.last
  case PathList("com", "google", xs @ _*) => MergeStrategy.last
  case PathList("io", "netty", xs @ _*) => MergeStrategy.last
  case "about.html" => MergeStrategy.rename
  case "META-INF/ECLIPSEF.RSA" => MergeStrategy.last
  case "META-INF/mailcap" => MergeStrategy.last
  case "META-INF/io.netty.versions.properties" => MergeStrategy.last
  case "META-INF/mimetypes.default" => MergeStrategy.last
  case "plugin.properties" => MergeStrategy.last
  case "log4j.properties" => MergeStrategy.last
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
        