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
class ZipTransactionIT extends FileTransactionITSuite {

  // Mind the parameter swap!
  def tx(oneByte: File, notExists: File) =
    new ZipTransaction(notExists, oneByte, oneByte.getName)

  "A zip transaction" when {
    "executing successfully" should {
      "have zipped the source file" in {
        setUpAndLoan { (oneByte, notExists, tx) =>
          Transactions execute tx
          oneByte.length should be (1)
          notExists.length should be > (1L)
        }
      }
    }
  }
}
