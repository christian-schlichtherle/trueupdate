/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.maven

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers._
import org.eclipse.aether.repository.{LocalRepository, RemoteRepository}
import com.stimulus.archiva.update.server.resolver.ArtifactDescriptor
import java.nio.file.{Files, Paths}

/** @author Christian Schlichtherle */
@RunWith(classOf[JUnitRunner])
class MavenArtifactResolverIT extends WordSpec {

  private val resolver = new MavenArtifactResolver(testRepository, centralRepository)

  private def testRepository = new LocalRepository(baseDir.toFile)
  private def baseDir = Paths.get("target/repository").toAbsolutePath
  private def centralRepository = new RemoteRepository.Builder(
    "central", "default", "http://repo1.maven.org/maven2/").build

  private def artifactDescriptor =
    new ArtifactDescriptor.Builder()
      .groupId("net.java.truevfs")
      .artifactId("truevfs-kernel-spec")
      .version("0.9")
      .build

  private def resolvedPath(descriptor: ArtifactDescriptor) =
    baseDir resolve relativePath(descriptor)

  private def relativePath(descriptor: ArtifactDescriptor) =
    "%1$s/%2$s/%3$s/%2$s-%3$s%4$s.%5$s".format(
    slashify(descriptor.groupId),
    descriptor.artifactId,
    descriptor.version,
    dashify(descriptor.classifier),
    descriptor.extension)

  private def slashify(groupId: String) = groupId.replace('.', '/')

  private def dashify(classifier: String) =
    if (classifier.isEmpty) classifier else "-" + classifier

  "A maven artifact resolver" should {
    val artifactPath = resolver resolveArtifactPath artifactDescriptor

    "resolve the artifact path to a readable file" in {
      artifactPath should equal (resolvedPath(artifactDescriptor))
      Files.isReadable(artifactPath) should be (true)
    }

    "resolve the update descriptor and path to a readable file" in {
      // The resolved artifact descriptor may change over time.
      val updateDescriptor = resolver resolveUpdateDescriptor artifactDescriptor
      updateDescriptor.groupId should equal (artifactDescriptor.groupId)
      updateDescriptor.artifactId should equal (artifactDescriptor.artifactId)
      updateDescriptor.version should not equal (artifactDescriptor.version)
      updateDescriptor.classifier should equal (artifactDescriptor.classifier)
      updateDescriptor.extension should equal (artifactDescriptor.extension)
      val updatePath = resolver resolveArtifactPath updateDescriptor
      Files.isReadable(updatePath) should be (true)
    }
  }
}
