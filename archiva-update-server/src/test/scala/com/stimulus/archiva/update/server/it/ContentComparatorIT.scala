/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.it

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers._
import org.scalatest.prop.PropertyChecks._
import com.stimulus.archiva.update.server.jardiff.model.EntryInFile

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class ContentComparatorIT extends WordSpec with JarDiffITContext {

  "A JAR content comparator" when {
    "mutually comparing all entries in a JAR file" should {
      "find them to be equal if and only if comparing an equal named entry" in {
        val table = Table(
          ("jar"),
          (jarFile1()),
          (jarFile2())
        )
        forAll(table) { jar =>
          try {
            def entries = {
              import collection.JavaConverters._
              jar.entries.asScala
            }
            entries foreach { entry1 =>
              entries foreach { entry2 =>
                val entry1InJar = new EntryInFile(entry1, jar)
                val entry2InJar = new EntryInFile(entry2, jar)
                val equalNames = entry1.getName == entry2.getName
                comparator equals (entry1InJar, entry2InJar) should
                  be (equalNames)
              }
            }
          } finally {
            jar close ()
          }
        }
      }
    }
  }
}
