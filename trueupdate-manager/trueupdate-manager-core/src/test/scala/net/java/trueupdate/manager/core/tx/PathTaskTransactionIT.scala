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
import net.java.trueupdate.manager.core.io.{FileTask, Files}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar.mock
import org.scalatest.WordSpec
import net.java.trueupdate.core.zip.patch.ZipPatch

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class PathTaskTransactionIT extends WordSpec {

  def setUpAndLoan[A](fun: (File, File, File, Transaction) => A) = {
    Files.loanTempFile(new FileTask[A, Exception] {
      override def execute(input: File) = {
        Files.loanTempFile(new FileTask[A, Exception] {
          override def execute(diff: File) = {
            Files.loanTempFile(new FileTask[A, Exception] {
              override def execute(output: File) = {
                Sinks execute new OutputTask[Unit, IOException] {
                  def execute(out: OutputStream) { out write 0 }
                } on new FileStore(diff)
                zip(input, diff)
                ZipDiff.builder.input1(input).input2(input).build.output(diff)
                input.length should be > (1L)
                diff.length should be > (1L)
                output delete ()
                output.exists should be (false)
                val task = ZipPatch
                  .builder
                  .input(input)
                  .diff(diff)
                  .createJar(false)
                  .build
                  .bindTo(output)
                fun(input, diff, output,
                  new PathTaskTransaction(output, task))
              }
            }, "output")
          }
        }, "diff")
      }
    }, "input")
  }

  "A zip patch transaction" when {

    "executing successfully" should {
      "have patched the input archive to the output archive using the patch archive" in {
        setUpAndLoan { (inputArchive, diffArchive, notExists, tx) =>
          Transactions execute tx
          inputArchive.length should be > (1L)
          diffArchive.length should be > (1L)
          notExists.length should be (inputArchive.length)
        }
      }
    }

    "failing" should {
      "leave the source and destination files unmodified" in {
        setUpAndLoan { (inputArchive, diffArchive, notExists, tx) =>
          inputArchive delete ()
          intercept[IOException] { Transactions execute tx }
          inputArchive.exists should be (false)
          diffArchive.length should be > (1L)
          notExists.exists should be (false)
        }
      }
    }

    "participating in a composite transaction" which {
      "subsequently fails" should {
        "leave the source and destination files unmodified" in {
          setUpAndLoan { (inputArchive, diffArchive, notExists, tx1) =>
            val tx2 = mock[Transaction]
            val ctx = new CompositeTransaction(tx1, tx2)
            doThrow(new Exception) when(tx2) perform ()
            intercept[Exception] { Transactions execute ctx }
            inputArchive.length should be > (1L)
            diffArchive.length should be > (1L)
            notExists.exists should be (false)
          }
        }
      }
    }
  }
}
