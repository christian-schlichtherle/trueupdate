/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.core.it

import com.stimulus.archiva.update.core.io.FileStore
import com.stimulus.archiva.update.core.io.Loan._
import com.stimulus.archiva.update.core.zip.diff.ZipDiff
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers._
import java.io.File
import java.util.jar.JarFile
import java.util.zip.ZipFile
import scala.collection.JavaConverters._
import scala.collection.SortedSet

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class ZipPatchIT extends WordSpec with ZipITContext {

  def tempFile() = File.createTempFile("tmp", null)

  "A JAR patch" when {
    "generating and applying the JAR diff file to the first test JAR file" should {
      "reconstitute the second test JAR file" in {
        val zipPatchTemp = tempFile ()
        try {
          withZipDiff { _ writeZipPatchFileTo new FileStore(zipPatchTemp) }
          loan(new ZipFile(zipPatchTemp)) to { zipPatchFile =>
            val firstZipTemp = tempFile ()
            try {
              withZipPatch(zipPatchFile) {
                _ applyZipPatchFileTo new FileStore(firstZipTemp)
              }
              loan(new JarFile(firstZipTemp)) to { firstZipFile =>
                loan(secondZipFile()) to { secondZipFile =>
                  val ref = SortedSet.empty[String] ++
                    secondZipFile.entries.asScala.map(_.getName)
                  val diff = new ZipDiff.Builder()
                    .firstZipFile(firstZipFile)
                    .secondZipFile(secondZipFile)
                    .build
                    .computeDiff ()
                  diff.added should be (null)
                  diff.removed should be (null)
                  diff.unchanged.keySet.asScala should equal (ref)
                  diff.changed should be (null)
                }
              }
            } finally {
              firstZipTemp delete ()
            }
          }
        } finally {
          zipPatchTemp delete ()
        }
      }
    }
  }
}
