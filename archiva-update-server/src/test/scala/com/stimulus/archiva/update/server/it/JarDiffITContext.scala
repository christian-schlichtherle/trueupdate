/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.it

import com.stimulus.archiva.update.core.io.MemoryStore
import com.stimulus.archiva.update.server.jar.diff.JarDiff.Builder
import com.stimulus.archiva.update.server.util.MessageDigests
import edu.umd.cs.findbugs.annotations.CreatesObligation
import java.io.File
import java.util.jar.JarFile
import com.stimulus.archiva.update.server.jar.JarContext
import com.stimulus.archiva.update.server.jar.diff.JarDiff

/**
 * @author Christian Schlichtherle
 */
trait JarDiffITContext {

  def withJarDiff[A](fun: JarDiff => A) = {
    withJars { (jar1, jar2) => fun(
      new JarContext()
        .diff
          .jarFile1(jar1)
          .jarFile2(jar2)
          .messageDigest(digest)
          .patchFileSink(store)
          .build)
    }
  }

  def withJars[A](fun: (JarFile, JarFile) => A) = {
    val jar1 = jarFile1()
    try {
      val jar2 = jarFile2()
      try {
        fun(jar1, jar2)
      } finally {
        jar2 close ()
      }
    } finally {
      jar1 close ()
    }
  }

  @CreatesObligation def jarFile1() = new JarFile(file("test1.jar"))
  @CreatesObligation def jarFile2() = new JarFile(file("test2.jar"))

  private def file(resourceName: String) =
    new File((classOf[JarDiffITContext] getResource resourceName).toURI)

  def digest = MessageDigests.sha1

  def store = new MemoryStore
}
