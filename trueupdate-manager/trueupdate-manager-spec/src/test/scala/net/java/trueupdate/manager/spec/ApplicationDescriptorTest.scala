/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.spec

import java.net.URI
import net.java.trueupdate.artifact.spec.ArtifactDescriptor
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers._
import org.scalatest.prop.PropertyChecks._

/** @author Christian Schlichtherle */
@RunWith(classOf[JUnitRunner])
class ApplicationDescriptorTest extends WordSpec {

  "An application descriptor" when {
    "build" should {
      def builder = ApplicationDescriptor.builder

      "reflect the specified properties" in {
        val table = Table(
          ("builder", "artifactDescriptor", "currentLocation"),
          (builder.artifactDescriptor().groupId("groupId").artifactId("artifactId").version("version").inject.currentLocation(URI create "here"),
            ArtifactDescriptor.builder.groupId("groupId").artifactId("artifactId").version("version").build,
            URI create "here")
        )
        forAll (table) { (builder, artifactDescriptor, currentLocation) =>
          val descriptor1 = builder.build
          descriptor1.artifactDescriptor should equal (artifactDescriptor)
          descriptor1.currentLocation should be (currentLocation)

          val descriptor2 = builder.build
          descriptor2 should not be theSameInstanceAs (descriptor1)
          descriptor2 should equal (descriptor1)
          descriptor2.hashCode should equal (descriptor1.hashCode)
          descriptor2.toString should not be 'empty
          descriptor2.toString should equal (descriptor1.toString)
        }
      }
    }
  }
}
