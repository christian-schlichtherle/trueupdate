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
          var ex: Throwable = null
          val patchZipFile = new ZipFile(zipPatchFile)
          try {
            val updatedJarFile = tempFile ()
            try {
              loanZipPatch(patchZipFile) {
                _ applyZipPatchFileTo new FileStore(updatedJarFile)
              }
              val jarFile1 = testJar2()
              try {
                val unchangedReference = (List.empty[String] ++
                  jarFile1.entries.asScala.map(_.getName)).filter(!_.endsWith("/"))
                val jarFile2 = new JarFile(updatedJarFile)
                try {
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
                } catch {
                  case ex2: Throwable => ex = ex2; throw ex
                } finally {
                  try {
                    jarFile2 close ()
                  } catch {
                    case ex2: Throwable => if (null == ex) throw ex2
                  }
                }
              } catch {
                case ex2: Throwable => ex = ex2; throw ex
              } finally {
                try {
                  jarFile1 close ()
                } catch {
                  case ex2: Throwable => if (null == ex) throw ex2
                }
              }
            } finally {
              updatedJarFile delete ()
            }
          } catch {
            case ex2: Throwable => ex = ex2; throw ex
          } finally {
            try {
              patchZipFile close ()
            } catch {
              case ex2: Throwable => if (null == ex) throw ex2
            }
          }
        } finally {
          zipPatchFile delete ()
        }
      }
    }
  }
}
