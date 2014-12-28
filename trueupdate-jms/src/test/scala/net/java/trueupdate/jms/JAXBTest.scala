/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms

import java.util.logging._

import net.java.trueupdate.artifact.spec.ArtifactDescriptor
import net.java.trueupdate.message.UpdateMessage
import net.java.trueupdate.message.UpdateMessage.{builder, _}
import org.junit.runner.RunWith
import org.scalatest.Matchers._
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.prop.PropertyChecks._
import JAXBTest._

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class JAXBTest extends WordSpec {

  "An update message" should {
    "be round-trip XML-serializable" in {
      val table = Table(
        "originalMessage",
        builder
          .from("from")
          .to("to")
          .`type`(Type.UPDATE_NOTICE)
          .artifactDescriptor
            .groupId("groupId")
            .artifactId("artifactId")
            .version("version")
            .inject
          .currentLocation("currentLocation")
          .build,
        addLogRecord(builder
          .from("from")
          .to("to")
          .`type`(Type.UPDATE_NOTICE)
          .artifactDescriptor
            .groupId("groupId")
            .artifactId("artifactId")
            .version("version")
            .classifier("classifier")
            .packaging("extension")
            .inject
          .updateVersion("updateVersion")
          .currentLocation("currentLocation")
          .updateLocation("updateLocation")
          .build)
      )
      forAll(table) { originalMessage =>
        val encoding = JAXB.encode(originalMessage)
        logger log (Level.FINE, "\n{0}", encoding)
        encoding should not include "xsi:type"
        val clonedMessage = JAXB.decode(encoding)
        clonedMessage should equal (originalMessage)
      }
    }
  }
}

object JAXBTest {

  val logger = Logger.getLogger(getClass.getName)

  def addLogRecord(message: UpdateMessage) = {
    val ad = ArtifactDescriptor
      .builder
      .groupId("groupId")
      .artifactId("artifactId")
      .version("version")
      .classifier("classifier")
      .packaging("extension")
      .build
    val lr = new LogRecord(Level.INFO, "message")
    import lr._
    setLoggerName("loggerName")
    setResourceBundleName(classOf[UpdateMessage].getName)
    setSequenceNumber(Long.MaxValue)
    setSourceClassName(classOf[JAXBTest].getName)
    setSourceMethodName("logRecord")
    setMessage("test")
    setParameters(Array(1: Integer, 2: Integer, 3: Integer, ad))
    setThreadID(Int.MinValue)
    setMillis(System.currentTimeMillis)
    setThrown(new Throwable)
    message.attachedLogs.add(lr)
    message
  }
}
