/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.artifact.impl.maven.it

import net.java.trueupdate.artifact.spec._
import net.java.trueupdate.core.TestContext
import net.java.trueupdate.core.io.Sources
import net.java.trueupdate.artifact.impl.maven.MavenArtifactResolver

/** @author Christian Schlichtherle */
trait MavenArtifactResolverTestContext
extends TestContext with ArtifactResolverTestContext {

  override def artifactResolver =
    MavenArtifactResolver decodeFromXml testRepositories

  private def testRepositories =
    Sources.forResource("test-repositories.xml",
      classOf[MavenArtifactResolverTestContext])

  final override lazy val jaxbContext = MavenArtifactResolver.jaxbContext
}
