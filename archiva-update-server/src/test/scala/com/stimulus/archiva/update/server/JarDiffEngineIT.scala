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
import JarDiff.Fingerprint

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class JarDiffEngineIT extends WordSpec {

  private def file(name: String) = new File((getClass getResource name).toURI)

  "A JAR diff engine" when {
    "diffing the test JAR files" should {
      val file1 = new JarFile(file("test1.jar"))
      val file2 = new JarFile(file("test2.jar"))
      val engine = new JarDiff.Engine(file1, file2) {

        override def onEntryOnlyInFile1(fingerprint1: Fingerprint) {
          fingerprint1.name should equal ("entryOnlyInFile1")
        }

        override def onEntryOnlyInFile2(fingerprint2: Fingerprint) {
          fingerprint2.name should equal ("entryOnlyInFile2")
        }

        override def onEqualEntries(fingerprint1: Fingerprint, fingerprint2: Fingerprint) {
          fingerprint1.name should equal (fingerprint2.name)
          fingerprint1 should equal (fingerprint2)
          fingerprint1.name should equal ("equalEntry")
        }

        override def onDifferentEntries(fingerprint1: Fingerprint, fingerprint2: Fingerprint) {
          fingerprint1.name should equal (fingerprint2.name)
          fingerprint1 should not equal (fingerprint2)
          Set("META-INF/", "META-INF/MANIFEST.MF", "differentEntrySize", "differentEntryTime") should
            contain (fingerprint1.name)
        }
      }

      "call the template methods with correct parameters" in {
        try {
          engine run ()
        } finally {
          try {
            engine.file1 close ()
          } finally {
            engine.file2 close ()
          }
        }
      }
    }
  }
}
