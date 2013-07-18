/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.commons;

import java.util.Locale;
import javax.annotation.concurrent.Immutable;

/**
 * Represents a human readable string for the number of bytes transferred.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public class TransferSize {

    private final long sizeBytes;
    private final TransferUnit unit;

    /**
     * Constructs a transfer size.
     *
     * @param sizeBytes the number of transferred bytes.
     */
    public TransferSize(final long sizeBytes) {
        if (0 > sizeBytes) throw new IllegalArgumentException();
        this.sizeBytes = sizeBytes;
        final TransferUnit[] units = TransferUnit.values();
        for (int i = units.length; 0 <= --i;) {
            final TransferUnit unit = units[i];
            if (sizeBytes >= unit.threshold()) {
                this.unit = unit;
                return;
            }
        }
        throw new AssertionError();
    }

    /**
     * Returns a human readable string representation of the number of bytes
     * transferred.
     */
    @Override public String toString() { return toString(Locale.getDefault()); }

    /**
     * Returns a human readable string representation of the number of bytes
     * transferred for the given locale.
     */
    public String toString(Locale locale) {
        return unit.format(locale, sizeBytes);
    }

    private enum TransferUnit {

        bytes(0), KB(1024), MB(1024 * 1024), GB(1024 * 1024 * 1024);

        private final long threshold;

        TransferUnit(final long threshold) {
            assert 0 <= threshold;
            this.threshold = threshold;
        }

        long threshold() { return threshold; }
        double quotient() { return 0 != threshold ? threshold : 1; }

        String format(Locale locale, long sizeBytes) {
            return String.format(locale, "%,.2f %s", sizeBytes / quotient(),
                    name());
        }
    }
}
