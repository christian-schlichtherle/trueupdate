/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.core.tx

import java.io._
import net.java.trueupdate.core.io._
import net.java.trueupdate.manager.core.io._
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar.mock

/**
 * @author Christian Schlichtherle
 */
abstract class FileTransactionITSuite extends WordSpec {

  def tx(oneByte: File, notExists: File): Transaction

  def setUpAndLoan[A](fun: (File, File, Transaction) => A) = {
    Files.loanTempDir(new PathTask[A, Exception] {
      override def execute(tempDir: File) = {
        val oneByte = new File(tempDir, "oneByte")
        Sinks execute new OutputTask[Unit, IOException] {
          def execute(out: OutputStream) { out write 0 }
        } on new FileStore(oneByte)
        oneByte.length should be (1)
        val notExists = new File(tempDir, "notExists")
        fun(oneByte, notExists, tx(oneByte, notExists))
      }
    }, "dir", null, null)
  }

  "A file transaction" when {

    "failing" should {
      "leave the source and destination files unmodified" in {
        setUpAndLoan { (oneByte, notExists, tx) =>
          oneByte delete ()
          intercept[IOException] { Transactions execute tx }
          oneByte.exists should be (false)
          notExists.exists should be (false)
        }
      }
    }

    "participating in a composite transaction" which {
      "subsequently fails" should {
        "leave the source and destination files unmodified" in {
          setUpAndLoan { (oneByte, notExists, tx1) =>
            val tx2 = mock[Transaction]
            val ctx = new CompositeTransaction(tx1, tx2)
            doThrow(new Exception) when(tx2) perform ()
            intercept[Exception] { Transactions execute ctx }
            oneByte.length should be (1)
            notExists.exists should be (false)
          }
        }
      }
    }
  }
}
