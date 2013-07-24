/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers._
import java.io.File
import java.util.jar.JarFile
import com.stimulus.archiva.update.server.JarDiff._

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class JarDiffEngineIT extends WordSpec {

  private def file(name: String) = new File((getClass getResource name).toURI)

  private val comparator = new Comparator {
    def equals(entryInFile1: EntryInFile, entryInFile2: EntryInFile) = {
      val entry1 = entryInFile1.entry
      val entry2 = entryInFile2.entry
      entry1.getName should equal (entry2.getName)
      entry1.getTime == entry2.getTime &&
        entry1.getSize == entry2.getSize &&
        entry1.getCrc == entry2.getCrc
    }
  }
  private val diff = new JarDiff(comparator)

  "A JAR diff engine" when {
    "diffing the test JAR files" should {

      "call the template methods with correct parameters" in {
        val jar1 = new JarFile(file("test1.jar"))
        try {
          val jar2 = new JarFile(file("test2.jar"))
          try {
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
