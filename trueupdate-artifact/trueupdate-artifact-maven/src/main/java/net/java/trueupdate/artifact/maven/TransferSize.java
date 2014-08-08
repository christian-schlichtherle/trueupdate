/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.artifact.maven;

import java.io.Serializable;
import java.util.Locale;
import javax.annotation.concurrent.Immutable;

/**
 * A value object which represents a human readable string for the number of
 * bytes transferred.
 *
 * @author Christian Schlichtherle
 */
@Immutable
class TransferSize implements Serializable {

    private static final long serialVersionUID = 0L;

    private final double amount;
    private final TransferUnit unit;

    /**
     * Constructs a transfer size.
     *
     * @param sizeBytes the number of transferred bytes.
     */
    public TransferSize(final long sizeBytes) {
        if (0 > sizeBytes) throw new IllegalArgumentException();
        final TransferUnit[] units = TransferUnit.values();
        for (int i = units.length; 0 <= --i;) {
            final TransferUnit unit = units[i];
            if (sizeBytes >= unit.threshold()) {
                this.amount = sizeBytes / unit.quotient();
                this.unit = unit;
                return;
            }
        }
        throw new AssertionError();
    }

    @Override public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TransferSize)) return false;
        final TransferSize that = (TransferSize) obj;
        return Double.compare(this.amount, that.amount) == 0 &&
                this.unit == that.unit;
    }

    @Override public int hashCode() {
        final long temp = Double.doubleToLongBits(amount);
        int c;
        c = (int) (temp ^ (temp >>> 32));
        c = 31 * c + unit.hashCode();
        return c;
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
        return unit.format(locale, amount);
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

        String format(Locale locale, double amount) {
            return String.format(locale, "%,.2f %s", amount, name());
        }
    }
}
