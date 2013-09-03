/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.core.tx

import java.io._
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers._
import net.java.trueupdate.manager.core.io.Files._
import net.java.trueupdate.core.zip.diff.ZipDiff
import net.java.trueupdate.core.io._
import java.util.zip.ZipFile
import net.java.trueupdate.manager.core.io.{FileTask, Files}
import org.scalatest.mock.MockitoSugar._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar.mock
import org.scalatest.WordSpec

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class PatchZipTransactionIT extends WordSpec {

  def setUpAndLoan[A](fun: (File, File, File, Transaction) => A) = {
    Files.loanTempFile(new FileTask[A, Exception] {
      override def execute(inputArchive: File) = {
        Files.loanTempFile(new FileTask[A, Exception] {
          override def execute(patchArchive: File) = {
            Files.loanTempFile(new FileTask[A, Exception] {
              override def execute(notExists: File) = {
                Sinks execute new OutputTask[Unit, IOException] {
                  def execute(out: OutputStream) { out write 0 }
                } on new FileStore(patchArchive)
                zip(inputArchive, patchArchive)
                ZipSources execute new ZipInputTask[Unit, IOException] {
                  def execute(archive1: ZipFile) {
                    ZipSources execute new ZipInputTask[Unit, IOException] {
                      def execute(archive2: ZipFile) {
                        ZipDiff
                          .builder
                          .input1(archive1)
                          .input2(archive2)
                          .build
                          .diffTo(new FileStore(patchArchive))
                      }
                    } on new ZipFile(inputArchive)
                  }
                } on new ZipFile(inputArchive)
                inputArchive.length should be > (1L)
                patchArchive.length should be > (1L)
                notExists delete ()
                notExists.exists should be (false)
                fun(inputArchive, patchArchive, notExists,
                  new PatchZipTransaction(inputArchive, patchArchive, notExists,
                                          false))
              }
            }, "output")
          }
        }, "patch")
      }
    }, "input")
  }

  "A patch zip transaction" when {

    "executing successfully" should {
      "have patched the input archive to the output archive using the patch archive" in {
        setUpAndLoan { (inputArchive, patchArchive, notExists, tx) =>
          Transactions execute tx
          inputArchive.length should be > (1L)
          patchArchive.length should be > (1L)
          notExists.length should be (inputArchive.length)
        }
      }
    }

    "failing" should {
      "leave the source and destination files unmodified" in {
        setUpAndLoan { (inputArchive, patchArchive, notExists, tx) =>
          inputArchive delete ()
          intercept[IOException] { Transactions execute tx }
          inputArchive.exists should be (false)
          patchArchive.length should be > (1L)
          notExists.exists should be (false)
        }
      }
    }

    "participating in a composite transaction" which {
      "subsequently fails" should {
        "leave the source and destination files unmodified" in {
          setUpAndLoan { (inputArchive, patchArchive, notExists, tx1) =>
            val tx2 = mock[Transaction]
            val ctx = new CompositeTransaction(tx1, tx2)
            doThrow(new Exception) when(tx2) perform ()
            intercept[Exception] { Transactions execute ctx }
            inputArchive.length should be > (1L)
            patchArchive.length should be > (1L)
            notExists.exists should be (false)
          }
        }
      }
    }
  }
}
