name := "scala-sbt"

version := "1.0"

// scala 2.11+
scalaVersion := "2.11.0"
// https://mvnrepository.com/artifact/org.apache.spark/spark-core_2.10
libraryDependencies += "org.apache.spark" % "spark-core_2.10" % "1.6.0"

libraryDependencies += "org.apache.spark" % "spark-mllib_2.10" % "1.6.0"

// https://mvnrepository.com/artifact/com.hankcs/hanlp
libraryDependencies += "com.hankcs" % "hanlp" % "portable-1.3.3"

// https://mvnrepository.com/artifact/io.vertx/vertx-core
libraryDependencies += "io.vertx" % "vertx-core" % "3.4.1"

// proto3 not proto2
libraryDependencies += "io.grpc" % "grpc-all" % "1.3.0"

libraryDependencies += "com.google.protobuf" % "protobuf-java" % "3.2.0"

// generate scala code
PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)

libraryDependencies += "com.trueaccord.scalapb" %% "scalapb-runtime" % com.trueaccord.scalapb
  .compiler.Version.scalapbVersion % "protobuf" exclude ("io", "netty")

libraryDependencies ++= Seq("io.grpc" % "grpc-netty" % com.trueaccord.scalapb.compiler.Version.grpcJavaVersion,
  "com.trueaccord.scalapb" %% "scalapb-runtime-grpc"
    % com.trueaccord.scalapb.compiler.Version.scalapbVersion exclude ("io", "netty")
)

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
        