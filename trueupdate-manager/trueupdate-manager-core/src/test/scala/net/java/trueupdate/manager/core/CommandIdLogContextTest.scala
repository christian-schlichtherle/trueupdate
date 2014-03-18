/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.core

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import net.java.trueupdate.manager.spec.CommandId
import net.java.trueupdate.manager.spec.cmd.LogContext.Method
import java.util.logging.{Level, Logger}

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class CommandIdLogContextTest extends WordSpec {

  "A CommandIdLogContext" should {
    "log a message for all command identifiers" in {
      val className = classOf[CommandIdLogContext].getName
      for (id <- CommandId.values) {
        val ctx = new CommandIdLogContext {
          def commandId = id
          def loggerName = className
        }
        for (method <- Method.values) {
          ctx logStarting method
          ctx logSucceeded (method, 0)
          ctx logFailed (method, 0)
        }
      }
    }
  }
}
