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

private object MavenArtifactRepositoryTest {

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
class MavenArtifactRepositoryTest extends WordSpec
with MavenArtifactRepositoryTestContext {

  import MavenArtifactRepositoryTest._

  def resolvedPath(descriptor: ArtifactDescriptor) =
    new File(baseDir, relativePath(descriptor))

  def baseDir = new File(testRepositories().local.basedir).getAbsoluteFile

  "A maven artifact resolver" should {
    val artifactFile = artifactRepository resolveArtifactFile artifactDescriptor

    "resolve the readable artifact file" in {
      artifactFile should equal (resolvedPath(artifactDescriptor))
      artifactFile canRead () should be (true)
    }

    "resolve the update descriptor and path to a readable file" in {
      val updateDescriptor = artifactRepository resolveUpdateDescriptor artifactDescriptor
      updateDescriptor.groupId should equal (artifactDescriptor.groupId)
      updateDescriptor.artifactId should equal (artifactDescriptor.artifactId)
      // The version may change over time.
      updateDescriptor.version should not equal (artifactDescriptor.version)
      updateDescriptor.classifier should equal (artifactDescriptor.classifier)
      updateDescriptor.extension should equal (artifactDescriptor.extension)
      val updateFile = artifactRepository resolveArtifactFile updateDescriptor
      updateFile canRead () should be (true)
    }
  }
}
