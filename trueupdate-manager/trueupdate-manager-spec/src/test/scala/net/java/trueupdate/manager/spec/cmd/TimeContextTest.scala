package net.java.trueupdate.manager.spec.cmd

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.WordSpec
import org.mockito.Mockito._
import java.util.logging.Logger
import java.util.logging.Level._
import net.java.trueupdate.manager.spec.cmd.TimeContext.Method
import org.scalatest.mock.MockitoSugar.mock
import TimeContext.Method._
import java.util.Calendar
import org.scalatest.prop.PropertyChecks._

/** @author Christian Schlichtherle */
@RunWith(classOf[JUnitRunner])
class TimeContextTest extends WordSpec {

  val methods = Table("method", perform, revert)

  val durationMillis = {
    val cal = Calendar.getInstance()
    import cal._
    setTimeInMillis(0)
    add(Calendar.HOUR_OF_DAY, 48)
    add(Calendar.MINUTE, 59)
    add(Calendar.SECOND, 59)
    add(Calendar.MILLISECOND, 999)
    cal.getTimeInMillis
  }

  "A TimeContext" should {
    "log the starting of the methods" in {
      val tc = new TestTimeContext
      forAll(methods) { method =>
        tc.logStarting(method)
        verify(tc.logger) log (FINE, tc startingMessage method, method)
      }
      verifyNoMoreInteractions(tc.logger)
    }

    "log the success of the methods" in {
      val tc = new TestTimeContext
      forAll(methods) { method =>
        tc.logSucceeded(method, durationMillis)
        verify(tc.logger) log (INFO, tc succeededMessage method,
          Array[Integer](method.ordinal, 0, 48, 59, 59, 999).asInstanceOf[Array[AnyRef]])
      }
      verifyNoMoreInteractions(tc.logger)
    }

    "log the failure of the methods" in {
      val tc = new TestTimeContext
      forAll(methods) { method =>
        tc.logFailed(method, durationMillis)
        verify(tc.logger) log (WARNING, tc failedMessage method,
          Array[Integer](method.ordinal, 1, 48, 59, 59, 999).asInstanceOf[Array[AnyRef]])
      }
      verifyNoMoreInteractions(tc.logger)
    }
  }
}

private class TestTimeContext extends TimeContext {
  override val logger = mock[Logger]
  override def startingMessage(method: Method) = "Starting to " + method
  override def succeededMessage(method: Method) = "Succeeded to " + method
  override def failedMessage(method: Method) = "Failed to " + method
}
