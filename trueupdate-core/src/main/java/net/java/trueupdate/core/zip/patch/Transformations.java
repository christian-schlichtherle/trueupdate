/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip.patch;

import net.java.trueupdate.core.zip.model.EntryNameAndDigest;
import net.java.trueupdate.core.zip.model.EntryNameAndTwoDigests;

/**
 * Transforms an object into an {@link EntryNameAndDigest} by applying some
 * dark magic.
 *
 * @param <T> the type of the objects to transform.
 * @author Christian Schlichtherle
 */
interface Transformation<T> {

    EntryNameAndDigest apply(T item);
}

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