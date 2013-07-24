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
class JarDiffEngineIT extends WordSpec with JarDiffTestContext {

  "A JAR diff engine" when {
    "diffing the test JAR files" should {
      "call the template methods with correct parameters" in {
        val jar1 = jarFile1()
        try {
          val jar2 = jarFile2()
          try {
            val diff = jarDiff
            val engine = new diff.Engine(jar1, jar2) {
              override def onEntryOnlyInFile1(entryInFile1: EntryInFile) {
                entryInFile1.entry.getName should equal ("entryOnlyInFile1")
              }

              override def onEntryOnlyInFile2(entryInFile2: EntryInFile) {
                entryInFile2.entry.getName should equal ("entryOnlyInFile2")
              }

              override def onEqualEntries(entryInFile1: EntryInFile,
                                          entryInFile2: EntryInFile) {
                entryInFile1.entry.getName should equal (entryInFile2.entry.getName)
                entryInFile1.entry.getName should equal ("equalEntry")
              }

              override def onDifferentEntries(entryInFile1: EntryInFile,
                                              entryInFile2: EntryInFile) {
                entryInFile1.entry.getName should equal (entryInFile2.entry.getName)
                Set("META-INF/", "META-INF/MANIFEST.MF", "differentEntrySize", "differentEntryTime") should
                  contain (entryInFile1.entry.getName)
              }
            }
            engine run ()
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
