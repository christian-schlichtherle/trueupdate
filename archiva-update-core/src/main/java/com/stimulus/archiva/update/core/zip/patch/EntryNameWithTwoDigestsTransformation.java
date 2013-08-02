/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.core.zip.patch;

import com.stimulus.archiva.update.core.zip.model.EntryNameAndDigest;
import com.stimulus.archiva.update.core.zip.model.EntryNameAndTwoDigests;

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
