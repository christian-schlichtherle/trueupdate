/*******************************************************************************
 * Copyright (c) 2010, 2011 Sonatype, Inc. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Sonatype, Inc. - initial API and implementation
 *******************************************************************************/
package com.stimulus.archiva.update.server.maven;

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
