/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.spec.cmd

import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar.mock
import LogContext.Method._
import org.mockito.{ArgumentMatcher, Matchers}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.prop.PropertyChecks._
import org.hamcrest.{CoreMatchers, Matcher}

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class CommandsTest extends WordSpec {

  "The execute function" when {
    "executing a nested command" should {
      "throw an UnsupportedOperationException" in {
        intercept[UnsupportedOperationException] {
          Commands execute new Command {
            def perform() { Commands execute this }
            def revert() { }
          }
        }
      }
    }
  }

  def nonNegativeLong = longThat(new ArgumentMatcher[java.lang.Long] {
    override def matches(argument: Any) =
      0 <= argument.asInstanceOf[java.lang.Long]
  })

  "The time function" when {
    "given a time context and a command" should {
      "log the start and success events" in {
        forAll(Table("method", perform, revert)) { method =>
          val cmd = mock[Command]
          val ctx = mock[LogContext]
          method invoke (Commands time (cmd, ctx))
          val io = inOrder(ctx, cmd)
          import io._
          verify(ctx) logStarting method
          if (method == perform) verify(cmd) perform ()
          else verify(cmd) revert ()
          verify(ctx) logSucceeded (Matchers.eq(method), nonNegativeLong)
          verifyNoMoreInteractions()
        }
      }

      "log the start and failure events" in {
        forAll(Table("method", perform, revert)) { method =>
          val cmd = mock[Command]
          val ctx = mock[LogContext]
          when(cmd perform ()) thenThrow new UnsupportedOperationException
          when(cmd revert ()) thenThrow new UnsupportedOperationException
          intercept[UnsupportedOperationException] {
            method invoke (Commands time (cmd, ctx))
          }
          val io = inOrder(ctx, cmd)
          import io._
          verify(ctx) logStarting method
          if (method == perform) verify(cmd) perform ()
          else verify(cmd) revert ()
          verify(ctx) logFailed (Matchers.eq(method), nonNegativeLong)
          verifyNoMoreInteractions()
        }
      }
    }
  }
}
