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

/**
 * @author Christian Schlichtherle
 */
trait JarDiffITContext {

  def withJarDiff[A](fun: JarDiff => A) =
    withJars { (jarFile1, jarFile2) =>
      fun(new JarDiff.Builder()
        .jarFile1(jarFile1)
        .jarFile2(jarFile2)
        .messageDigest(digest)
        .build)
    }

  def withJarPatch[A](diff: ZipFile)(fun: JarPatch => A) =
    withJars { (jarFile1, jarFile2) =>
      fun(new JarPatch.Builder()
        .jarDiffFile(diff)
        .inputJarFile(jarFile1)
        .build)
    }

  def withJars[A](fun: (JarFile, JarFile) => A) = {
    val jarFile1 = this jarFile1 ()
    try {
      val jarFile2 = this jarFile2 ()
      try {
        fun(jarFile1, jarFile2)
      } finally {
        jarFile2 close ()
      }
    } finally {
      jarFile1 close ()
    }
  }

  @CreatesObligation def jarFile1() = new JarFile(file("test1.jar"))
  @CreatesObligation def jarFile2() = new JarFile(file("test2.jar"))

  private def file(resourceName: String) =
    new File((classOf[JarDiffITContext] getResource resourceName).toURI)

  def digest = MessageDigests.sha1

  def store = new MemoryStore
}
