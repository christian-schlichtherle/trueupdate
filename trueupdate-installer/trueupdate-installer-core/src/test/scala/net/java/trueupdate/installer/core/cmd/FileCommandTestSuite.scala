/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.core.cmd

import java.io._
import net.java.trueupdate.core.io._
import net.java.trueupdate.installer.core.io._
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar.mock
import net.java.trueupdate.manager.spec.cmd._

/**
 * @author Christian Schlichtherle
 */
abstract class FileCommandTestSuite extends WordSpec {

  def cmd(oneByte: File, notExists: File): Command

  def setUpAndLoan[A](fun: (File, File, Command) => A) = {
    Files.loanTempDir(new PathTask[A, Exception] {
      override def execute(tempDir: File) = {
        val oneByte = new File(tempDir, "oneByte")
        Sinks execute new OutputTask[Unit, IOException] {
          def execute(out: OutputStream) { out write 0 }
        } on new FileStore(oneByte)
        oneByte.length should be (1)
        val notExists = new File(tempDir, "notExists")
        fun(oneByte, notExists, cmd(oneByte, notExists))
      }
    }, "dir", null, null)
  }

  "A file command" when {

    "failing to perform" should {
      "leave the source and destination files unmodified" in {
        setUpAndLoan { (file, notExists, cmd) =>
          file delete ()
          intercept[IOException] { Commands execute cmd }
          file.exists should be (false)
          notExists.exists should be (false)
        }
      }
    }

    "participating in a composite command" which {
      "subsequently fails" should {
        "leave the source and destination files unmodified" in {
          setUpAndLoan { (file, notExists, tx1) =>
            val tx2 = mock[Command]
            val ctx = new CompositeCommand(tx1, tx2)
            doThrow(new UnsupportedOperationException) when(tx2) perform ()
            intercept[UnsupportedOperationException] { Commands execute ctx }
            file.isFile should be (true)
            notExists.exists should be (false)
          }
        }
      }
    }
  }
}
