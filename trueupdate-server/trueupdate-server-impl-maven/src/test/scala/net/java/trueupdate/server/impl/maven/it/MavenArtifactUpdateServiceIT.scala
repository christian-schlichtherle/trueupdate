/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.server.impl.maven.it

import net.java.trueupdate.artifact.impl.maven.it.MavenArtifactResolverTestContext
import net.java.trueupdate.jax.rs.server.it.ArtifactUpdateServiceITSuite
import com.sun.jersey.test.framework.WebAppDescriptor
import net.java.trueupdate.server.impl.maven._
import javax.ws.rs.core.Application

/** @author Christian Schlichtherle */
class MavenArtifactUpdateServiceIT
extends ArtifactUpdateServiceITSuite
with MavenArtifactResolverTestContext {

  override def artifactResolver = throw new UnsupportedOperationException

  override protected def configure =
    new WebAppDescriptor
      .Builder(packagesOf(new ArtifactUpdateServerApplication): _*)
      .contextPath("test")
      .build

  private def packagesOf(app: Application) = {
    import collection.JavaConverters._
    (app.getClasses.asScala map (_.getPackage.getName)).toArray
  }
}
