/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.spec.tx

import java.util.logging._
import net.java.trueupdate.manager.spec.tx.Transactions._
import org.junit.runner.RunWith
import org.mockito._
import org.mockito.Mockito._
import org.mockito.Matchers._
import org.mockito.internal.matchers.VarargMatcher
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar.mock

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
final class TransactionsTest extends WordSpec {

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
    val logger = mock[Logger]
    val tx = spy(new SlowTransaction)
    val ttx = time(tx, "slow transaction",
      new LoggerConfig { def logger = fixture.logger })

    when(logger isLoggable any[Level]) thenReturn true

    def verifyLogger(io: InOrder, method: Method, succeeded: Boolean) {
      val level = if (succeeded) method.succeeded else method.failed
      val matches = new ArgumentMatcher[Array[AnyRef]] with VarargMatcher {
        def matches(argument: AnyRef) = {
          val args = argument.asInstanceOf[Array[AnyRef]]
          args.length == 6 &&
          args(0).asInstanceOf[java.lang.Integer] == (if (succeeded) 0 else 1) &&
          args(1).asInstanceOf[java.lang.Integer] == method.ordinal &&
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

  def success = true
  def failure = false

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
            verifyLogger(io, Method.prepare, success)
            verify(tx) perform ()
            verifyLogger(io, Method.perform, success)
            verify(tx) commit ()
            verifyLogger(io, Method.commit, success)
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
            verifyLogger(io, Method.prepare, success)
            verify(tx) perform ()
            verifyLogger(io, Method.perform, failure)
            verify(tx) rollback ()
            verifyLogger(io, Method.rollback, success)
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
  override def commit() { Thread.sleep(1) }
  def rollback() { Thread.sleep(1) }
}
