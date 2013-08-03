/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.maven.model

import com.stimulus.archiva.update.core.TestContext
import javax.xml.bind.JAXBContext
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers._

/** @author Christian Schlichtherle */
@RunWith(classOf[JUnitRunner])
class RepositoriesTest extends WordSpec with TestContext {

  override lazy val jaxbContext = Repositories.jaxbContext

  def empty = repositories(null)

  def populated =
    repositories(local("basedir"), remote("url1"), remote("url2"))

  def local(basedir: String) = new Local(basedir, "local")

  def remote(url: String) = new Remote(null, "remote", url)

  def repositories(local: Local, remotes: Remote*) = {
    import collection.JavaConverters._
    new Repositories(local, remotes.asJava)
  }

  def roundTrip(original: Repositories) {
    val store = memoryStore
    jaxbCodec encode (store, original)
    logger debug ("\n{}", utf8String(store))
    val clone: Repositories = jaxbCodec decode (store, classOf[Repositories])
    clone should equal (original)
    clone should not be theSameInstanceAs (original)
  }

  "A repositories model" when {
    "constructed with no data" should {
      "be round-trip XML-serializable" in {
        roundTrip(empty)
      }
    }

    "constructed with all data populated" should {
      "be round-trip XML-serializable" in {
        roundTrip(populated)
      }
    }
  }
}
