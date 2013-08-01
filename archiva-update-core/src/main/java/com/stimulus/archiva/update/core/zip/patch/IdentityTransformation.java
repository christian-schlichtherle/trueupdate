/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.core.zip.patch;

import com.stimulus.archiva.update.core.zip.model.EntryNameWithDigest;

/**
 * The identity transformation.
 *
 * @author Christian Schlichtherle
 */
final class IdentityTransformation
implements Transformation<EntryNameWithDigest> {
    @Override public EntryNameWithDigest apply(
            EntryNameWithDigest entryNameWithDigest) {
        return entryNameWithDigest;
    }
}
