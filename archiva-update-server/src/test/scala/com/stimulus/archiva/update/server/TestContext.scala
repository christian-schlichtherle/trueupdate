/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server

import com.stimulus.archiva.update.server.resolver.{ArtifactDescriptor, ArtifactResolver}

/** @author Christian Schlichtherle */
trait TestContext {
  def artifactResolver: ArtifactResolver
  def artifactDescriptor: ArtifactDescriptor
}
