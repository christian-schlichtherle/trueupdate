/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.core.it

import com.stimulus.archiva.update.core.io.MemoryStore
import java.nio.charset.Charset
import java.lang.String
import com.stimulus.archiva.update.core.codec.{TestJaxbCodec, JaxbCodec}
import org.slf4j.LoggerFactory
import javax.xml.bind.JAXBContext

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

  def jaxbCodec = new TestJaxbCodec(jaxbContext)

  lazy val jaxbContext: JAXBContext = throw new UnsupportedOperationException
}
