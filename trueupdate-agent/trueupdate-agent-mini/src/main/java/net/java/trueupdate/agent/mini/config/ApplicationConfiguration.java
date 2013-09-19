/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.mini.config;

/**
 * Configures an application.
 *
 * @author Christian Schlichtherle
 */
@SuppressWarnings("PublicField")
public class ApplicationConfiguration {
    public ArtifactConfiguration artifact;
    public String currentLocation, updateLocation, listenerClass;
}
