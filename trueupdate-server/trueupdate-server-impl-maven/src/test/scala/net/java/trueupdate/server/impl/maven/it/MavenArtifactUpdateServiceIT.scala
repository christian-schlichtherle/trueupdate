/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.server.impl.maven.it

import net.java.trueupdate.artifact.impl.maven.it.MavenArtifactResolverTestContext
import net.java.trueupdate.jax.rs.server.it.ArtifactUpdateServiceITSuite
import com.sun.jersey.test.framework.WebAppDescriptor
import com.sun.jersey.api.core.DefaultResourceConfig
import net.java.trueupdate.jax.rs.server.{BasicArtifactUpdateServer, ArtifactUpdateServiceExceptionMapper}
import javax.ws.rs.Path

/** @author Christian Schlichtherle */
class MavenArtifactUpdateServiceIT
extends ArtifactUpdateServiceITSuite
with MavenArtifactResolverTestContext {

  override def artifactResolver = throw new UnsupportedOperationException

  override protected def configure =
    new WebAppDescriptor
      .Builder("net.java.trueupdate.jax.rs.server;net.java.trueupdate.server.impl.maven")
      .contextPath("test")
      .build
}
