/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.maven.it

import java.io.File
import org.eclipse.aether.repository.{RemoteRepository, LocalRepository}
import com.stimulus.archiva.update.core.artifact.{ArtifactDescriptor, ArtifactResolver}
import com.stimulus.archiva.update.core.it.ArtifactITContext
import com.stimulus.archiva.update.maven.{MavenArtifactResolverAdapter, MavenArtifactResolver}
import com.stimulus.archiva.update.maven.model.Repositories
import com.stimulus.archiva.update.core.io.Sources

/** @author Christian Schlichtherle */
trait MavenArtifactITContext extends ArtifactITContext {

  override def artifactResolver: ArtifactResolver =
    new MavenArtifactResolverAdapter() unmarshal testRepositories()

  def testRepositories(): Repositories =
    jaxbCodec decode (testRepositoriesSource, classOf[Repositories])

  private def testRepositoriesSource = Sources.forResource("test-repositories.xml",
    classOf[MavenArtifactITContext])

  override lazy val jaxbContext = Repositories.jaxbContext

  override def artifactDescriptor =
    new ArtifactDescriptor.Builder()
      .groupId("net.java.truevfs")
      .artifactId("truevfs-kernel-spec")
      .version("0.9")
      .build
}
