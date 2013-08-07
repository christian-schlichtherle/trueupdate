/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.maven.it

import net.java.trueupdate.core.TestContext
import net.java.trueupdate.core.io.Sources
import net.java.trueupdate.maven.MavenArtifactRepositoryAdapter
import net.java.trueupdate.maven.model.Repositories
import net.java.trueupdate.repository.spec._

/** @author Christian Schlichtherle */
trait MavenArtifactRepositoryTestContext
extends TestContext with ArtifactRepositoryTestContext {

  final override def artifactRepository: ArtifactRepository =
    new MavenArtifactRepositoryAdapter() unmarshal testRepositories()

  final def testRepositories(): Repositories =
    jaxbCodec decode (testRepositoriesSource, classOf[Repositories])

  private def testRepositoriesSource =
    Sources.forResource("test-repositories.xml",
      classOf[MavenArtifactRepositoryTestContext])

  final override lazy val jaxbContext = Repositories.jaxbContext
}
