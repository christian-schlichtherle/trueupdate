/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.maven

import com.stimulus.archiva.update.core._
import java.io.File
import org.eclipse.aether.repository.{RemoteRepository, LocalRepository}

/** @author Christian Schlichtherle */
trait MavenTestContext extends TestContext {

  override def artifactResolver: ArtifactResolver =
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
