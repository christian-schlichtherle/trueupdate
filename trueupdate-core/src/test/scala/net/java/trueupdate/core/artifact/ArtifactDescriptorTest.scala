/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.artifact

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers._
import org.scalatest.prop.PropertyChecks._
import ArtifactDescriptor.Builder

/** @author Christian Schlichtherle */
@RunWith(classOf[JUnitRunner])
class ArtifactDescriptorTest extends WordSpec {

  "An artifact descriptor" when {
    "build" should {
      def builder = new Builder

      "reflect the specified properties" in {
        val table = Table(
          ("builder", "groupId", "artifactId", "version", "classifier", "extension"),
          (builder.groupId("groupId").artifactId("artifactId").version("version"),
            "groupId", "artifactId", "version", "", "jar")
        )
        forAll (table) { (builder, groupId, artifactId, version, classifier, extension) =>
          val descriptor1 = builder.build
          descriptor1.groupId should be (groupId)
          descriptor1.artifactId should be (artifactId)
          descriptor1.version should be (version)
          descriptor1.classifier should be (classifier)
          descriptor1.extension should be (extension)

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
