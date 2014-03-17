/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.core.cmd

import java.io._
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers._
import net.java.trueupdate.installer.core.io.Files._
import net.java.trueupdate.manager.spec.cmd.Commands

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class UnzipCommandIT extends FileCommandTestSuite {

  def cmd(oneByte: File, notExists: File) = {
    zip(notExists, oneByte, oneByte.getName)
    deletePath(oneByte)
    renamePath(notExists, oneByte)
    new UnzipCommand(oneByte, notExists)
  }

  "An UnzipCommand" when {
    "executing successfully" should {
      "have unzipped the ZIP file" in {
        setUpAndLoan { (oneByte, notExists, cmd) =>
          Commands execute cmd
          oneByte.length should be > (1L)
          notExists.isDirectory should be (true)
          new File(notExists, oneByte.getName).isFile should be (true)
        }
      }
    }
  }
}
