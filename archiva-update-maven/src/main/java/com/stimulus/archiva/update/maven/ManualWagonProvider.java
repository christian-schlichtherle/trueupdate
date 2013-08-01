/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.maven;

import org.apache.maven.wagon.Wagon;
import org.eclipse.aether.connector.wagon.WagonProvider;
import org.sonatype.maven.wagon.AhcWagon;

/**
 * A simplistic provider for wagon instances when no Plexus-compatible IoC
 * container is used.
 *
 * @author Christian Schlichtherle (revision)
 */
final class ManualWagonProvider implements WagonProvider {

    @Override public Wagon lookup(String roleHint) throws Exception {
        return "http".equals(roleHint) ? new AhcWagon() : null;
    }

    @Override public void release(Wagon wagon) { }
}
