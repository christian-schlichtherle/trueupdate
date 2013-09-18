/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.server.maven.it

import net.java.trueupdate.artifact.maven.it.MavenArtifactResolverTestContext
import net.java.trueupdate.jaxrs.server.UpdateServiceExceptionMapper
import net.java.trueupdate.jaxrs.server.it.UpdateServiceITSuite
import com.sun.jersey.test.framework.WebAppDescriptor
import net.java.trueupdate.server.maven._

/** @author Christian Schlichtherle */
final class MavenUpdateServiceIT
extends UpdateServiceITSuite
with MavenArtifactResolverTestContext {

  override def artifactResolver = throw new UnsupportedOperationException

  override protected def configure =
    new WebAppDescriptor.Builder(
      Array[String](classOf[MavenUpdateServerApplication].getPackage.getName,
                    classOf[UpdateServiceExceptionMapper].getPackage.getName): _*)
      .contextPath("test")
      .build
}
