/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms

import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers._
import net.java.trueupdate.message.UpdateMessage
import net.java.trueupdate.message.UpdateMessage.Type
import java.util.logging.{Level, Logger}

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class JAXBTest extends WordSpec {

  def updateMessage = UpdateMessage
    .builder
    .from("from")
    .to("to")
    .`type`(Type.UPDATE_NOTICE)
    .artifactDescriptor()
      .groupId("groupId")
      .artifactId("artifactId")
      .version("version")
      .classifier("classifier")
      .extension("extension")
      .inject
    .currentLocation("currentLocation")
    .updateLocation("updateLocation")
    .updateVersion("updateVersion")
    .status("status")
    .build

  "An update message" when {
    "constructed" should {
      "be round-trip XML-serializable" in {
        val original = updateMessage
        val originalEncoding = JAXB.encode(original)
        JAXBTest.logger log (Level.FINE, "\n{0}", originalEncoding)
        val clone = JAXB.decode(originalEncoding)
        val cloneEncoding = JAXB.encode(clone)
        cloneEncoding should equal (originalEncoding)
      }
    }
  }
}

object JAXBTest {
  val logger = Logger.getLogger(getClass.getName)
}