/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.it

import com.stimulus.archiva.update.core.io._
import com.stimulus.archiva.update.server.jar.diff.JarDiff
import com.stimulus.archiva.update.server.util.MessageDigests
import edu.umd.cs.findbugs.annotations.CreatesObligation
import java.io.File
import java.util.jar.JarFile
import com.stimulus.archiva.update.server.jar.patch.JarPatch
import java.util.zip.ZipFile
import javax.annotation.WillNotClose

/**
 * @author Christian Schlichtherle
 */
trait JarDiffITContext {

  def withJarDiff[A](fun: JarDiff => A) =
    withJars { (firstJarFile, secondJarFile) =>
      fun(new JarDiff.Builder()
        .firstJarFile(firstJarFile)
        .secondJarFile(secondJarFile)
        .messageDigest(digest)
        .build)
    }

  def withJarPatch[A](@WillNotClose jarDiffFile: ZipFile)(fun: JarPatch => A) =
    withJars { (inputJarFile, unused) =>
      fun(new JarPatch.Builder()
        .jarDiffFile(jarDiffFile)
        .inputJarFile(inputJarFile)
        .build)
    }

  def withJars[A](fun: (JarFile, JarFile) => A) = {
    val firstJarFile = this firstJarFile ()
    try {
      val secondJarFile = this secondJarFile ()
      try {
        fun(firstJarFile, secondJarFile)
      } finally {
        secondJarFile close ()
      }
    } finally {
      firstJarFile close ()
    }
  }

  @CreatesObligation def firstJarFile() = new JarFile(file("test1.jar"))
  @CreatesObligation def secondJarFile() = new JarFile(file("test2.jar"))

  private def file(resourceName: String) =
    new File((classOf[JarDiffITContext] getResource resourceName).toURI)

  def digest = MessageDigests.sha1

  def store = new MemoryStore
}
