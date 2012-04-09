package org.doxla.sbt.ostrich.dist

import sbt._
import sbt.Keys._

object OstrichDist extends Plugin
{
  val createOstrichDist: TaskKey[File] = TaskKey[File]("create-ostrich-dist",
    "Create an Ostrich compatable distribution zip")

  val ostrichDistSettings = Seq(
    target in createOstrichDist ~= {value: File => value / "ostrich-dist"},

    sources in createOstrichDist <<= (baseDirectory in Compile) map {
      (bd: File) =>
        val configSrc = bd / "config"
        val webappSrc: Seq[File] = (bd / "src/main/webapp") match {
          case f: File if f.exists() => Seq(f)
          case _ => Seq[File]()
        }
        Seq[File](configSrc) ++ webappSrc
    },

    unmanagedResources in createOstrichDist <<= (name, version, mainClass in Runtime) map {
      (projectName: String, projectVersion: String, classToRun: Option[String]) =>
        val t = IO.createTemporaryDirectory

        val buildProperties: File = t / "build.properties"
        IO.append(buildProperties, "\n#created by createOstrichDist task\n")
        IO.append(buildProperties, "name=%s\n".format(projectName))
        IO.append(buildProperties, "version=%s\n".format(projectVersion))

        val scripts: Seq[File] = classToRun map { clazzName: String =>
          val bashScript: File = t / (projectName + "-" + projectVersion + ".sh")
          bashScript.setExecutable(true)
          IO.append(bashScript, "#!/bin/sh\n")

          IO.append(bashScript, "\n#created by createOstrichDist task\n")
          // TODO add java options
          IO.append(bashScript, "java -cp \"`dirname $0`/lib/*\" %s".format(clazzName))

          Seq(bashScript)
        } getOrElse(Seq[File]())

        Seq[File](buildProperties) ++ scripts
    },

    artifact in createOstrichDist := {
      Artifact("ostrich-dist.zip")
    },

    packageBin in createOstrichDist <<= (target in createOstrichDist,
      sources in createOstrichDist,
      unmanagedResources in createOstrichDist,
      artifact in createOstrichDist,
      dependencyClasspath in Runtime,
      streams
      ) map {
      (targetF: File, sourceFiles: Seq[File], scripts: Seq[File], artifactA: Artifact, cp: Classpath, s) =>

        val artifactTarget = targetF / artifactA.name
        s.log.info { "Creating ostrich distribution: %s".format(artifactTarget.getAbsolutePath) }
        val classpathFiles: Seq[File] = Build.data(cp)

        val libs: Seq[(sbt.File, String)] = classpathFiles map {
          f: File => (f, "lib/" + f.getName)
        }

        val scriptsToAdd: Seq[(sbt.File, String)] = scripts map {
          (f: File) => (f, f.getName)
        }

        val sourceParentWithSources: Seq[(sbt.File, Seq[File])] = sourceFiles.map {
          f: File => (f, (f ** "*").get)
        }

        val allSrcFiles: Seq[(sbt.File, String)] = sourceParentWithSources flatMap {
          case (parent: File, children: Seq[File]) =>
            children map {
              f: File => (f, parent.name + "/" + f.getAbsolutePath.replace(parent.getAbsolutePath, ""))
            }
        }

        IO.zip(libs ++ allSrcFiles ++ scriptsToAdd, artifactTarget)

        artifactTarget
    },

    createOstrichDist <<= packageBin in createOstrichDist
  ).asInstanceOf[Seq[Project.Setting[_]]]

}
