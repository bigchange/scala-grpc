name := "scala-sbt"

version := "1.0"

scalaVersion := "2.11.0"

libraryDependencies += "org.apache.spark" % "spark-core_2.11" % "2.0.2"  exclude ("io", "netty")

libraryDependencies += "org.apache.spark" % "spark-mllib_2.11" % "2.0.2" exclude ("io", "netty")

// https://mvnrepository.com/artifact/com.hankcs/hanlp
libraryDependencies += "com.hankcs" % "hanlp" % "portable-1.3.3" exclude ("io", "netty")

// https://mvnrepository.com/artifact/io.vertx/vertx-core
libraryDependencies += "io.vertx" % "vertx-core" % "3.4.1" exclude ("io", "netty")

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)

// (optional) If you need scalapb/scalapb.proto or anything from
// google/protobuf/*.proto
libraryDependencies += "com.trueaccord.scalapb" %% "scalapb-runtime" % com.trueaccord.scalapb
  .compiler.Version.scalapbVersion % "protobuf" exclude ("io", "netty")

libraryDependencies ++= Seq(
  "io.grpc" % "grpc-netty" % com.trueaccord.scalapb.compiler.Version.grpcJavaVersion,
  "com.trueaccord.scalapb" %% "scalapb-runtime-grpc" % com.trueaccord.scalapb.compiler.Version
    .scalapbVersion exclude ("io", "netty")
)

assemblyMergeStrategy in assembly := {
  case PathList("javax", "servlet", xs @ _*) => MergeStrategy.last
  case PathList("javax", "activation", xs @ _*) => MergeStrategy.last
  case PathList("javax", "el", xs @ _*) => MergeStrategy.last
  case PathList("javax", "inject", xs @ _*) => MergeStrategy.last
  case PathList("org", "apache", xs @ _*) => MergeStrategy.last
  case PathList("org", "aopalliance", xs @ _*) => MergeStrategy.last
  case PathList("org", "glassfish.", xs @ _*) => MergeStrategy.last
  case PathList("com", "esotericsoftware", xs @ _*) => MergeStrategy.last
  case PathList("com", "codahale", xs @ _*) => MergeStrategy.last
  case PathList("com", "yammer", xs @ _*) => MergeStrategy.last
  case PathList("io", "netty", xs @ _*) => MergeStrategy.last
  case "about.html" => MergeStrategy.rename
  case "META-INF/ECLIPSEF.RSA" => MergeStrategy.last
  case "META-INF/mailcap" => MergeStrategy.last
  case "META-INF/mimetypes.default" => MergeStrategy.last
  case "plugin.properties" => MergeStrategy.last
  case "log4j.properties" => MergeStrategy.last
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
        