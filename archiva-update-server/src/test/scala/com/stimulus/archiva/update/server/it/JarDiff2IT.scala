/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.it

import com.stimulus.archiva.update.core.codec.JaxbTestCodec
import com.stimulus.archiva.update.core.io.MemoryStore
import com.stimulus.archiva.update.server.jardiff.JarDiff
import com.stimulus.archiva.update.server.jarpatch.model.Diff
import com.stimulus.archiva.update.server.util.MessageDigests
import java.lang.String
import java.nio.charset.Charset
import javax.xml.bind.JAXBContext
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers._
import org.slf4j.LoggerFactory

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class JarDiff2IT extends WordSpec with JarDiffITContext {

  "A JAR diff" when {
    "diffing the test JAR files" should {
      "partition the entry names and digests correctly" in {
        val index = this index ()
        import collection.JavaConverters._
        import index._
        removed.values.asScala map (_.name) should
          equal (List("entryOnlyInFile1"))
        added.values.asScala map (_.name) should
          equal (List("entryOnlyInFile2"))
        unchanged.values.asScala map (_.name) should
          equal (List("META-INF/", "META-INF/MANIFEST.MF", "differentEntryTime", "equalEntry"))
        changed.values.asScala map (_.name) should
          equal (List("differentEntrySize"))
      }
    }
  }
}
