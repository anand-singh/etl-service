addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.5")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")

ThisBuild / libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
)
