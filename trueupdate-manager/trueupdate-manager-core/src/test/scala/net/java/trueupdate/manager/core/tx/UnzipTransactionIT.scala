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

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class UnzipTransactionIT extends FileTransactionITSuite {

  def tx(oneByte: File, notExists: File) =
    new UnzipTransaction(oneByte, notExists)

  "An unzip transaction" when {
    "executing successfully" should {
      "have unzipped the ZIP file" in {
        setUpAndLoan { (oneByte, notExists, tx) =>
          zip(notExists, oneByte, oneByte.getName)
          copyFile(notExists, oneByte)
          deletePath(notExists)
          Transactions execute tx
          oneByte.length should be > (1L)
          notExists.isDirectory should be (true)
          new File(notExists, oneByte.getName).isFile should be (true)
        }
      }
    }
  }
}
