/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.core.tx

import java.io._
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers._
import net.java.trueupdate.installer.core.io.Files._

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class UnzipTransactionIT extends FileTransactionITSuite {

  def tx(oneByte: File, notExists: File) = {
    zip(notExists, oneByte, oneByte.getName)
    deletePath(oneByte)
    renamePath(notExists, oneByte)
    new UnzipTransaction(oneByte, notExists)
  }

  "An unzip transaction" when {
    "executing successfully" should {
      "have unzipped the ZIP file" in {
        setUpAndLoan { (oneByte, notExists, tx) =>
          Transactions execute tx
          oneByte.length should be > (1L)
          notExists.isDirectory should be (true)
          new File(notExists, oneByte.getName).isFile should be (true)
        }
      }
    }
  }
}
