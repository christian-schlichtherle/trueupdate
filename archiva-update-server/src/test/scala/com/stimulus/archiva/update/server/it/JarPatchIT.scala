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

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class JarPatchIT extends WordSpec with JarDiffITContext {

  def tempFile() = File.createTempFile("tmp", null)

  "A JAR patch" when {
    "generating and applying the JAR diff file to the first test JAR file" should {
      "reconstitute the second test JAR file" in {
        val diffTemp = tempFile ()
        try {
          withJarDiff { _ writeDiffFileTo new FileStore(diffTemp) }
          val diffZip = new ZipFile(diffTemp)
          try {
            val jarTemp = tempFile ()
            try {
              withJarPatch(diffZip) { _ applyDiffFileTo new FileStore(jarTemp) }
              val jarFile1 = new JarFile(jarTemp)
              try {
                val jarFile2 = this jarFile2 ()
                try {
                  val diff = new JarDiff.Builder()
                    .firstJarFile(jarFile1)
                    .secondJarFile(jarFile2)
                    .build
                    .computeDiff ()
                  diff.removed should be (null)
                  diff.added should be (null)
                  diff.changed should be (null)
                  val ref = SortedSet.empty[String] ++ jarFile2.entries.asScala.map(_.getName)
                  diff.unchanged.keySet.asScala should equal (ref)
                } finally {
                  jarFile2 close ()
                }
              } finally {
                jarFile1 close ()
              }
            } finally {
              jarTemp delete ()
            }
          } finally {
            diffZip close ()
          }
        } finally {
          diffTemp delete ()
        }
      }
    }
  }
}
