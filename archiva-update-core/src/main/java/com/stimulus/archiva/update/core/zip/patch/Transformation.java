/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.core.zip.patch;

import com.stimulus.archiva.update.core.zip.model.EntryNameAndDigest;

/**
 * Transforms an object into an {@link com.stimulus.archiva.update.core.zip.model.EntryNameAndDigest}.
 *
 * @param <T> the type of the objects to transform.
 * @author Christian Schlichtherle
 */
interface Transformation<T> {
    EntryNameAndDigest apply(T item);
}
