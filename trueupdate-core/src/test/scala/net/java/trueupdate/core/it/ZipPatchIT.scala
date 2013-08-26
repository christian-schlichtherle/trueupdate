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
import net.java.trueupdate.core.zip.diff.ZipDiff

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
        val zipPatchFile = tempFile ()

        try {
          loanZipDiff(_ writePatchZipTo new FileStore(zipPatchFile))

          class ApplyPatchAndComputeReferenceAndDiffTask extends ZipInputTask[Unit, Exception] {
            override def execute(patchZipFile: ZipFile) {
              val updatedJarFile = tempFile ()

              try {
                loanZipPatch(patchZipFile) {
                  _ applyPatchZipTo new FileStore(updatedJarFile)
                }

                class ComputeReferenceAndDiffTask extends ZipInputTask[Unit, Exception] {
                  override def execute(jar1: ZipFile) {
                    val unchangedReference = fileEntryNames(jar1)

                    class DiffTask extends ZipInputTask[Unit, Exception] {
                      override def execute(jar2: ZipFile) {
                        val diffModel = ZipDiff.builder
                          .zip1(jar1)
                          .zip2(jar2)
                          .build
                          .computeDiffModel ()
                        diffModel.addedEntries.isEmpty should be (true)
                        diffModel.removedEntries.isEmpty should be (true)
                        diffModel.unchangedEntries.asScala map (_.name) should
                          equal (unchangedReference)
                        diffModel.changedEntries.isEmpty should be (true)
                      }
                    }

                    ZipSources execute new DiffTask on new JarFile(updatedJarFile)
                  }
                }

                ZipSources execute new ComputeReferenceAndDiffTask on new TestJar2Source
              } finally {
                updatedJarFile delete ()
              }
            }
          }

          ZipSources execute new ApplyPatchAndComputeReferenceAndDiffTask on new JarFile(zipPatchFile)
        } finally {
          zipPatchFile delete ()
        }
      }
    }
  }
}
