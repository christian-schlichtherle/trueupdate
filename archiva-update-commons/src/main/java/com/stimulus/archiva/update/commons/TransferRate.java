/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.commons;

import java.util.Locale;
import javax.annotation.concurrent.Immutable;

/**
 * Represents a human readable string for the number of bytes transferred per
 * second.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public class TransferRate extends TransferSize {

    /**
     * Constructs a transfer rate.
     *
     * @param sizeBytes the number of transferred bytes.
     * @param durationMillis the duration of the transfer in milliseconds.
     */
    public TransferRate(final long sizeBytes, final long durationMillis) {
        super(sizeBytes * 1000 / Math.max(1, durationMillis));
        if (0 > sizeBytes) throw new IllegalArgumentException();
        if (0 > durationMillis) throw new IllegalArgumentException();
    }

    /**
     * Returns a human readable string representation of the number of bytes
     * transferred per second.
     */
    @Override public String toString(Locale locale) {
        return super.toString(locale) + " per second";
    }
}
