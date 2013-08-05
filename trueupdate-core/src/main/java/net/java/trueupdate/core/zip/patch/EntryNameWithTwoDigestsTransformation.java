/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip.patch;

import net.java.trueupdate.core.zip.model.EntryNameAndDigest;
import net.java.trueupdate.core.zip.model.EntryNameAndTwoDigests;

/**
 * Selects the second entry name with digest from the given entry name with
 * two digests.
 *
 * @author Christian Schlichtherle
 */
final class EntryNameWithTwoDigestsTransformation
implements Transformation<EntryNameAndTwoDigests> {
    @Override public EntryNameAndDigest apply(
            EntryNameAndTwoDigests entryNameAndTwoDigests) {
        return entryNameAndTwoDigests.entryNameWithSecondDigest();
    }
}
