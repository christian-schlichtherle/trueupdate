/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.it

import com.stimulus.archiva.update.core.codec.JaxbTestCodec
import com.stimulus.archiva.update.core.io.MemoryStore
import com.stimulus.archiva.update.server.jardiff.dto.Index
import java.nio.charset.Charset
import javax.xml.bind.JAXBContext
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers._
import org.slf4j.LoggerFactory

private object IndexIT {
  val logger = LoggerFactory.getLogger(classOf[IndexIT])

  val jaxbContext = JAXBContext.newInstance(classOf[Index])
  val codec = new JaxbTestCodec(jaxbContext)

  def store = new MemoryStore

  val utf8 = Charset.forName("UTF-8")
  def xmlString(store: MemoryStore) = new String(store.data(), utf8)
}

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class IndexIT extends WordSpec with JarDiffITContext {

  import IndexIT._

  def roundTrip(index: Index) {
    val store = IndexIT.store
    codec encode (store, index)
    logger debug ("\n{}", xmlString(store))
    val index2: Index = codec decode (store, classOf[Index])
    index2 should equal (index)
    index2 should not be theSameInstanceAs (index)
  }

  "An index" when {
    "constructed with no data" should {
      "be round-trip XML-serializable" in {
        roundTrip(new Index)
      }
    }

    "constructed from a JAR diff" should {
      "be round-trip XML-serializable" in {
        val jar1 = jarFile1()
        try {
          val jar2 = jarFile2()
          try {
            roundTrip(new Index(jarDiff.compute(jar1, jar2), digest))
          } finally {
            jar2 close ()
          }
        } finally {
          jar1 close ()
        }
      }
    }
  }
}
