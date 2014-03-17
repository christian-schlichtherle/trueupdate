/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.core.cmd

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers._
import java.io._
import net.java.trueupdate.manager.spec.cmd.Commands

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class RenamePathCommandIT extends FileCommandTestSuite {

  def cmd(oneByte: File, notExists: File) =
    new RenamePathCommand(oneByte, notExists)

  "A RenamePathCommand" when {
    "executing successfully" should {
      "have renamed the source file to the destination file" in {
        setUpAndLoan { (oneByte, notExists, cmd) =>
          Commands execute cmd
          oneByte.exists should be (false)
          notExists.length should be (1)
        }
      }
    }
  }
}
