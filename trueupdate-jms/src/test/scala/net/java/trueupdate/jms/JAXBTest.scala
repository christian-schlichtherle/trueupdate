/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms

import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers._
import org.scalatest.prop.PropertyChecks._
import net.java.trueupdate.message.LogMessage
import net.java.trueupdate.message.UpdateMessage
import net.java.trueupdate.message.UpdateMessage._
import java.util.logging._

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class JAXBTest extends WordSpec {

  type Int = java.lang.Integer

  def builder = UpdateMessage.builder

  val subjects = Table(
    ("original"),
    (builder
      .from("from")
      .to("to")
      .`type`(Type.UPDATE_NOTICE)
      .artifactDescriptor
        .groupId("groupId")
        .artifactId("artifactId")
        .version("version")
        .inject)
      .currentLocation("currentLocation"),
    (builder
      .from("from")
      .to("to")
      .`type`(Type.UPDATE_NOTICE)
      .artifactDescriptor
        .groupId("groupId")
        .artifactId("artifactId")
        .version("version")
        .classifier("classifier")
        .extension("extension")
        .inject
      .updateVersion("updateVersion")
      .currentLocation("currentLocation")
      .updateLocation("updateLocation")
      .logMessages
        .add(LogMessage
          .builder
          .level(Level.INFO)
          .message("message")
          .parameters(1: Int, 2: Int, 3: Int)
          .build)
        .inject)
  )

  "An update message" when {
    "constructed" should {
      "be round-trip XML-serializable" in {
        forAll(subjects) { builder =>
          val original = builder.build
          val originalEncoding = JAXB.encode(original)
          JAXBTest.logger log (Level.FINE, "\n{0}", originalEncoding)
          val clone = JAXB.decode(originalEncoding)
          clone should equal (original)
        }
      }
    }
  }
}

object JAXBTest {
  val logger = Logger.getLogger(getClass.getName)
}
