seq(githubPagesMavenPublishSettings: _*)

sbtPlugin := true

name := "ostrich-dist"

organization := "org.doxla"

version := "0.3"

githubPagesCheckoutDir := Path.userHome / "proj" / "oxlade39.github.com" / "ostrich-dist-plugin" / "maven"

publishMavenStyle := true

libraryDependencies += "org.scalatest" %% "scalatest" % "1.7.1" % "test"

scalacOptions ++= Seq("-deprecation", "-unchecked")