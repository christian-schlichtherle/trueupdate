/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.core.tx

import java.io._
import net.java.trueupdate.core.io._
import net.java.trueupdate.core.zip.diff.ZipDiff
import net.java.trueupdate.core.zip.patch.ZipPatch
import net.java.trueupdate.manager.core.io._
import net.java.trueupdate.manager.core.io.Files._
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers._

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class ZipDiffPatchTransactionIT extends WordSpec {

  def setUpAndLoan[A](fun: (File, File, File, File, Transaction) => A) = {
    loanTempFile(new PathTask[A, Exception] {
      override def execute(diff: File) = {
        loanTempFile(new PathTask[A, Exception] {
          override def execute(input1: File) = {

            Sinks execute new OutputTask[Unit, IOException] {
              def execute(out: OutputStream) { out write 0 }
            } on input1
            zip(diff, input1, input1.getName)
            deletePath(input1)
            renamePath(diff, input1)

            loanTempFile(new PathTask[A, Exception] {
              override def execute(input2: File) = {

                Sinks execute new OutputTask[Unit, IOException] {
                  def execute(out: OutputStream) { out write 0; out write 0 }
                } on input2
                zip(diff, input2, input2.getName)
                deletePath(input2)
                renamePath(diff, input2)

                loanTempFile(new PathTask[A, Exception] {
                  override def execute(output: File) = {

                    deletePath(output)

                    input1.isFile should be (true)
                    input1.length should be > (1L)
                    input2.isFile should be (true)
                    input2.length should be > (2L)
                    diff.exists should be (false)
                    output.exists should be (false)

                    val diffTx = new PathTaskTransaction(diff,
                      new PathTask[Unit, IOException] {
                        val diff = ZipDiff
                          .builder
                          .input1(input1)
                          .input2(input2)
                          .build

                        def execute(file: File) { diff.output(file) }
                      })

                    val patchTx = new PathTaskTransaction(output,
                      new PathTask[Unit, IOException] {
                        val patch = ZipPatch
                          .builder
                          .input(input1)
                          .diff(diff)
                          .build

                        def execute(file: File) { patch.output(file) }
                      })

                    fun(input1, input2, diff, output,
                      new CompositeTransaction(diffTx, patchTx))
                  }
                }, "output")
              }
            }, "input2")
          }
        }, "input1")
      }
    }, "diff")
  }

  "A ZIP diff/patch transaction" when {

    "executing successfully" should {
      "have diffed the two input files and patched the first input file to equal the second input file" in {
        setUpAndLoan { (input1, input2, diff, output, tx) =>
          Transactions execute tx
          input1.isFile should be (true)
          input1.length should be > (1L)
          input2.isFile should be (true)
          input2.length should be > (2L)
          diff.isFile should be (true)
          diff.length should be > (0L)
          output.isFile should be (true)
          output.length should be (input2.length)
        }
      }
    }

    "failing to diff because the first input file is missing" should {
      "not have created diff or output files" in {
        setUpAndLoan { (input1, input2, diff, output, tx) =>
          input1 delete ()
          intercept[IOException] { Transactions execute tx }
          input1.exists should be (false)
          input2.isFile should be (true)
          input2.length should be > (2L)
          diff.exists should be (false)
          output.exists should be (false)
        }
      }
    }

    "failing to diff because the second input file is missing" should {
      "not have created diff or output files" in {
        setUpAndLoan { (input1, input2, diff, output, tx) =>
          input2 delete ()
          intercept[IOException] { Transactions execute tx }
          input1.isFile should be (true)
          input1.length should be > (1L)
          input2.exists should be (false)
          diff.exists should be (false)
          output.exists should be (false)
        }
      }
    }

    "failing to patch because the output file already exists" should {
      "not have created the diff file and leave the output file unmodified" in {
        setUpAndLoan { (input1, input2, diff, output, tx) =>
          new FileOutputStream(output) close ()
          intercept[IOException] { Transactions execute tx }
          input1.isFile should be (true)
          input1.length should be > (1L)
          input2.isFile should be (true)
          input2.length should be > (2L)
          diff.exists should be (false)
          output.isFile should be (true)
          output.length should be (0)
        }
      }
    }
  }
}
