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
class MavenPathResolverIT extends WordSpec {

  private val resolver = new MavenPathResolver(testRepository, centralRepository)

  private def testRepository = new LocalRepository(baseDir.toFile)
  private def baseDir = Paths.get("target/repository").toAbsolutePath
  private def centralRepository = new RemoteRepository.Builder(
    "central", "default", "http://repo1.maven.org/maven2/").build

  private def descriptor =
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

  "A maven path resolver" should {
    "resolve the artifact path" in {
      resolver resolveArtifactPath descriptor should
        equal (resolvedPath(descriptor))
    }

    "resolve the update path" in {
      // The resolved update path may change over time.
      Files.isReadable(resolver resolveUpdatePath descriptor) should be (true)
    }
  }
}
