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
import net.java.trueupdate.core.io._
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
    new ZipInputTask[A, Exception](new TestJar1Source) {
      override def execute(inputJarFile: ZipFile) = {
        fun(ZipPatch.builder
          .inputFile(inputJarFile)
          .patchFile(zipPatchFile)
          .createJarFile(true)
          .build)
      }
    } call ()
  }

  def loanJarFiles[A](fun: (JarFile, JarFile) => A) = {
    new ZipInputTask[A, Exception](new TestJar1Source) {
      override def execute(jarFile1: ZipFile) = {
        new ZipInputTask[A, Exception](new TestJar2Source) {
          override def execute(jarFile2: ZipFile) = {
            fun(jarFile1.asInstanceOf[JarFile], jarFile2.asInstanceOf[JarFile])
          }
        } call ()
      }
    } call ()
  }

  final class TestJar1Source extends ZipSource {
    override def input() = testJar1()
  }

  final class TestJar2Source extends ZipSource {
    override def input() = testJar2()
  }

  @CreatesObligation def testJar1() = new JarFile(file("test1.jar"), false)
  @CreatesObligation def testJar2() = new JarFile(file("test2.jar"), false)

  private def file(resourceName: String) =
    new File((classOf[ZipITContext] getResource resourceName).toURI)

  def digest = MessageDigests.sha1

  override lazy val jaxbContext = DiffModel.jaxbContext
}
