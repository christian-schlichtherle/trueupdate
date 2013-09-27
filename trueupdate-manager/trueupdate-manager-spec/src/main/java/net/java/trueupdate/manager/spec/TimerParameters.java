/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.spec;

import java.util.Locale;
import java.util.concurrent.TimeUnit;
import javax.annotation.CheckForNull;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.manager.spec.dto.TimerParametersDto;
import static net.java.trueupdate.util.Objects.nonNullOr;
import static net.java.trueupdate.util.SystemProperties.resolve;
import net.java.trueupdate.util.builder.AbstractBuilder;

/**
 * Timer parameters.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class TimerParameters {

    private final long delay, period;
    private final TimeUnit unit;

    TimerParameters(final Builder<?> b) {
        this.delay = requireNonNegative(b.delay);
        this.period = requirePositive(b.period);
        this.unit = nonNullOr(b.unit, TimeUnit.MINUTES);
    }

    private static long requireNonNegative(final long l) {
        if (0 > l) throw new IllegalArgumentException();
        return l;
    }

    private static long requirePositive(final long l) {
        if (0 >= l) throw new IllegalArgumentException();
        return l;
    }

    /** Parses the given configuration item. */
    public static TimerParameters parse(TimerParametersDto ci) {
        return builder().parse(ci).build();
    }

    /** Returns a new builder for timer parameters. */
    public static Builder<Void> builder() { return new Builder<Void>(); }

    /** Returns the initial delay of the timer. */
    public long delay() { return delay; }

    /** Returns the period of the timer. */
    public long period() { return period; }

    /** Returns the time unit. */
    public TimeUnit unit() { return unit; }

    /**
     * A builder for timer parameters.
     *
     * @param <P> The type of the parent builder, if defined.
     */
    @SuppressWarnings("PackageVisibleField")
    public static class Builder<P> extends AbstractBuilder<P> {

        long delay, period;
        @CheckForNull TimeUnit unit;

        protected Builder() { }

        /** Selectively parses the given configuration item. */
        public final Builder<P> parse(final TimerParametersDto ci) {
            if (null != ci.delay)
                this.delay = Long.parseLong(resolve(ci.delay, "0"));
            if (null != ci.period)
                this.period = Long.parseLong(resolve(ci.period));
            if (null != ci.unit)
                this.unit = TimeUnit.valueOf(
                        resolve(ci.unit).toUpperCase(Locale.ENGLISH));
            return this;
        }

        public final Builder<P> delay(final long delay) {
            this.delay = delay;
            return this;
        }

        public final Builder<P> period(final long period) {
            this.period = period;
            return this;
        }

        public final Builder<P> delay(final TimeUnit unit) {
            this.unit = unit;
            return this;
        }

        @Override
        public TimerParameters build() { return new TimerParameters(this); }
    } // Builder
}
