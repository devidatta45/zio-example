val Http4sVersion = "0.23.12"
val CirceVersion = "0.14.1"
val LogbackVersion = "1.2.6"
val ZIOVersion = "1.0.15"
val ZIOInterop = "3.2.9.1"
val PureConfigVersion = "0.17.1"
val ScalaTestVersion = "3.2.11"
val ScalaCheckVersion = "1.15.4"

lazy val root = (project in file("."))
  .settings(
    organization := "com.ddm",
    name := "ddm-assignment",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.6",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % ZIOVersion,
      "dev.zio" %% "zio-interop-cats" % ZIOInterop,
      "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "com.github.pureconfig" %% "pureconfig" % PureConfigVersion,
      "dev.zio" %% "zio-test" % ZIOVersion % "test",
      "dev.zio" %% "zio-test-sbt" % ZIOVersion % "test",
      "ch.qos.logback" % "logback-classic" % LogbackVersion,
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.13.0" cross CrossVersion.full),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
    testFrameworks += new TestFramework("munit.Framework")
  )
