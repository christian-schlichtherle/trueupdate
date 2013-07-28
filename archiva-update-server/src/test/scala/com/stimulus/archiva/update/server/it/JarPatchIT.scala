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

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class JarPatchIT extends WordSpec with JarDiffITContext {

  def tempFile() = File.createTempFile("tmp", "jar")

  "A JAR patch" when {
    "generating and applying the JAR patch file to the first test JAR file" should {
      "reconstitute the second test JAR file" in {
        val temp = tempFile ()
        try {
          val patch = this.store
          withJarDiff { _ writePatchFileTo patch }
          withJarPatch(patch) { _ applyPatchFileTo new FileStore(temp) }
          ;{
            val jarFile1 = this.jarFile2
            try {
              val jarFile2 = new JarFile(temp)
              try {
                val diff = new JarDiff.Builder()
                  .jarFile1(jarFile1)
                  .jarFile2(jarFile2)
                  .build
                  .computeDiff ()
                diff.removed should be (null)
                diff.added should be (null)
                diff.changed should be (null)
                diff.unchanged should not be 'empty
              } finally {
                jarFile2 close ()
              }
            } finally {
              jarFile1 close ()
            }
          }
        } finally {
          temp delete ()
        }
      }
    }
  }
}
