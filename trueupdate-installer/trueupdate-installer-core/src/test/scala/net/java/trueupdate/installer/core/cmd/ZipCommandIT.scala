/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.core.cmd

import java.io._
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.matchers.ShouldMatchers._
import net.java.trueupdate.manager.spec.cmd.Commands

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class ZipCommandIT extends FileCommandTestSuite {

  // Mind the parameter swap!
  def cmd(oneByte: File, notExists: File) =
    new ZipCommand(notExists, oneByte, oneByte.getName)

  "A ZipCommand" when {
    "executing successfully" should {
      "have zipped the source file" in {
        setUpAndLoan { (oneByte, notExists, cmd) =>
          Commands execute cmd
          oneByte.length should be (1)
          notExists.length should be > (1L)
        }
      }
    }
  }
}
