/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jardiff

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers._

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class DifferIT extends WordSpec with DifferTestContext {

  "A JAR diff" when {
    "diffing the test JAR files" should {
      "partition the entries correctly" in {
        val jar1 = jarFile1()
        try {
          val jar2 = jarFile2()
          try {
            val result = differ.compute(jar1, jar2)
            import collection.JavaConverters._
            result.entriesOnlyInFile1.asScala map (_.entry.getName) should
              equal (List("entryOnlyInFile1"))
            result.entriesOnlyInFile2.asScala map (_.entry.getName) should
              equal (List("entryOnlyInFile2"))
            result.equalEntries.asScala map (_.entryInFile1.entry.getName) should
              equal (List("equalEntry"))
            result.differentEntries.asScala map (_.entryInFile1.entry.getName) should
              equal (List("META-INF/", "META-INF/MANIFEST.MF", "differentEntrySize", "differentEntryTime"))
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
