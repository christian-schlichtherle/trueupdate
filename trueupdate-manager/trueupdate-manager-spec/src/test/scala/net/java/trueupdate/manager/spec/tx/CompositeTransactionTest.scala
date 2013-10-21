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
class CompositeTransactionTest extends WordSpec {

  def fixture = new {
    val tx1 = mock[Transaction]
    val tx2 = mock[Transaction]
    val txs = Array(tx1, tx2)
    val ctx = new CompositeTransaction(txs: _*)
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
      doThrow (new UnsupportedOperationException) when tx2 prepare ()
      intercept[UnsupportedOperationException] { Transactions execute ctx }

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
      doThrow (new UnsupportedOperationException) when tx2 perform ()
      intercept[UnsupportedOperationException] { Transactions execute ctx }

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
      doThrow (new UnsupportedOperationException()) when tx2 perform ()
      doThrow (new RuntimeException) when tx2 rollback ()
      intercept[UnsupportedOperationException] { Transactions execute ctx }

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
      doThrow (new RuntimeException) when tx2 commit ()
      intercept[RuntimeException] { Transactions execute ctx }

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
