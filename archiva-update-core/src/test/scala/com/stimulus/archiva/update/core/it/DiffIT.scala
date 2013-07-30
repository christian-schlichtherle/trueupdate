/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.core.it

import com.stimulus.archiva.update.core.codec.JaxbTestCodec
import com.stimulus.archiva.update.core.io.MemoryStore
import com.stimulus.archiva.update.core.zip.model.Diff
import java.nio.charset.Charset
import javax.xml.bind.JAXBContext
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers._
import org.slf4j.LoggerFactory

private object DiffIT {
  val logger = LoggerFactory.getLogger(classOf[DiffIT])

  val jaxbContext = JAXBContext.newInstance(classOf[Diff])
  val codec = new JaxbTestCodec(jaxbContext)

  def store = new MemoryStore

  val utf8 = Charset.forName("UTF-8")
  def xmlString(store: MemoryStore) = new String(store.data(), utf8)
}

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class DiffIT extends WordSpec with ZipITContext {

  import DiffIT._

  def roundTrip(diff: Diff) {
    val store = DiffIT.store
    codec encode (store, diff)
    logger debug ("\n{}", xmlString(store))
    val diff2: Diff = codec decode (store, classOf[Diff])
    diff2 should equal (diff)
    diff2 should not be theSameInstanceAs (diff)
  }

  "A ZIP diff bean" when {
    "constructed with no data" should {
      "be round-trip XML-serializable" in {
        roundTrip(new Diff)
      }
    }

    "constructed from a ZIP diff" should {
      "be round-trip XML-serializable" in {
        roundTrip(withZipDiff(_ computeDiff ()))
      }
    }
  }
}
