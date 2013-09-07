/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.core;

import net.java.trueupdate.manager.spec.UpdateDescriptor;
import java.io.File;

/**
 * Resolves ZIP patch files for artifact updates.
 * <p>
 * Applications have no need to implement this class and should not do so
 * because it may be subject to future expansion.
 *
 * @author Christian Schlichtherle
 */
public interface UpdateResolver {

    /**
     * Resolves the diff ZIP file for the given update descriptor.
     * Clients must not modify or delete the returned file.
     *
     * @param descriptor the update descriptor.
     * @throws Exception at the discretion of the implementation.
     */
    File resolveDiffZip(UpdateDescriptor descriptor) throws Exception;
}
