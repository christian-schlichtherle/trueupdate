/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.core.tx

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.mockito.Mockito._
import org.scalatest.WordSpec
import org.scalatest.mock.MockitoSugar.mock

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class CompositeTransactionTest extends WordSpec {

  def fixture = new {
    val tx1 = mock[Transaction]
    val tx2 = mock[Transaction]
    val txs = Array(tx1, tx2)
    val ctx = new CompositeTransaction(txs: _*)
  }

  def failWithNonTransactionException(tx: Transaction) {
    try { Transactions execute tx }
    catch {
      case error: TransactionException => throw error
      case expected: Exception =>
    }
  }

  "A composite transaction" when {

    "executing successfully" should {
      val f = fixture
      Transactions execute f.ctx

      "prepare, perform and commit the transactions in order" in {
        val io = inOrder(f.tx1, f.tx2)
        io verify f.tx1 prepare ()
        io verify f.tx1 perform ()
        io verify f.tx2 prepare ()
        io verify f.tx2 perform ()
        io verify f.tx2 commit ()
        io verify f.tx1 commit ()
      }

      "never rollback any transaction" in {
        for (tx <- f.txs) verify(tx, never) rollback ()
      }

      "be reusable" in {
        Transactions execute f.ctx
      }
    }

    "failing to prepare the last transaction" should {
      val f = fixture
      doThrow (new Exception) when f.tx2 prepare ()
      failWithNonTransactionException(f.ctx)

      "prepare, perform and rollback the transactions in order" in {
        val io = inOrder(f.tx1, f.tx2)
        io verify f.tx1 prepare ()
        io verify f.tx1 perform ()
        io verify f.tx2 prepare ()
        io verify (f.tx2, never) perform ()
        io verify (f.tx2, never) rollback ()
        io verify f.tx1 rollback ()
      }

      "never commit any transaction" in {
        for (tx <- f.txs) verify(tx, never) commit ()
      }

      "be retryable" in {
        doNothing when f.tx2 prepare ()
        Transactions execute f.ctx
      }
    }

    "failing to perform the last transaction" should {
      val f = fixture
      doThrow (new Exception) when f.tx2 perform ()
      failWithNonTransactionException(f.ctx)

      "prepare, perform and rollback the transactions in order" in {
        val io = inOrder(f.tx1, f.tx2)
        io verify f.tx1 prepare ()
        io verify f.tx1 perform ()
        io verify f.tx2 prepare ()
        io verify f.tx2 perform ()
        io verify f.tx2 rollback ()
        io verify f.tx1 rollback ()
      }

      "never commit any transaction" in {
        for (tx <- f.txs) verify(tx, never) commit ()
      }

      "be retryable" in {
        doNothing when f.tx2 perform ()
        Transactions execute f.ctx
      }
    }

    "failing to perform and rollback the last transaction" should {
      val f = fixture
      doThrow (new Exception) when f.tx2 perform ()
      doThrow (new Exception) when f.tx2 rollback ()
      intercept[TransactionException] { Transactions execute f.ctx }

      "prepare, perform and rollback the transactions in order" in {
        val io = inOrder(f.tx1, f.tx2)
        io verify f.tx1 prepare ()
        io verify f.tx1 perform ()
        io verify f.tx2 prepare ()
        io verify f.tx2 perform ()
        io verify f.tx2 rollback ()
        io verify (f.tx1, never) rollback ()
      }

      "never commit any transaction" in {
        for (tx <- f.txs) verify(tx, never) commit ()
      }

      "not be retryable" in {
        doNothing when f.tx2 perform ()
        doNothing when f.tx2 rollback ()
        intercept[IllegalStateException] { Transactions execute f.ctx }
      }
    }

    "failing to commit the last transaction" should {
      val f = fixture
      doThrow (new Exception) when f.tx2 commit ()
      intercept[TransactionException] { Transactions execute f.ctx }

      "prepare, perform and commit the transactions in order" in {
        val io = inOrder(f.tx1, f.tx2)
        io verify f.tx1 prepare ()
        io verify f.tx1 perform ()
        io verify f.tx2 prepare ()
        io verify f.tx2 perform ()
        io verify f.tx2 commit ()
        io verify (f.tx1, never) commit ()
      }

      "never rollback any transaction" in {
        for (tx <- f.txs) verify(tx, never) rollback ()
      }

      "not be retryable" in {
        doNothing when f.tx2 commit ()
        intercept[IllegalStateException] { Transactions execute f.ctx }
      }
    }
  }
}
