/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.it

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers._
import java.io.File
import java.util.jar.JarFile
import java.util.zip.ZipFile
import scala.collection.JavaConverters._
import net.java.trueupdate.core.io._
import net.java.trueupdate.core.zip.diff.RawZipDiff

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class ZipPatchIT extends WordSpec with ZipITContext {

  def tempFile() = File.createTempFile("tmp", null)

  def fileEntryNames(zipFile: ZipFile) = (List.empty[String] ++
    zipFile.entries.asScala.map(_.getName)).filter(!_.endsWith("/"))

  "A ZIP patch" when {
    "generating and applying the ZIP patch file to the first test JAR file" should {
      "reconstitute the second test JAR file" in {

        class ApplyPatchAndComputeReferenceAndDiffTask extends ZipInputTask[Unit, Exception] {
          override def execute(diffFile: ZipFile) {
            val patchedFile = tempFile()

            class ComputeReferenceAndDiffTask extends ZipInputTask[Unit, Exception] {
              override def execute(input1: ZipFile) {
                val unchangedReference = fileEntryNames(input1)

                class DiffTask extends ZipInputTask[Unit, Exception] {
                  override def execute(input2: ZipFile) {
                    val model = RawZipDiff
                      .builder
                      .input1(input1)
                      .input2(input2)
                      .build
                      .model ()
                    model.addedEntries.isEmpty should be (true)
                    model.removedEntries.isEmpty should be (true)
                    model.unchangedEntries.asScala map (_.name) should
                      equal (unchangedReference)
                    model.changedEntries.isEmpty should be (true)
                  }
                }

                ZipSources execute new DiffTask on new JarFile(patchedFile)
              }
            }

            try {
              loanZipPatch(diffFile)(_ output patchedFile)
              ZipSources execute new ComputeReferenceAndDiffTask on testJar2()
            } finally {
              patchedFile delete ()
            }
          }
        }

        val diff = tempFile()
        try {
          loanRawZipDiff(_ output diff)
          ZipSources execute new ApplyPatchAndComputeReferenceAndDiffTask on diff
        } finally {
          diff delete ()
        }
      }
    }
  }
}
