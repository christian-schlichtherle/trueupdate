/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.it

import com.stimulus.archiva.update.jax.rs.it.ConfiguredUpdateServiceITSuite
import com.stimulus.archiva.update.maven.it.MavenArtifactITContext

/** @author Christian Schlichtherle */
class MavenConfiguredUpdateServiceIT
extends ConfiguredUpdateServiceITSuite
with MavenArtifactITContext
