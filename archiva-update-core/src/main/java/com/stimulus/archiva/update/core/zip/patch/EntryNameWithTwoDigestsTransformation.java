/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.core.zip.patch;

import com.stimulus.archiva.update.core.zip.model.EntryNameWithDigest;
import com.stimulus.archiva.update.core.zip.model.EntryNameWithTwoDigests;

/**
 * Selects the second entry name with digest from the given entry name with
 * two digests.
 *
 * @author Christian Schlichtherle
 */
final class EntryNameWithTwoDigestsTransformation
implements Transformation<EntryNameWithTwoDigests> {
    @Override public EntryNameWithDigest apply(
            EntryNameWithTwoDigests entryNameWithTwoDigests) {
        return entryNameWithTwoDigests.entryNameWithSecondDigest();
    }
}
