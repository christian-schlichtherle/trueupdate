/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip.patch;

import net.java.trueupdate.core.zip.model.EntryNameAndDigest;

/**
 * The identity transformation.
 *
 * @author Christian Schlichtherle
 */
final class IdentityTransformation
implements Transformation<EntryNameAndDigest> {
    @Override public EntryNameAndDigest apply(
            EntryNameAndDigest entryNameAndDigest) {
        return entryNameAndDigest;
    }
}
