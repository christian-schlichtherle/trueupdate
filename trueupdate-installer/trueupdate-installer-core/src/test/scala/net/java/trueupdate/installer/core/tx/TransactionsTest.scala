/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.core.tx

import org.junit.runner.RunWith
import org.mockito._
import org.mockito.Mockito._
import org.mockito.Matchers._
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers._
import org.scalatest.mock.MockitoSugar.mock
import net.java.trueupdate.installer.core.tx.Transactions._
import org.mockito.internal.matchers.VarargMatcher
import net.java.trueupdate.message.LogMessage.Level
import net.java.trueupdate.manager.spec.UpdateLogger

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class TransactionsTest extends WordSpec {

  "The execute function" when {
    "executing a nested transaction" should {
      "throw an illegal state exception" in {
        intercept[IllegalStateException] {
          Transactions execute new NestedTransaction
        }
      }
    }
  }

  def fixture = new { fixture =>
    val logger = mock[UpdateLogger]
    val tx = spy(new SlowTransaction)
    val ttx = timed("slow transaction", tx,
      new LoggerConfig { def logger = fixture.logger })

    when(logger isLoggable any.asInstanceOf[Level]) thenReturn true

    def verifyLogger(io: InOrder, method: Method, succeeded: Boolean) {
      val level = if (succeeded) method.succeeded else method.failed
      val matches = new ArgumentMatcher[Array[AnyRef]] with VarargMatcher {
        def matches(argument: AnyRef) = {
          val args = argument.asInstanceOf[Array[AnyRef]]
          args.length == 6
          args(0) == (if (succeeded) 0 else 1) &&
          args(1) == method.ordinal &&
          args(2).asInstanceOf[java.lang.Long] >= 0 &&
          args(3).asInstanceOf[java.lang.Long] >= 0 &&
          args(4).asInstanceOf[java.lang.Long] >= 0 &&
          args(5).asInstanceOf[java.lang.Long] >= 0
        }
      }
      io verify logger isLoggable level
      io verify logger log (Matchers.eq(level), anyString, argThat(matches))
    }
  }

  "The timed function" when {
    "called" should {
      "return a wrapper transaction" which {
        "logs the duration of its methods" when {

          "successfully executed" in {
            val f = fixture
            import f._
            Transactions execute ttx

            val io = inOrder(tx, logger)
            import io._
            verify(tx) prepare ()
            verifyLogger(io, Method.prepare, true)
            verify(tx) perform ()
            verifyLogger(io, Method.perform, true)
            verify(tx) commit ()
            verifyLogger(io, Method.commit, true)
            Mockito.verifyNoMoreInteractions(tx)
            Mockito.verifyNoMoreInteractions(logger)
          }

          "failed to perform" in {
            val f = fixture
            import f._
            doThrow(new UnsupportedOperationException) when tx perform ()
            intercept[UnsupportedOperationException] { Transactions execute ttx }

            val io = inOrder(tx, logger)
            import io._
            verify(tx) prepare ()
            verifyLogger(io, Method.prepare, true)
            verify(tx) perform ()
            verifyLogger(io, Method.perform, false)
            verify(tx) rollback ()
            verifyLogger(io, Method.rollback, true)
            Mockito.verifyNoMoreInteractions(tx)
            Mockito.verifyNoMoreInteractions(logger)
          }
        }
      }
    }
  }
}

class NestedTransaction extends Transaction {
  def perform() { Transactions execute this }
  def rollback() { }
}

class SlowTransaction extends Transaction {
  override def prepare() { Thread.sleep(1) }
  def perform() { Thread.sleep(1) }
  def rollback() { Thread.sleep(1) }
  override def commit() { Thread.sleep(1) }
}
