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
    loanArchives { (archive1, archive2) =>
      fun(ZipDiff.builder
        .input1(archive1)
        .input2(archive2)
        .digest(digest)
        .build)
    }

  def loanZipPatch[A](@WillNotClose patchArchive: ZipFile)(fun: ZipPatch => A) = {
    class FunTask extends ZipInputTask[A, Exception] {
      override def execute(inputArchive: ZipFile) = {
        fun(ZipPatch.builder
          .input(inputArchive)
          .diff(patchArchive)
          .createJar(true)
          .build)
      }
    }

    ZipSources execute new FunTask on new TestJar1Source
  }

  def loanArchives[A](fun: (ZipFile, ZipFile) => A) = {
    class Fun1Task extends ZipInputTask[A, Exception]() {
      override def execute(archive1: ZipFile) = {
        class Fun2Task extends ZipInputTask[A, Exception] {
          override def execute(archive2: ZipFile) = {
            fun(archive1, archive2)
          }
        }

        ZipSources execute new Fun2Task on new TestJar2Source
      }
    }

    ZipSources execute new Fun1Task on new TestJar1Source
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
