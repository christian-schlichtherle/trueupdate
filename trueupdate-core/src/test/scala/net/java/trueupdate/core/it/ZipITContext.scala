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
import net.java.trueupdate.core.zip.diff.RawZipDiff
import net.java.trueupdate.core.zip.patch.ZipPatch
import net.java.trueupdate.core.zip.{ZipSource, ZipSources, ZipInputTask}
import java.security.MessageDigest

/**
 * @author Christian Schlichtherle
 */
trait ZipITContext extends TestContext {

  def loanRawZipDiff[A](fun: RawZipDiff => A) =
    loanTestJars { (archive1, archive2) =>
      fun(new RawZipDiff {
        override def input1 = archive1
        override def input2 = archive2
        override def digest = ZipITContext.this.digest
      })
    }

  def loanZipPatch[A](@WillNotClose diff: ZipFile)(fun: ZipPatch => A) = {
    class FunTask extends ZipInputTask[A, Exception] {
      override def execute(input: ZipFile) = {
        fun(ZipPatch.builder
          .input(input)
          .diff(diff)
          .createJar(true)
          .build)
      }
    }

    ZipSources execute new FunTask on testJar1
  }

  def loanTestJars[A](fun: (ZipFile, ZipFile) => A) = {
    class Fun1Task extends ZipInputTask[A, Exception]() {
      override def execute(jar1: ZipFile) = {
        class Fun2Task extends ZipInputTask[A, Exception] {
          override def execute(jar2: ZipFile) = {
            fun(jar1, jar2)
          }
        }

        ZipSources execute new Fun2Task on testJar2()
      }
    }

    ZipSources execute new Fun1Task on testJar1()
  }

  final def testJarSource1: ZipSource = new ZipSource {
    override def input() = testJar1()
  }

  final def testJarSource2: ZipSource = new ZipSource {
    override def input() = testJar2()
  }

  @CreatesObligation final def testJar1() = new JarFile(file("test1.jar"), false)
  @CreatesObligation final def testJar2() = new JarFile(file("test2.jar"), false)

  private def file(resourceName: String) =
    new File((classOf[ZipITContext] getResource resourceName).toURI)

  def digest = MessageDigests.sha1

  override lazy val jaxbContext = DiffModel.jaxbContext
}
