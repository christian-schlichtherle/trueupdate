/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.it

import com.stimulus.archiva.update.core.io._
import com.stimulus.archiva.update.server.jar.diff.ZipDiff
import com.stimulus.archiva.update.server.util.MessageDigests
import edu.umd.cs.findbugs.annotations.CreatesObligation
import java.io.File
import java.util.jar.JarFile
import com.stimulus.archiva.update.server.jar.patch.ZipPatch
import java.util.zip.ZipFile
import javax.annotation.WillNotClose

/**
 * @author Christian Schlichtherle
 */
trait ZipDiffITContext {

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
    new File((classOf[ZipDiffITContext] getResource resourceName).toURI)

  def digest = MessageDigests.sha1

  def store = new MemoryStore
}
