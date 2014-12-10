/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms

import java.util.logging._
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.Matchers._
import org.scalatest.prop.PropertyChecks._
import net.java.trueupdate.artifact.spec.ArtifactDescriptor
import net.java.trueupdate.message.UpdateMessage
import net.java.trueupdate.message.UpdateMessage._

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class JAXBTest extends WordSpec {

  type Int = java.lang.Integer

  def builder = UpdateMessage.builder

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
    setParameters(Array(1: Int, 2: Int, 3: Int, ad))
    setThreadID(Int.MinValue)
    setMillis(System.currentTimeMillis)
    setThrown(new Throwable)
    message.attachedLogs.add(lr)
    message
  }

  val subjects = Table(
    "original",
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

  "An update message" when {
    "constructed" should {
      "be round-trip XML-serializable" in {
        forAll(subjects) { original =>
          val originalEncoding = JAXB.encode(original)
          JAXBTest.logger log (Level.FINE, "\n{0}", originalEncoding)
          originalEncoding contains "xsi:type" should not be true
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
