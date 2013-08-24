/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.it

import Loan._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers._
import java.io.File
import java.util.jar.JarFile
import java.util.zip.ZipFile
import scala.collection.JavaConverters._
import scala.collection.SortedSet
import net.java.trueupdate.core.io.FileStore
import net.java.trueupdate.core.zip.diff.ZipDiff

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class ZipPatchIT extends WordSpec with ZipITContext {

  def tempFile() = File.createTempFile("tmp", null)

  "A JAR patch" when {
    "generating and applying the JAR diff file to the first test JAR file" should {
      "reconstitute the second test JAR file" in {
        val zipPatchFile = tempFile ()
        try {
          withZipDiff { _ writeZipPatchFileTo new FileStore(zipPatchFile) }
          loan(new ZipFile(zipPatchFile)) to { zipPatchFile =>
            val patchedJarFile = tempFile ()
            try {
              withZipPatch(zipPatchFile) {
                _ applyZipPatchFileTo new FileStore(patchedJarFile)
              }
              loan(new JarFile(patchedJarFile)) to { zipFile1 =>
                loan(zipFile2()) to { zipFile2 =>
                  val ref = List.empty[String] ++
                    zipFile2.entries.asScala.map(_.getName)
                  val diff = ZipDiff.builder
                    .zipFile1(zipFile1)
                    .zipFile2(zipFile2)
                    .build
                    .computeZipDiffModel ()
                  diff.addedEntries.isEmpty should be (true)
                  diff.removedEntries.isEmpty should be (true)
                  diff.unchangedEntries.asScala map (_.entryName) should equal (ref)
                  diff.changedEntries.isEmpty should be (true)
                }
              }
            } finally {
              patchedJarFile delete ()
            }
          }
        } finally {
          zipPatchFile delete ()
        }
      }
    }
  }
}
