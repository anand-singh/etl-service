val Http4sVersion = "0.21.28"
val CirceVersion = "0.14.1"
val Specs2Version = "4.12.4"
val LogbackVersion = "1.2.6"

lazy val root = (project in file("."))
  .settings(
    organization := "io.github.anand-singh",
    name := "etl-service",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.5",
    libraryDependencies ++= Seq(
      "org.http4s"      %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s"      %% "http4s-blaze-client" % Http4sVersion,
      "org.http4s"      %% "http4s-circe"        % Http4sVersion,
      "org.http4s"      %% "http4s-dsl"          % Http4sVersion,
      "io.circe"        %% "circe-generic"       % CirceVersion,
      "org.specs2"      %% "specs2-core"         % Specs2Version % "test",
      "ch.qos.logback"  %  "logback-classic"     % LogbackVersion
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.10.3"),
    addCompilerPlugin("com.olegpy"     %% "better-monadic-for" % "0.3.1")
  )

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-language:higherKinds",
  "-language:postfixOps",
  "-feature",
  "-Xfatal-warnings",
)

coverageExcludedPackages := "io.github.etl.*Main.*;io.github.etl.*Server.*"
