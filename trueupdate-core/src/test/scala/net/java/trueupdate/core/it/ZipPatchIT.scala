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

  "A ZIP patch" when {
    "generating and applying the ZIP patch file to the first test JAR file" should {
      "reconstitute the second test JAR file" in {
        val zipPatchFile = tempFile ()
        class ZipPatchFileSource extends ZipSource {
          override def input() = new JarFile(zipPatchFile)
        }
        try {
          loanZipDiff(_ writePatchFileTo new FileStore(zipPatchFile))
          new ZipInputTask[Unit, Exception](new ZipPatchFileSource) {
            override def execute(patchZipFile: ZipFile) {
              val updatedJarFile = tempFile ()
              class UpdatedJarFileSource extends ZipSource {
                override def input() = new JarFile(updatedJarFile)
              }
              try {
                loanZipPatch(patchZipFile) {
                  _ applyZipPatchFileTo new FileStore(updatedJarFile)
                }
                new ZipInputTask[Unit, Exception](new TestJar2Source) {
                  override def execute(zipFile1: ZipFile) {
                    val jarFile1 = zipFile1.asInstanceOf[JarFile]
                    val unchangedReference = (List.empty[String] ++
                      jarFile1.entries.asScala.map(_.getName)).filter(!_.endsWith("/"))
                    new ZipInputTask[Unit, Exception](new UpdatedJarFileSource) {
                      override def execute(jarFile2: ZipFile) {
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
                    } call ()
                  }
                } call ()
              } finally {
                updatedJarFile delete ()
              }
            }
          } call ()
        } finally {
          zipPatchFile delete ()
        }
      }
    }
  }
}
