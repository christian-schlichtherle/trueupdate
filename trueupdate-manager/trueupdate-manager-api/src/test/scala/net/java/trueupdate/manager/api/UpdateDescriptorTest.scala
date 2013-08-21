/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.api

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers._
import org.scalatest.prop.PropertyChecks._
import net.java.trueupdate.artifact.api.ArtifactDescriptor

/** @author Christian Schlichtherle */
@RunWith(classOf[JUnitRunner])
class UpdateDescriptorTest extends WordSpec {

  "An update descriptor" when {
    "build" should {
      def builder = UpdateDescriptor.builder

      "reflect the specified properties" in {
        val table = Table(
          ("builder", "artifactDescriptor", "updateVersion"),
          (builder.artifactDescriptor().groupId("groupId").artifactId("artifactId").version("version").inject.updateVersion("1.0"),
            ArtifactDescriptor.builder.groupId("groupId").artifactId("artifactId").version("version").build,
            "1.0")
        )
        forAll (table) { (builder, artifactDescriptor, updateVersion) =>
          val descriptor1 = builder.build
          descriptor1.artifactDescriptor should equal (artifactDescriptor)
          descriptor1.updateVersion should be (updateVersion)

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
