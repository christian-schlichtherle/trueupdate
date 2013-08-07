/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.repository.spec

import net.java.trueupdate.artifact.ArtifactDescriptor

/** @author Christian Schlichtherle */
trait ArtifactResolverTestContext {

  def artifactResolver: ArtifactResolver

  final def artifactDescriptor =
    new ArtifactDescriptor.Builder()
      .groupId("net.java.truevfs")
      .artifactId("truevfs-kernel-spec")
      .version("0.9")
      .build
}
