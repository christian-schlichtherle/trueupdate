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
      import f._
      Transactions execute ctx

      "prepare, perform and commit the transactions in order" in {
        val io = inOrder(tx1, tx2)
        import io._
        verify(tx1) prepare ()
        verify(tx1) perform ()
        verify(tx2) prepare ()
        verify(tx2) perform ()
        verify(tx2) commit ()
        verify(tx1) commit ()
      }

      "never rollback any transaction" in {
        for (tx <- txs) verify(tx, never) rollback ()
      }

      "be reusable" in {
        Transactions execute f.ctx
      }
    }

    "failing to prepare the last transaction" should {
      val f = fixture
      import f._
      doThrow (new Exception) when tx2 prepare ()
      failWithNonTransactionException(ctx)

      "prepare, perform and rollback the transactions in order" in {
        val io = inOrder(tx1, tx2)
        import io._
        verify(tx1) prepare ()
        verify(tx1) perform ()
        verify(tx2) prepare ()
        verify(tx2, never) perform ()
        verify(tx2, never) rollback ()
        verify(tx1) rollback ()
      }

      "never commit any transaction" in {
        for (tx <- txs) verify(tx, never) commit ()
      }

      "be retryable" in {
        doNothing when tx2 prepare ()
        Transactions execute ctx
      }
    }

    "failing to perform the last transaction" should {
      val f = fixture
      import f._
      doThrow (new Exception) when tx2 perform ()
      failWithNonTransactionException(ctx)

      "prepare, perform and rollback the transactions in order" in {
        val io = inOrder(tx1, tx2)
        import io._
        verify(tx1) prepare ()
        verify(tx1) perform ()
        verify(tx2) prepare ()
        verify(tx2) perform ()
        verify(tx2) rollback ()
        verify(tx1) rollback ()
      }

      "never commit any transaction" in {
        for (tx <- txs) verify(tx, never) commit ()
      }

      "be retryable" in {
        doNothing when tx2 perform ()
        Transactions execute ctx
      }
    }

    "failing to perform and rollback the last transaction" should {
      val f = fixture
      import f._
      doThrow (new Exception) when tx2 perform ()
      doThrow (new Exception) when tx2 rollback ()
      intercept[TransactionException] { Transactions execute ctx }

      "prepare, perform and rollback the transactions in order" in {
        val io = inOrder(tx1, tx2)
        import io._
        verify(tx1) prepare ()
        verify(tx1) perform ()
        verify(tx2) prepare ()
        verify(tx2) perform ()
        verify(tx2) rollback ()
        verify(tx1, never) rollback ()
      }

      "never commit any transaction" in {
        for (tx <- txs) verify(tx, never) commit ()
      }

      "not be retryable" in {
        doNothing when tx2 perform ()
        doNothing when tx2 rollback ()
        intercept[IllegalStateException] { Transactions execute ctx }
      }
    }

    "failing to commit the last transaction" should {
      val f = fixture
      import f._
      doThrow (new Exception) when tx2 commit ()
      intercept[TransactionException] { Transactions execute ctx }

      "prepare, perform and commit the transactions in order" in {
        val io = inOrder(tx1, tx2)
        import io._
        verify(tx1) prepare ()
        verify(tx1) perform ()
        verify(tx2) prepare ()
        verify(tx2) perform ()
        verify(tx2) commit ()
        verify(tx1, never) commit ()
      }

      "never rollback any transaction" in {
        for (tx <- txs) verify(tx, never) rollback ()
      }

      "not be retryable" in {
        doNothing when tx2 commit ()
        intercept[IllegalStateException] { Transactions execute ctx }
      }
    }
  }
}
