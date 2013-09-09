package net.java.trueupdate.manager.core.tx

import java.io._
import net.java.trueupdate.manager.core.io._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers._

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class PathTaskTransactionIT extends FileTransactionITSuite {

  def tx(oneByte: File, notExists: File) =
    new PathTaskTransaction(notExists, new PathTask[Unit, IOException] {
      def execute(notExists: File) {
        Files.zip(notExists, oneByte, oneByte.getName)
      }
    })

  "A path task transaction" when {
    "executing successfully" should {
      "have zipped the source file" in {
        setUpAndLoan { (oneByte, notExists, tx) =>
          Transactions execute tx
          oneByte.length should be (1)
          notExists.length should be > (1L)
        }
      }
    }
  }
}
