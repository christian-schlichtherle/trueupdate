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
import net.java.trueupdate.core.io.FileStore
import net.java.trueupdate.core.zip.diff.ZipDiff
import java.util.logging._

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class ZipPatchIT extends WordSpec with ZipITContext {

  def tempFile() = File.createTempFile("tmp", null)

  "A ZIP patch" when {
    "generating and applying the ZIP patch file to the first test JAR file" should {
      "reconstitute the second test JAR file" in {
        val zipPatchFile = tempFile ()
        try {
          loanZipDiff(_ writePatchFileTo new FileStore(zipPatchFile))
          loan(new ZipFile(zipPatchFile)) to { zipPatchFile =>
            val updatedJarFile = tempFile ()
            try {
              loanZipPatch(zipPatchFile) {
                _ applyZipPatchFileTo new FileStore(updatedJarFile)
              }
              loan(testJar2()) to { jarFile1 =>
                val unchangedReference = (List.empty[String] ++
                  jarFile1.entries.asScala.map(_.getName)).filter(!_.endsWith("/"))
                loan(new JarFile(updatedJarFile)) to { jarFile2 =>
                  val diffModel = ZipDiff.builder
                    .file1(jarFile1)
                    .file2(jarFile2)
                    .build
                    .computeDiffModel ()
                  diffModel.addedEntries.isEmpty should be (true)
                  diffModel.removedEntries.isEmpty should be (true)
                  diffModel.unchangedEntries.asScala map (_.name) should
                    equal (unchangedReference)
                  diffModel.changedEntries.isEmpty should be (true)
                }
              }
            } finally {
              updatedJarFile delete ()
            }
          }
        } finally {
          zipPatchFile delete ()
        }
      }
    }
  }
}
