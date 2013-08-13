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
                  val ref = List.empty[String] ++
                    secondZipFile.entries.asScala.map(_.getName)
                  val diff = ZipDiff.builder
                    .firstZipFile(firstZipFile)
                    .secondZipFile(secondZipFile)
                    .build
                    .computeDiffModel ()
                  diff.addedEntries.isEmpty should be (true)
                  diff.removedEntries.isEmpty should be (true)
                  diff.unchangedEntries.asScala map (_.name) should equal (ref)
                  diff.changedEntries.isEmpty should be (true)
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
