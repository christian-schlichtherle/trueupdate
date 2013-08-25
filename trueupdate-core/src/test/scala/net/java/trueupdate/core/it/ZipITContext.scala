/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.it

import edu.umd.cs.findbugs.annotations.CreatesObligation
import java.io.File
import java.util.jar.JarFile
import java.util.zip.ZipFile
import javax.annotation.WillNotClose
import net.java.trueupdate.core.TestContext
import net.java.trueupdate.core.io.MessageDigests
import net.java.trueupdate.core.zip.model.DiffModel
import net.java.trueupdate.core.zip.diff.ZipDiff
import net.java.trueupdate.core.zip.patch.ZipPatch

/**
 * @author Christian Schlichtherle
 */
trait ZipITContext extends TestContext {

  def loanZipDiff[A](fun: ZipDiff => A) =
    loanJarFiles { (jarFile1, jarFile2) =>
      fun(ZipDiff.builder
        .file1(jarFile1)
        .file2(jarFile2)
        .digest(digest)
        .build)
    }

  def loanZipPatch[A](@WillNotClose zipPatchFile: ZipFile)(fun: ZipPatch => A) = {
    var ex: Throwable = null
    val inputJarFile = testJar1()
    try {
      fun(ZipPatch.builder
        .inputFile(inputJarFile)
        .patchFile(zipPatchFile)
        .createJarFile(true)
        .build)
    } catch {
      case ex2: Throwable => ex = ex2; throw ex
    } finally {
      try {
        inputJarFile close ()
      } catch {
        case ex2: Throwable => if (null == ex) throw ex2
      }
    }
  }

  def loanJarFiles[A](fun: (JarFile, JarFile) => A) = {
    var ex: Throwable = null
    val jarFile1 = testJar1()
    try {
      val jarFile2 = testJar2()
      try {
        fun(jarFile1, jarFile2)
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
  }

  @CreatesObligation def testJar1() = new JarFile(file("test1.jar"), false)
  @CreatesObligation def testJar2() = new JarFile(file("test2.jar"), false)

  private def file(resourceName: String) =
    new File((classOf[ZipITContext] getResource resourceName).toURI)

  def digest = MessageDigests.sha1

  override lazy val jaxbContext = DiffModel.jaxbContext
}
