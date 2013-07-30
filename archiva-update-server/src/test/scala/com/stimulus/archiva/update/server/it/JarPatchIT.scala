/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.it

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers._
import com.stimulus.archiva.update.server.jar.diff.JarDiff
import com.stimulus.archiva.update.core.io.FileStore
import java.io.File
import java.util.jar.JarFile
import java.util.zip.ZipFile
import scala.collection.JavaConverters._
import scala.collection.SortedSet
import com.stimulus.archiva.update.server.jar.model.{EntryDigest, Diff}

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class JarPatchIT extends WordSpec with JarDiffITContext {

  def tempFile() = File.createTempFile("tmp", null)

  "A JAR patch" when {
    "generating and applying the JAR diff file to the first test JAR file" should {
      "reconstitute the second test JAR file" in {
        val jarDiffTemp = tempFile ()
        try {
          withJarDiff { _ writeDiffFileTo new FileStore(jarDiffTemp) }
          val jarDiffFile = new ZipFile(jarDiffTemp)
          try {
            val firstJarTemp = tempFile ()
            try {
              withJarPatch(jarDiffFile) { _ applyDiffFileTo new FileStore(firstJarTemp) }
              val firstJarFile = new JarFile(firstJarTemp)
              try {
                val secondJarFile = this secondJarFile ()
                try {
                  val ref = SortedSet.empty[String] ++
                    secondJarFile.entries.asScala.map(_.getName)
                  val diff = new JarDiff.Builder()
                    .firstJarFile(firstJarFile)
                    .secondJarFile(secondJarFile)
                    .build
                    .computeDiff ()
                  diff.added should be (null)
                  diff.removed should be (null)
                  diff.unchanged.keySet.asScala should equal (ref)
                  diff.changed should be (null)
                } finally {
                  secondJarFile close ()
                }
              } finally {
                firstJarFile close ()
              }
            } finally {
              firstJarTemp delete ()
            }
          } finally {
            jarDiffFile close ()
          }
        } finally {
          jarDiffTemp delete ()
        }
      }
    }
  }
}
