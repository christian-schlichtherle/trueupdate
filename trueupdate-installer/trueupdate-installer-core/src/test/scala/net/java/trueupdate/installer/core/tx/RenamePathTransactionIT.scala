/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.core.tx

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers._
import java.io._

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class RenamePathTransactionIT extends FileTransactionITSuite {

  def tx(oneByte: File, notExists: File) =
    new RenamePathTransaction(oneByte, notExists)

  "A rename path transaction" when {
    "executing successfully" should {
      "have renamed the source file to the destination file" in {
        setUpAndLoan { (oneByte, notExists, tx) =>
          Transactions execute tx
          oneByte.exists should be (false)
          notExists.length should be (1)
        }
      }
    }
  }
}
