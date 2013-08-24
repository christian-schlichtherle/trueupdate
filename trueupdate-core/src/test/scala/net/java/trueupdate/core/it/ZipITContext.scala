/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.it

import Loan._
import edu.umd.cs.findbugs.annotations.CreatesObligation
import java.io.File
import java.util.jar.JarFile
import java.util.zip.ZipFile
import javax.annotation.WillNotClose
import net.java.trueupdate.core.TestContext
import net.java.trueupdate.core.util.MessageDigests
import net.java.trueupdate.core.zip.model.ZipDiffModel
import net.java.trueupdate.core.zip.diff.ZipDiff
import net.java.trueupdate.core.zip.patch.ZipPatch

/**
 * @author Christian Schlichtherle
 */
trait ZipITContext extends TestContext {

  def withZipDiff[A](fun: ZipDiff => A) =
    withZipFiles { (zipFile1, zipFile2) =>
      fun(ZipDiff.builder
        .zipFile1(zipFile1)
        .zipFile2(zipFile2)
        .messageDigest(digest)
        .build)
    }

  def withZipPatch[A](@WillNotClose zipPatchFile: ZipFile)(fun: ZipPatch => A) =
    loan(zipFile1()) to { inputZipFile =>
      fun(ZipPatch.builder
        .zipPatchFile(zipPatchFile)
        .inputZipFile(inputZipFile)
        .outputJarFile(true)
        .build)
    }

  def withZipFiles[A](fun: (ZipFile, ZipFile) => A) =
    loan(zipFile1()) to { firstZipFile =>
      loan(zipFile2()) to { secondZipFile =>
        fun(firstZipFile, secondZipFile)
      }
    }

  @CreatesObligation def zipFile1() = new JarFile(file("test1.jar"), false)
  @CreatesObligation def zipFile2() = new JarFile(file("test2.jar"), false)

  private def file(resourceName: String) =
    new File((classOf[ZipITContext] getResource resourceName).toURI)

  def digest = MessageDigests.sha1

  override lazy val jaxbContext = ZipDiffModel.jaxbContext
}
