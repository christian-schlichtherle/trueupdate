/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.maven.it

import net.java.trueupdate.maven.MavenArtifactResolverAdapter
import net.java.trueupdate.maven.model.Repositories
import net.java.trueupdate.core.it.ArtifactITContext
import net.java.trueupdate.core.io.Sources
import net.java.trueupdate.core.artifact.{ArtifactResolver}
import net.java.trueupdate.artifact.ArtifactDescriptor

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
