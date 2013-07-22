/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.maven

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers._
import org.scalatest.prop.PropertyChecks._
import com.stimulus.archiva.update.commons.ArtifactDescriptor
import ArtifactDescriptor.Builder
import com.stimulus.archiva.update.commons.ArtifactDescriptor

/** @author Christian Schlichtherle */
@RunWith(classOf[JUnitRunner])
class ArtifactConversionTest extends WordSpec {

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
      val artifact = ArtifactConversion artifact descriptor
      val descriptor2 = ArtifactConversion descriptor artifact
      val artifact2 = ArtifactConversion artifact descriptor2

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
