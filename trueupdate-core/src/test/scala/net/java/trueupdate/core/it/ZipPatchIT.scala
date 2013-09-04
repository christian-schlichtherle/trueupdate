/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.it

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers._
import java.io._
import scala.collection.JavaConverters._
import net.java.trueupdate.core.io._
import net.java.trueupdate.core.zip._
import net.java.trueupdate.core.zip.diff.RawZipDiff

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class ZipPatchIT extends WordSpec with ZipITContext {

  def tempFile() = File.createTempFile("tmp", null)

  def fileEntryNames(zip: ZipInput) = List.empty[String] ++
    zip.iterator.asScala.filter(!_.isDirectory).map(_.getName)

  "A ZIP patch" when {
    "generating and applying the ZIP patch file to the first test JAR file" should {
      "reconstitute the second test JAR file" in {

        class ApplyPatchAndComputeReferenceAndDiffTask extends ZipInputTask[Unit, Exception] {
          override def execute(diffFile: ZipInput) {
            val patchedFile = tempFile()

            class ComputeReferenceAndDiffTask extends ZipInputTask[Unit, Exception] {
              override def execute(archive1: ZipInput) {
                val unchangedReference = fileEntryNames(archive1)

                class DiffTask extends ZipInputTask[Unit, Exception] {
                  override def execute(archive2: ZipInput) {
                    val model = new RawZipDiff {
                      val _digest = MessageDigests.sha1

                      override def input1 = archive1
                      override def input2 = archive2
                      override def digest = _digest
                    } model ()
                    model.addedEntries.isEmpty should be (true)
                    model.removedEntries.isEmpty should be (true)
                    model.unchangedEntries.asScala map (_.name) should
                      equal (unchangedReference)
                    model.changedEntries.isEmpty should be (true)
                  }
                }

                ZipSources execute new DiffTask on new JarFileStore(patchedFile)
              }
            }

            try {
              loanRawZipPatch(diffFile)(_ output new JarFileStore(patchedFile).output)
              ZipSources execute new ComputeReferenceAndDiffTask on testJar2()
            } finally {
              patchedFile delete ()
            }
          }
        }

        val diff = tempFile()
        try {
          loanRawZipDiff(_ output new ZipFileStore(diff).output())
          ZipSources execute new ApplyPatchAndComputeReferenceAndDiffTask on diff
        } finally {
          diff delete ()
        }
      }
    }
  }
}
