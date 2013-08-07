/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.maven.it

import net.java.trueupdate.artifact.spec._
import net.java.trueupdate.core.TestContext
import net.java.trueupdate.core.io.Sources
import net.java.trueupdate.maven.MavenArtifactResolverAdapter
import net.java.trueupdate.maven.model.Repositories

/** @author Christian Schlichtherle */
trait MavenArtifactResolverTestContext
extends TestContext with ArtifactResolverTestContext {

  final override def artifactResolver: ArtifactResolver =
    new MavenArtifactResolverAdapter() unmarshal testRepositories()

  final def testRepositories(): Repositories =
    jaxbCodec decode (testRepositoriesSource, classOf[Repositories])

  private def testRepositoriesSource =
    Sources.forResource("test-repositories.xml",
      classOf[MavenArtifactResolverTestContext])

  final override lazy val jaxbContext = Repositories.jaxbContext
}
