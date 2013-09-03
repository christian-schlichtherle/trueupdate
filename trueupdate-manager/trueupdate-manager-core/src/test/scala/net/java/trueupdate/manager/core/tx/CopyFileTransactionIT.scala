/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.core.tx

import java.io._
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers._

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class CopyFileTransactionIT extends FileTransactionITSuite {

  def tx(oneByte: File, notExists: File) =
    new CopyFileTransaction(oneByte, notExists)

  "A copy file transaction" when {
    "executing successfully" should {
      "have copied the null byte to the destination file" in {
        setUpAndLoan { (file1, file2, tx) =>
          Transactions execute tx
          file1.length should be (1)
          file2.length should be (1)
        }
      }
    }
  }
}
