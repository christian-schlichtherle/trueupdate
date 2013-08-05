/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.core

import com.stimulus.archiva.update.core.codec._
import com.stimulus.archiva.update.core.io.MemoryStore
import java.nio.charset.Charset
import java.lang.String
import javax.xml.bind.JAXBContext
import org.slf4j.LoggerFactory
import org.scalatest.matchers.ShouldMatchers._

/** @author Christian Schlichtherle */
object TestContext {

  val utf8 = Charset.forName("UTF-8")
}

/** @author Christian Schlichtherle */
trait TestContext {

  import TestContext._

  lazy val logger = LoggerFactory.getLogger(getClass)

  def utf8String(store: MemoryStore) = new String(store.data, utf8)

  def memoryStore = new MemoryStore

  def jaxbCodec: JaxbCodec = new TestJaxbCodec(jaxbContext)

  lazy val jaxbContext: JAXBContext = throw new UnsupportedOperationException

  final def assertRoundTripXmlSerializable(original: AnyRef) {
    val store = memoryStore
    jaxbCodec encode (store, original)
    logger debug ("\n{}", utf8String(store))
    val clone: AnyRef = jaxbCodec decode (store, original.getClass)
    clone should equal (original)
    clone should not be theSameInstanceAs (original)
  }
}
