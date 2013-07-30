/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.core.it

import com.stimulus.archiva.update.core.artifact.{ArtifactDescriptor, ArtifactResolver}

/** @author Christian Schlichtherle */
trait ArtifactITContext {
  def artifactResolver: ArtifactResolver
  def artifactDescriptor: ArtifactDescriptor
}
