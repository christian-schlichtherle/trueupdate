/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.core.tx

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.WordSpec

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
}

class NestedTransaction extends Transaction {
  def perform() { Transactions execute this }
  def rollback() { }
}
