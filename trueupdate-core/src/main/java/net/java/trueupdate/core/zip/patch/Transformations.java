/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip.patch;

import net.java.trueupdate.core.zip.model.ZipEntryNameAndDigestValue;
import net.java.trueupdate.core.zip.model.ZipEntryNameAndTwoDigestValues;

/**
 * Transforms an object into an {@link net.java.trueupdate.core.zip.model.ZipEntryNameAndDigestValue} by applying some
 * dark magic.
 *
 * @param <T> the type of the objects to transform.
 * @author Christian Schlichtherle
 */
interface Transformation<T> {

    ZipEntryNameAndDigestValue apply(T item);
}

/**
 * The identity transformation.
 *
 * @author Christian Schlichtherle
 */
final class IdentityTransformation
implements Transformation<ZipEntryNameAndDigestValue> {

    @Override public ZipEntryNameAndDigestValue apply(
            ZipEntryNameAndDigestValue zipEntryNameAndDigestValue) {
        return zipEntryNameAndDigestValue;
    }
}

/**
 * Selects the second entry name with digest from the given entry name with
 * two digests.
 *
 * @author Christian Schlichtherle
 */
final class ZipEntryNameWithTwoDigestsTransformation
        implements Transformation<ZipEntryNameAndTwoDigestValues> {

    @Override public ZipEntryNameAndDigestValue apply(
            ZipEntryNameAndTwoDigestValues zipEntryNameAndTwoDigestValues) {
        return zipEntryNameAndTwoDigestValues.entryNameWithSecondDigest();
    }
}
