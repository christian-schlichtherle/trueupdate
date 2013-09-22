/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.artifact.maven.it

import java.io.File
import net.java.trueupdate.artifact.maven._
import net.java.trueupdate.artifact.spec._
import net.java.trueupdate.core.TestContext
import org.eclipse.aether.repository._

/** @author Christian Schlichtherle */
trait MavenArtifactResolverTestContext
extends TestContext with ArtifactResolverTestContext {

  override def artifactResolver = new MavenArtifactResolver(parameters)

  lazy val parameters = MavenParameters
    .builder
    .localRepository(new LocalRepository(new File("target/repository")))
    .remoteRepositories(new RemoteRepository
                        .Builder("central", "default", "http://repo1.maven.org/maven2/")
                        .build)
    .build
}
