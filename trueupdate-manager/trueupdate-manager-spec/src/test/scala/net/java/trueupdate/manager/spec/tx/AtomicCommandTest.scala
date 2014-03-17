/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.spec.tx

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.mockito.Mockito._
import org.scalatest.WordSpec
import org.scalatest.mock.MockitoSugar.mock
import scala._

/** @author Christian Schlichtherle */
@RunWith(classOf[JUnitRunner])
class AtomicCommandTest extends WordSpec {

  def fixture = new {
    val delegate = mock[Command]
    val command = Commands atomic delegate
  }

  "An atomic command" when {

    "executing successfully" should {
      val f = fixture
      import f._
      Commands execute command

      "call perform once" in {
        verify(delegate) perform ()
      }

      "never call revert" in {
        verify(delegate, never) revert ()
      }

      "not be idempotent" in {
        intercept[IllegalStateException] { Commands execute command }
      }
    }

    "failing to perform" should {
      val f = fixture
      import f._
      doThrow (new UnsupportedOperationException) when delegate perform ()
      intercept[UnsupportedOperationException] { Commands execute command }

      "call perform once" in {
        verify(delegate) perform ()
      }

      "never call revert" in {
        verify(delegate, never) revert ()
      }

      "be restartable" in {
        doNothing when delegate perform ()
        Commands execute command
      }
    }

    "participating in a composite transaction" which {
      val f = fixture
      import f._
      val nextCommand = mock[Command]
      val composition = new CompositeCommand(command, nextCommand)

      "subsequently fails" should {
        doThrow (new UnsupportedOperationException) when nextCommand perform ()
        intercept[UnsupportedOperationException] { Commands execute composition }

        "call perform and revert once in order" in {
          val io = inOrder(delegate)
          import io._
          verify(delegate) perform ()
          verify(delegate) revert ()
        }

        "be restartable" in {
          doNothing when nextCommand perform ()
          Commands execute composition
        }
      }
    }
  }
}
