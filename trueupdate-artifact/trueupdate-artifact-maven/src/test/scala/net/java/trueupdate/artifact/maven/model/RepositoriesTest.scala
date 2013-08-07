/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.artifact.maven.model

import net.java.trueupdate.core.TestContext
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.WordSpec

/** @author Christian Schlichtherle */
@RunWith(classOf[JUnitRunner])
class RepositoriesTest extends WordSpec with TestContext {

  def empty = repositories(null)

  def populated =
    repositories(local("basedir"), remote("url1"), remote("url2"))

  def local(basedir: String) = new Local(basedir, "local")

  def remote(url: String) = new Remote(null, "remote", url)

  def repositories(local: Local, remotes: Remote*) = {
    import collection.JavaConverters._
    new Repositories(local, remotes.asJava)
  }

  override lazy val jaxbContext = Repositories.jaxbContext

  "A repositories model" when {
    "constructed with no data" should {
      "be round-trip XML-serializable" in {
        assertRoundTripXmlSerializable(empty)
      }
    }

    "constructed with all data populated" should {
      "be round-trip XML-serializable" in {
        assertRoundTripXmlSerializable(populated)
      }
    }
  }
}
