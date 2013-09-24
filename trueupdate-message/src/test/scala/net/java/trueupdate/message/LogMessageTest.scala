/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.message

import java.util.logging.Level
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers._
import org.scalatest.prop.PropertyChecks._

/** @author Christian Schlichtherle */
@RunWith(classOf[JUnitRunner])
class LogMessageTest extends WordSpec {

  def builder = LogMessage.builder

  val subjects = Table(
    ("builder", "level", "message", "parameters"),
    (builder.level(Level.FINEST).message("message"),
      Level.FINEST, "message", Array()),
    (builder.level(Level.FINER).message("message").parameters("one", 2: java.lang.Integer),
      Level.FINER, "message", Array("one", 2: java.lang.Integer))
  )

  "A log message" when {
    "build" should {
      "reflect the specified properties" in {
        forAll (subjects) { (builder, level, message, parameters) =>
          val message1 = builder.build
          message1.level should equal (level)
          message1.message should equal (message)
          message1.parameters should equal (parameters)

          val message2 = builder.build
          message2 should not be theSameInstanceAs (message1)
          message2 should equal (message1)
          message2.hashCode should equal (message1.hashCode)
          message2.toString should not be 'empty
          message2.toString should equal (message1.toString)
        }
      }
    }
  }
}
