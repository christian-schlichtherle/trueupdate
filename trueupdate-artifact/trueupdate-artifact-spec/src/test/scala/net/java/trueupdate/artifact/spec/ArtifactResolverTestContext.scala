/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.artifact.spec

/** @author Christian Schlichtherle */
trait ArtifactResolverTestContext {

  def artifactResolver: ArtifactResolver

  final def artifactDescriptor =
    ArtifactDescriptor
      .builder
      .groupId("net.java.truevfs")
      .artifactId("truevfs-kernel-spec")
      .version("0.9")
      .build
}
