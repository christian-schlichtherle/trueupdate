/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.spec.ci;

import net.java.trueupdate.artifact.spec.ci.ArtifactCi;

/**
 * Configures an application.
 *
 * @author Christian Schlichtherle
 */
@SuppressWarnings("PublicField")
public class ApplicationCi {
    public ArtifactCi artifact;
    public String currentLocation, updateLocation, listenerClass;
}
