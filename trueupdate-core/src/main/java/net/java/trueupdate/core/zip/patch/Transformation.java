/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip.patch;

import net.java.trueupdate.core.zip.model.EntryNameAndDigest;

/**
 * Transforms an object into an {@link net.java.trueupdate.core.zip.model.EntryNameAndDigest}.
 *
 * @param <T> the type of the objects to transform.
 * @author Christian Schlichtherle
 */
interface Transformation<T> {
    EntryNameAndDigest apply(T item);
}
