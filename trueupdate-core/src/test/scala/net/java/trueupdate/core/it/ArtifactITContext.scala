/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.it

import net.java.trueupdate.core.TestContext
import net.java.trueupdate.core.artifact.{ArtifactResolver}
import net.java.trueupdate.agent.spec.ArtifactDescriptor

/** @author Christian Schlichtherle */
trait ArtifactITContext extends TestContext {
  def artifactResolver: ArtifactResolver
  def artifactDescriptor: ArtifactDescriptor
}
