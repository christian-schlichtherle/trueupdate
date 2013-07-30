/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.it

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers._
import com.stimulus.archiva.update.server.jar.diff.ZipDiff
import com.stimulus.archiva.update.core.io.FileStore
import java.io.File
import java.util.jar.JarFile
import java.util.zip.ZipFile
import scala.collection.JavaConverters._
import scala.collection.SortedSet

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class JarPatchIT extends WordSpec with JarDiffITContext {

  def tempFile() = File.createTempFile("tmp", null)

  "A JAR patch" when {
    "generating and applying the JAR diff file to the first test JAR file" should {
      "reconstitute the second test JAR file" in {
        val zipPatchTemp = tempFile ()
        try {
          withZipDiff { _ writeDiffFileTo new FileStore(zipPatchTemp) }
          val zipPatchFile = new ZipFile(zipPatchTemp)
          try {
            val firstZipTemp = tempFile ()
            try {
              withZipPatch(zipPatchFile) { _ applyDiffFileTo new FileStore(firstZipTemp) }
              val firstZipFile = new JarFile(firstZipTemp)
              try {
                val secondZipFile = this secondZipFile ()
                try {
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
                } finally {
                  secondZipFile close ()
                }
              } finally {
                firstZipFile close ()
              }
            } finally {
              firstZipTemp delete ()
            }
          } finally {
            zipPatchFile close ()
          }
        } finally {
          zipPatchTemp delete ()
        }
      }
    }
  }
}
