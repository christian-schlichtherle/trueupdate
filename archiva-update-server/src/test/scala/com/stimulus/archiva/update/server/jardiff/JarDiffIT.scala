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
class JarDiffIT extends WordSpec with JarDiffTestContext {

  "A JAR diff" when {
    "diffing the test JAR files" should {
      "partition the entries correctly" in {
        val jar1 = jarFile1()
        try {
          val jar2 = jarFile2()
          try {
            val diff = jarDiff.compute(jar1, jar2)
            import collection.JavaConverters._
            diff.entriesOnlyInFile1.asScala map (_.entry.getName) should
              equal (List("entryOnlyInFile1"))
            diff.entriesOnlyInFile2.asScala map (_.entry.getName) should
              equal (List("entryOnlyInFile2"))
            diff.equalEntries.asScala map (_.entryInFile1.entry.getName) should
              equal (List("equalEntry"))
            diff.equalEntries.asScala map (_.entryInFile2.entry.getName) should
              equal (List("equalEntry"))
            diff.differentEntries.asScala map (_.entryInFile1.entry.getName) should
              equal (List("META-INF/", "META-INF/MANIFEST.MF", "differentEntrySize", "differentEntryTime"))
            diff.differentEntries.asScala map (_.entryInFile2.entry.getName) should
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
