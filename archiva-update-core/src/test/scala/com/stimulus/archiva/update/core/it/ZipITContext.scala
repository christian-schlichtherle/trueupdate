/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.core.it

import com.stimulus.archiva.update.core.digest.MessageDigests
import com.stimulus.archiva.update.core.io._
import edu.umd.cs.findbugs.annotations.CreatesObligation
import java.io.File
import java.util.jar.JarFile
import java.util.zip.ZipFile
import javax.annotation.WillNotClose
import com.stimulus.archiva.update.core.zip.diff.ZipDiff
import com.stimulus.archiva.update.core.zip.patch.ZipPatch

/**
 * @author Christian Schlichtherle
 */
trait ZipITContext {

  def withZipDiff[A](fun: ZipDiff => A) =
    withZipFiles { (firstZipFile, secondZipFile) =>
      fun(new ZipDiff.Builder()
        .firstZipFile(firstZipFile)
        .secondZipFile(secondZipFile)
        .messageDigest(digest)
        .build)
    }

  def withZipPatch[A](@WillNotClose zipPatchFile: ZipFile)(fun: ZipPatch => A) =
    withZipFiles { (inputZipFile, unused) =>
      fun(new ZipPatch.Builder()
        .zipPatchFile(zipPatchFile)
        .inputZipFile(inputZipFile)
        .outputJarFile(true)
        .build)
    }

  def withZipFiles[A](fun: (ZipFile, ZipFile) => A) = {
    val firstZipFile = this firstZipFile ()
    try {
      val secondZipFile = this secondZipFile ()
      try {
        fun(firstZipFile, secondZipFile)
      } finally {
        secondZipFile close ()
      }
    } finally {
      firstZipFile close ()
    }
  }

  @CreatesObligation def firstZipFile() = new JarFile(file("test1.jar"))
  @CreatesObligation def secondZipFile() = new JarFile(file("test2.jar"))

  private def file(resourceName: String) =
    new File((classOf[ZipITContext] getResource resourceName).toURI)

  def digest = MessageDigests.sha1

  def store = new MemoryStore
}
