/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.maven

import com.stimulus.archiva.update.core.ArtifactDescriptor
import java.nio.file.Files
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers._

private object MavenArtifactResolverIT {

  private def relativePath(descriptor: ArtifactDescriptor) =
    "%1$s/%2$s/%3$s/%2$s-%3$s%4$s.%5$s".format(
      slashify(descriptor.groupId),
      descriptor.artifactId,
      descriptor.version,
      dashify(descriptor.classifier),
      descriptor.extension
    )

  private def slashify(groupId: String) = groupId.replace('.', '/')

  private def dashify(classifier: String) =
    if (classifier.isEmpty) classifier else "-" + classifier
}

/** @author Christian Schlichtherle */
@RunWith(classOf[JUnitRunner])
class MavenArtifactResolverIT extends WordSpec with MavenTestContext {

  import MavenArtifactResolverIT._

  private def resolvedPath(descriptor: ArtifactDescriptor) =
    testRepository.getBasedir.toPath resolve relativePath(descriptor)

  "A maven artifact resolver" should {
    val artifactPath = artifactResolver resolveArtifactPath artifactDescriptor

    "resolve the artifact path to a readable file" in {
      artifactPath should equal (resolvedPath(artifactDescriptor))
      Files.isReadable(artifactPath) should be (true)
    }

    "resolve the update descriptor and path to a readable file" in {
      val updateDescriptor = artifactResolver resolveUpdateDescriptor artifactDescriptor
      updateDescriptor.groupId should equal (artifactDescriptor.groupId)
      updateDescriptor.artifactId should equal (artifactDescriptor.artifactId)
      // The version may change over time.
      updateDescriptor.version should not equal (artifactDescriptor.version)
      updateDescriptor.classifier should equal (artifactDescriptor.classifier)
      updateDescriptor.extension should equal (artifactDescriptor.extension)
      val updatePath = artifactResolver resolveArtifactPath updateDescriptor
      Files.isReadable(updatePath) should be (true)
    }
  }
}
