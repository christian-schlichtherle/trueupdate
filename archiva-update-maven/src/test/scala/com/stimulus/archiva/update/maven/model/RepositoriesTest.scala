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

  override lazy val jaxbContext = JAXBContext.newInstance(classOf[Repositories])

  def populated = {
    val repositories = this.repositories
    repositories.local = local("basedir")
    repositories.remote.add(remote("url1"))
    repositories.remote.add(remote("url2"))
    repositories
  }

  def local(basedir: String) = {
    val local = new Local
    local.basedir = basedir
    local
  }

  def remote(url: String) = {
    val remote = new Remote
    remote.`type` = "remote"
    remote.url = url
    remote
  }

  def repositories = new Repositories

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
        roundTrip(repositories)
      }
    }

    "constructed with all data populated" should {
      "be round-trip XML-serializable" in {
        roundTrip(populated)
      }
    }
  }
}
