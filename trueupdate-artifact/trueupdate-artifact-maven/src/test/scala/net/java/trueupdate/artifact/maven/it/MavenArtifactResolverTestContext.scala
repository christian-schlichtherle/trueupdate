/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.artifact.maven.it

import net.java.trueupdate.artifact.spec._
import net.java.trueupdate.core.TestContext
import net.java.trueupdate.core.io.Sources
import net.java.trueupdate.artifact.maven.MavenArtifactResolverAdapter
import net.java.trueupdate.artifact.maven.model.Repositories

/** @author Christian Schlichtherle */
trait MavenArtifactResolverTestContext
extends TestContext with ArtifactResolverTestContext {

  final override def artifactResolver =
    new MavenArtifactResolverAdapter() unmarshal testRepositories()

  final def testRepositories(): Repositories =
    jaxbCodec decode (testRepositoriesSource, classOf[Repositories])

  private def testRepositoriesSource =
    Sources.forResource("test-repositories.xml", classOf[Repositories])

  final override lazy val jaxbContext = Repositories.jaxbContext
}
