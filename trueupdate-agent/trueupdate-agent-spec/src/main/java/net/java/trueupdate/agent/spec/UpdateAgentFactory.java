/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.spec;

import net.java.trueupdate.artifact.spec.ArtifactDescriptor;

/**
 * @author Christian Schlichtherle
 */
public interface UpdateAgentFactory {

    UpdateAgent newUpdateAgent(ArtifactDescriptor artifactDescriptor,
                               UpdateManagerListener visitor);
}
