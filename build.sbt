sbtPlugin := true

name := "ostrich-dist"

organization := "org.doxla"

version := "0.1"

publishTo := Some(
  Resolver.file("Github Pages",
    Path.userHome / "proj" / "oxlade39.github.com" / "ostrich-dist-plugin" / "maven" asFile)(Patterns(true, Resolver.mavenStyleBasePattern)))

publishMavenStyle := true

libraryDependencies += "org.scalatest" %% "scalatest" % "1.7.1" % "test"

scalacOptions ++= Seq("-deprecation", "-unchecked")