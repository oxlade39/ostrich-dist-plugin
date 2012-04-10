package org.doxla.sbt.ostrich.dist

import org.scalatest.matchers.MustMatchers
import org.doxla.sbt.ostrich.dist.GithubPagesMavenPublish.IndexMaker
import org.scalatest.{BeforeAndAfterEach, FlatSpec}
import sbt.Resolver.file
import java.io.File
import sbt.{PathExtra, IO}
import xml.{NodeSeq, Elem, XML}

/**
 * @author dan
 */

class IndexSpec extends FlatSpec with MustMatchers with PathExtra with BeforeAndAfterEach {
  behavior of "IndexMaker"

  var tmpDir: File = null



  it should "create an index.html in the root directory" in {
    val maker: IndexMaker = new IndexMaker(tmpDir)

    (tmpDir / "index.html").exists() must be(false)
    maker.make()
    (tmpDir / "index.html").exists() must be(true)

  }

  it should "add the css to the root directory" in {
    new IndexMaker(tmpDir).make()

    val cssFile: File = (tmpDir) / "maven-repo.css"
    cssFile.exists() must be(true)
    IO.read(cssFile).size must be > 1

  }

  it should "create an index.html which lists all subdirectories" in {
    val dirs: List[File] = tmpDir / "subdir1" ::
                           tmpDir / "subdir2" / "subdir2subdir1" :: Nil
    dirs.foreach(_.mkdirs())

    new IndexMaker(tmpDir).make()

    val index: String = IO.read(tmpDir / "index.html")
    val indexHtml: Elem = MyXML.loadString(index)

    val links: List[String] = ((indexHtml \\ "li" \ "a" \\ "@href") map (_.text)).toList

    links must be( "../" :: "subdir1/" :: "subdir2/" :: Nil )
  }

  it should "recurse down each sub directory" in {
    val dirs: List[File] = tmpDir / "subdir1" ::
                           tmpDir / "subdir2" / "subdir2subdir1" :: Nil
    dirs.foreach(_.mkdirs())

    new IndexMaker(tmpDir).make()

    val index: String = IO.read(tmpDir / "subdir2" / "index.html")
    val indexHtml: Elem = MyXML.loadString(index)

    val links: List[String] = ((indexHtml \\ "li" \ "a" \\ "@href") map (_.text)).toList

    links must be( "../" :: "subdir2subdir1/" :: Nil )
  }

  it should "create index.html with title equal to relative path" in {
    val dirs: List[File] = tmpDir / "subdir1" ::
                           tmpDir / "subdir2" / "subdir2subdir1" :: Nil
    dirs.foreach(_.mkdirs())

    new IndexMaker(tmpDir).make()

    val index: String = IO.read(tmpDir / "subdir2" / "subdir2subdir1" / "index.html")
    val indexHtml: Elem = MyXML.loadString(index)

    val headings: List[String] = ((indexHtml \\ "h1") map (_.text)).toList

    headings must be( tmpDir.name + "/subdir2/subdir2subdir1" :: Nil )
  }

  // don't know why the compiler requires this
  override def fail = None.asInstanceOf[Nothing]

  override protected def beforeEach() {
    tmpDir = IO.createTemporaryDirectory
    println("tmp dir: %s".format(tmpDir))
  }

  override protected def afterEach() {
    IO.delete(tmpDir)
  }
}

import scala.xml.Elem
import scala.xml.factory.XMLLoader
import javax.xml.parsers.SAXParser

object MyXML extends XMLLoader[Elem] {
  override def parser: SAXParser = {
    val f = javax.xml.parsers.SAXParserFactory.newInstance()
    f.setNamespaceAware(false)
    f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    f.newSAXParser()
  }
}
