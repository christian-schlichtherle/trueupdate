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
class NamingDescriptorTest extends WordSpec with TestContext {

  final override lazy val jaxbContext =
    JAXBContext.newInstance(classOf[NamingDescriptorHolder])

  "A naming descriptor" when {
    def builder = NamingDescriptor.builder

    "build" should {
      "reflect the specified properties" in {
        val table = Table(
          ("builder", "initialContextClass", "lookup"),
          (builder, "javax.naming.InitialContext", "java:comp/env"),
          (builder.initialContextClass("initialContextClass").lookup("lookup"),
            "initialContextClass", "lookup")
        )
        forAll (table) { (builder, initialContextClass, lookup) =>
          val descriptor1 = builder.build
          descriptor1.initialContextClass should be (initialContextClass)
          descriptor1.lookup should be (lookup)

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
          (builder.initialContextClass("initialContextClass").lookup("lookup").build)
        )
        forAll (table) { descriptor =>
          val holder = new NamingDescriptorHolder
          holder.descriptor = descriptor
          assertRoundTripXmlSerializable(holder)
        }
      }
    }
  }
}

@XmlRootElement
class NamingDescriptorHolder {

  @BeanProperty var descriptor: NamingDescriptor = _

  override def equals(obj: Any) =
    super.equals(obj) || (obj match {
      case that: NamingDescriptorHolder => this.descriptor == that.descriptor
      case _ => false
    })

  override def hashCode() = descriptor.hashCode
}
