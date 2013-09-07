package net.java.trueupdate.manager.core.tx

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.mockito.Mockito._
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers._
import org.scalatest.mock.MockitoSugar.mock

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class AtomicMethodsTransactionTest extends WordSpec {

  def failWithNonTransactionException(tx: Transaction) {
    try { Transactions execute tx }
    catch {
      case error: TransactionException => throw error
      case expected: Exception =>
    }
  }

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
    }

    "failing to perform" should {
      val tx = new TestTransaction
      import tx._
      doThrow (new Exception) when delegate perform ()
      failWithNonTransactionException(tx)

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
    }
  }
}

class TestTransaction extends AtomicMethodsTransaction {

  val delegate = mock[Transaction]

  override def prepareAtomic() { delegate prepare () }
  override def performAtomic() { delegate perform () }
  override def rollbackAtomic() { delegate rollback () }
  override def commitAtomic() { delegate commit () }
}
