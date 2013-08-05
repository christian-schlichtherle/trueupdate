/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.maven

import net.java.trueupdate.core.artifact.ArtifactDescriptor
import ArtifactDescriptor.Builder
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers._
import org.scalatest.prop.PropertyChecks._

/** @author Christian Schlichtherle */
@RunWith(classOf[JUnitRunner])
class ArtifactConvertersTest extends WordSpec {

  "The artifact conversion should comply to the identity productions" in {
    val table = Table(
      ("groupId", "artifactId", "version", "classifier", "extension"),
      ("groupId", "artifactId", "version", "classifier", "extension")
    )
    forAll(table) { (groupId, artifactId, version, classifier, extension) =>
      val descriptor = new Builder()
        .groupId(groupId)
        .artifactId(artifactId)
        .version(version)
        .classifier(classifier)
        .extension(extension)
        .build
      val artifact = ArtifactConverters artifact descriptor
      val descriptor2 = ArtifactConverters descriptor artifact
      val artifact2 = ArtifactConverters artifact descriptor2

      descriptor should equal (descriptor2)
      descriptor should not be theSameInstanceAs (descriptor2)
      artifact should equal (artifact2)
      artifact should not be theSameInstanceAs (artifact2)

      artifact.getGroupId should equal (descriptor.groupId)
      artifact.getArtifactId should equal (descriptor.artifactId)
      artifact.getVersion should equal (descriptor.version)
      artifact.getClassifier should equal (descriptor.classifier)
      artifact.getExtension should equal (descriptor.extension)
    }
  }
}
