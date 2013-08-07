/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.maven.it

import java.io.File
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers._
import org.scalatest.WordSpec
import org.eclipse.aether.repository.{RemoteRepository, LocalRepository}
import net.java.trueupdate.artifact.ArtifactDescriptor

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
class MavenArtifactResolverIT extends WordSpec
with MavenArtifactResolverTestContext {

  import MavenArtifactResolverIT._

  def resolvedPath(descriptor: ArtifactDescriptor) =
    new File(baseDir, relativePath(descriptor))

  def baseDir = new File(testRepositories().local.basedir).getAbsoluteFile

  "A maven artifact resolver" should {
    val artifactFile = artifactResolver resolveArtifactFile artifactDescriptor

    "resolve the readable artifact file" in {
      artifactFile should equal (resolvedPath(artifactDescriptor))
      artifactFile canRead () should be (true)
    }

    "resolve the update descriptor and path to a readable file" in {
      val updateDescriptor = artifactResolver resolveUpdateDescriptor artifactDescriptor
      updateDescriptor.groupId should equal (artifactDescriptor.groupId)
      updateDescriptor.artifactId should equal (artifactDescriptor.artifactId)
      // The version may change over time.
      updateDescriptor.version should not equal (artifactDescriptor.version)
      updateDescriptor.classifier should equal (artifactDescriptor.classifier)
      updateDescriptor.extension should equal (artifactDescriptor.extension)
      val updateFile = artifactResolver resolveArtifactFile updateDescriptor
      updateFile canRead () should be (true)
    }
  }
}
