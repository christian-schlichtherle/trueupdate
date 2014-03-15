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

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class AtomicMethodsTransactionTest extends WordSpec {

  "An atomic methods transaction" when {

    "executing successfully" should {
      val tx = new TestTransaction
      import tx._
      Transactions execute tx

      "call the atomic variants of prepare, perform and commit in order" in {
        val io = inOrder(delegate)
        import io._
        verify(delegate) prepare ()
        verify(delegate) perform ()
        verify(delegate) commit ()
      }

      "never rollback" in {
        verify(delegate, never) rollback ()
      }

      "not be retryable" in {
        intercept[Exception] { Transactions execute tx }
      }
    }

    "failing to perform" should {
      val tx = new TestTransaction
      import tx._
      doThrow (new UnsupportedOperationException) when delegate perform ()
      intercept[UnsupportedOperationException] { Transactions execute tx }

      "call the atomic variants of prepare and perform in order" in {
        val io = inOrder(delegate)
        import io._
        verify(delegate) prepare ()
        verify(delegate) perform ()
      }

      "never rollback or commit" in {
        verify(delegate, never) rollback ()
        verify(delegate, never) commit ()
      }

      "be retryable" in {
        doNothing when delegate perform ()
        Transactions execute tx
      }
    }

    "participating in a composite transaction" which {
      val tx1 = new TestTransaction
      import tx1._
      val tx2 = mock[Transaction]
      val ctx = new CompositeTransaction(tx1, tx2)

      "subsequently fails" should {
        doThrow (new UnsupportedOperationException) when tx2 perform ()
        intercept[UnsupportedOperationException] { Transactions execute ctx }

        "call the atomic variants of prepare, perform and rollback in order" in {
          val io = inOrder(delegate)
          import io._
          verify(delegate) prepare ()
          verify(delegate) perform ()
          verify(delegate) rollback ()
        }

        "never commit" in {
          verify(delegate, never) commit ()
        }

        "be retryable" in {
          doNothing when tx2 perform ()
          Transactions execute ctx
        }
      }
    }
  }
}

class TestTransaction extends AtomicMethodsTransaction {

  val delegate = mock[Transaction]

  override def prepareAtomic() { delegate prepare () }
  override def performAtomic() { delegate perform () }
  override def commitAtomic() { delegate commit () }
  override def rollbackAtomic() { delegate rollback () }
}
