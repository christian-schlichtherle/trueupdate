/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.maven

import com.stimulus.archiva.update.server.TestContext
import org.eclipse.aether.repository.{RemoteRepository, LocalRepository}
import java.nio.file.Paths
import java.io.File
import com.stimulus.archiva.update.server.resolver.ArtifactDescriptor

/** @author Christian Schlichtherle */
trait MavenTestContext extends TestContext {

  override def artifactResolver =
    new MavenArtifactResolver(testRepository, centralRepository)

  def testRepository = new LocalRepository(baseDir)
  private def baseDir = new File("target/repository").getAbsoluteFile
  def centralRepository = new RemoteRepository.Builder(
    "central", "default", "http://repo1.maven.org/maven2/").build

  override def artifactDescriptor =
    new ArtifactDescriptor.Builder()
      .groupId("net.java.truevfs")
      .artifactId("truevfs-kernel-spec")
      .version("0.9")
      .build
}
