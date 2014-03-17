/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.core.tx

import java.io._
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers._
import net.java.trueupdate.manager.spec.tx.Commands

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class CopyFileCommandIT extends FileCommandTestSuite {

  def tx(oneByte: File, notExists: File) =
    new CopyFileCommand(oneByte, notExists)

  "A copy file transaction" when {
    "executing successfully" should {
      "have copied the null byte to the destination file" in {
        setUpAndLoan { (file1, file2, tx) =>
          Commands execute tx
          file1.length should be (1)
          file2.length should be (1)
        }
      }
    }
  }
}
