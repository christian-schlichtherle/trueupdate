/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.spec.cmd

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.mockito.Mockito._
import org.scalatest.WordSpec
import org.scalatest.mock.MockitoSugar.mock
import scala._

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class CompositeCommandTest extends WordSpec {

  def fixture = new {
    val cmd1 = mock[Command]
    val cmd2 = mock[Command]
    val cmds = Array(cmd1, cmd2)
    val ccmd = new CompositeCommand(cmds: _*)
  }

  "A CompositeCommand" when {

    "executing successfully" should {
      val f = fixture
      import f._
      Commands execute ccmd

      "call perform once in order" in {
        val io = inOrder(cmd1, cmd2)
        import io._
        verify(cmd1) perform ()
        verify(cmd2) perform ()
      }

      "never call revert" in {
        for (cmd <- cmds) verify(cmd, never) revert ()
      }

      "not be idempotent" in {
        intercept[IllegalStateException] { Commands execute ccmd }
      }
    }

    "failing to perform the last command" should {
      val f = fixture
      import f._
      doThrow (new UnsupportedOperationException) when cmd2 perform ()
      intercept[UnsupportedOperationException] { Commands execute ccmd }

      "perform and revert the commands in order" in {
        val io = inOrder(cmd1, cmd2)
        import io._
        verify(cmd1) perform ()
        verify(cmd2) perform ()
        verify(cmd2) revert ()
        verify(cmd1) revert ()
      }

      "be restartable" in {
        doNothing when cmd2 perform ()
        Commands execute ccmd
      }
    }

    "failing to perform the last command and revert the first command" should {
      val f = fixture
      import f._
      doThrow (new UnsupportedOperationException()) when cmd2 perform ()
      doThrow (new RuntimeException) when cmd1 revert ()
      intercept[UnsupportedOperationException] { Commands execute ccmd }

      "perform and revert the commands in order" in {
        val io = inOrder(cmd1, cmd2)
        import io._
        verify(cmd1) perform ()
        verify(cmd2) perform ()
        verify(cmd2) revert ()
        verify(cmd1) revert ()
      }

      "not be restartable" in {
        intercept[IllegalStateException] { Commands execute ccmd }
      }
    }
  }
}
