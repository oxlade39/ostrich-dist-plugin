# Ostrich Dist Plugin

An [sbt](https://github.com/harrah/xsbt) plugin for creating binary distributions which use [Ostrich](https://github.com/twitter/ostrich)

[![Build Status](https://secure.travis-ci.org/oxlade39/ostrich-dist-plugin.png?branch=master)](http://travis-ci.org/oxlade39/ostrich-dist-plugin)

### What it creates

a `.zip` file containing the following:

    /lib
        your project's runtime dependencies
    /config
        your project's Ostrich configuration
    /webapp
        if your project has a webapp directory this will be included too
    ${project.name}-${project.version}.sh
        a bash script to run your Ostrich configured main class

### Basic configuration

build.sbt:

    name := "my-project"

    organization := "org.my.project"

    version := "1.0"

    seq(ostrichDistSettings: _*)

project/plugins.sbt:

    resolvers += "oxlade39 github ostrich" at "http://oxlade39.github.com/ostrich-dist-plugin/maven/"

    addSbtPlugin("org.doxla" % "ostrich-dist" % "0.3")

### Full configuration

project/plugins.sbt:

    resolvers += "oxlade39 github ostrich" at "http://oxlade39.github.com/ostrich-dist-plugin/maven/"

    addSbtPlugin("org.doxla" % "ostrich-dist" % "0.3")

project/ExampleBuild.scala:

    import org.doxla.sbt.ostrich.dist.OstrichDist
    import sbt._
    import Keys._

    object ExampleBuild extends Build {

        def standardSettings = Seq(
            exportJars := true
        ) ++ Defaults.defaultSettings

        lazy val root = Project(id = "root-project",
            base = file(".")) aggregate(proj1, proj2)

        lazy val proj1 = Project(id = "proj1",
            base = file("proj1"),
            settings = standardSettings) dependsOn proj2

        lazy val proj2 = Project(id = "proj2",
            base = file("proj2"),
            settings = standardSettings ++
            OstrichDist.ostrichDistSettings)

    }

### Running

    $ sbt create-ostrich-dist

### Expectations

The plugin currently has the following expectations on **directory locations**: (which may be made customisable if required)

    projectName/config
        - your Ostrich config files
    projectName/src/main/webapp
        - Optional webapp folder, containing say a WEB-INF sub-folder

And finally that you want a bash executable. (I've left it open to extend easily)