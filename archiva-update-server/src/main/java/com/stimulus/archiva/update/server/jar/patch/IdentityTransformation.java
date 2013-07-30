/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jar.patch;

import com.stimulus.archiva.update.server.jar.model.EntryNameWithDigest;

/**
 * The identity transformation.
 *
 * @author Christian Schlichtherle
 */
final class IdentityTransformation
implements Transformation<EntryNameWithDigest> {
    @Override public EntryNameWithDigest apply(EntryNameWithDigest entryDigest) {
        return entryDigest;
    }
}
