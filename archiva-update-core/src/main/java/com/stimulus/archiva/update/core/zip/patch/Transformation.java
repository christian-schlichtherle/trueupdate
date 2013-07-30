/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.core.zip.patch;

import com.stimulus.archiva.update.core.zip.model.EntryNameWithDigest;

/**
 * Transforms an object into an {@link EntryNameWithDigest}.
 *
 * @param <T> the type of the objects to transform.
 * @author Christian Schlichtherle
 */
interface Transformation<T> {
    EntryNameWithDigest apply(T item);
}
