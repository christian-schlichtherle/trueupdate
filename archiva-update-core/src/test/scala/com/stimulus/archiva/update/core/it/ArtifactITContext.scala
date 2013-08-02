/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.core.it

import com.stimulus.archiva.update.core.artifact.{ArtifactDescriptor, ArtifactResolver}
import com.stimulus.archiva.update.core.TestContext

/** @author Christian Schlichtherle */
trait ArtifactITContext extends TestContext {
  def artifactResolver: ArtifactResolver
  def artifactDescriptor: ArtifactDescriptor
}
