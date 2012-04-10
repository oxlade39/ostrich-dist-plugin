package org.doxla.sbt.ostrich.dist

import sbt._
import sbt.Keys._
import xml.PrettyPrinter


/**
 * @author oxladed
 */
object GithubPagesMavenPublish extends Plugin {
  val githubPagesMavenPublish: TaskKey[File] = TaskKey[File]("github-publish",
    "Publish project artifacts to a maven repo on gh-pages")

  val githubPagesMavenPublishSettings = Seq(
    publish in githubPagesMavenPublish <<= (publish, publishTo) map {
      (pub, to) =>
        val originalPublishTask: Types.Id[Unit] = pub

        to match {
          case Some(FileRepository(fileName, publishConfig, patterns)) if(publishConfig.isLocal)=> {
            val publishToDirectory: sbt.File = file(fileName)
            if(publishToDirectory.exists()) {
              IndexMaker(publishToDirectory)
            }
          }
          case _ =>
        }

        originalPublishTask
    }
  ).asInstanceOf[Seq[Project.Setting[_]]]

  object IndexMaker {
    def apply(root: File) {
      new IndexMaker(root).make()
    }
  }

  class IndexMaker(val root: File) {
    val cssFile = root / "maven-repo.css"
    val dirs: List[File] = root ::
                           (root ** DirectoryFilter).get.toList.sorted

    val indecies: List[Index] = dirs map {
      case f: File => Index(f, root, rootLink(Some(f)) + "/" + cssFile.name)
    }

    def rootLink(dir: Option[File]): String = dir match {
      case None => ".."
      case Some(f) if f == root => ""
      case Some(f) => rootLink(Option(f.getParentFile)) + "/.."
    }

    def make() {
      writeCss()
      writeIndexFiles()
    }

    def writeIndexFiles() {
      indecies.foreach(_.write())
    }

    def writeCss() {
      IO.write(cssFile,
        """
        body {
          font-family: Verdana;
          font-size: 20px;
          color: #B78E68;
        }
        h1 {
          font-size: 30px;
        }
        ul {
          list-style: none;
          padding-left: 20px;
        }
        a:visited {
          color: blue;
        }
        """)
    }
  }

  case class Index(dir: File, root: File, cssLink: String) {

    val excludes = "maven-repo.css" :: "index.html" :: Nil

    val children: List[String] = (dir * AllPassFilter).get.toList.map {
      case f: File if (f.isDirectory) => f.name + "/"
      case f: File => f.name
    }.filterNot(excludes.contains(_)).sorted

    def indexHtml =
      <html>
        <head><link type="text/css" href={ cssLink } rel="stylesheet" /></head>
        <body>
          <h1>{ dir.relativeTo(root.getParentFile) map (_.getPath) getOrElse("?") }</h1>
          <ul>
            <li><a href="../">..</a></li>
            { for (child <- children) yield <li><a href={ child }>{ child }</a></li> }
          </ul>
        </body>
      </html>

    def write() {
      val indexFile: File = dir / "index.html"
      IO.write(indexFile, """<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">""")
      IO.append(indexFile, "\n")
      IO.append(indexFile, new PrettyPrinter(120,2).format(indexHtml))
    }
  }
}