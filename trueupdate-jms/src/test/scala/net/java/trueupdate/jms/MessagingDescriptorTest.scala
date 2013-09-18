/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms

import javax.xml.bind.JAXBContext
import javax.xml.bind.annotation.XmlRootElement
import net.java.trueupdate.core.TestContext
import org.scalatest.WordSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers._
import org.scalatest.prop.PropertyChecks._
import scala.beans.BeanProperty

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class MessagingDescriptorTest extends WordSpec with TestContext {

  final override lazy val jaxbContext =
    JAXBContext.newInstance(classOf[MessagingDescriptorHolder])

  "A messaging descriptor" when {
    def builder = MessagingDescriptor.builder

    "build" should {
      "reflect the specified properties" in {
        val table = Table(
          ("builder", "connectionFactory", "agent", "manager"),
          (builder.connectionFactory("connectionFactory").agent("agent").manager("manager"),
            "connectionFactory", "agent", "manager")
        )
        forAll (table) { (builder, connectionFactory, agent, manager) =>
          val descriptor1 = builder.build
          descriptor1.connectionFactory should be (connectionFactory)
          descriptor1.agent should be (agent)
          descriptor1.manager should be (manager)

          val descriptor2 = builder.build
          descriptor2 should not be theSameInstanceAs (descriptor1)
          descriptor2 should equal (descriptor1)
          descriptor2.hashCode should equal (descriptor1.hashCode)
        }
      }
    }

    "composed into another XML serializable object" should {
      "be round-trip serializable" in {
        val table = Table(
          ("descriptor"),
          (builder.connectionFactory("connectionFactory").agent("agent").manager("manager").build)
        )
        forAll (table) { descriptor =>
          val holder = new MessagingDescriptorHolder
          holder.descriptor = descriptor
          assertRoundTripXmlSerializable(holder)
        }
      }
    }
  }
}

@XmlRootElement
class MessagingDescriptorHolder {

  @BeanProperty var descriptor: MessagingDescriptor = _

  override def equals(obj: Any) =
    super.equals(obj) || (obj match {
      case that: MessagingDescriptorHolder => this.descriptor == that.descriptor
      case _ => false
    })

  override def hashCode() = descriptor.hashCode
}
