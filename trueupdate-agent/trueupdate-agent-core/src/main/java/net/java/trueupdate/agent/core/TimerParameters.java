/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.core;

import java.util.Locale;
import java.util.concurrent.TimeUnit;
import javax.annotation.CheckForNull;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.agent.core.ci.TimerParametersCi;
import static net.java.trueupdate.util.SystemProperties.resolve;
import net.java.trueupdate.util.builder.AbstractBuilder;

/**
 * Timer parameters.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class TimerParameters {

    private final long delay;
    private final TimeUnit unit;

    TimerParameters(final Builder<?> b) {
        this.delay = requireNonNegative(b.delay);
        if (null != b.unit) {
            this.unit = b.unit;
        } else {
            if (0 != this.delay) throw new IllegalArgumentException();
            this.unit = TimeUnit.MILLISECONDS;
        }
    }

    private static long requireNonNegative(final long l) {
        if (0 > l) throw new IllegalArgumentException();
        return l;
    }

    /** Parses the given configuration item. */
    public static TimerParameters parse(TimerParametersCi ci) {
        return builder().parse(ci).build();
    }

    /** Returns a new builder for timer parameters. */
    public static Builder<Void> builder() { return new Builder<Void>(); }

    /** Returns the initial delay of the timer. */
    public long delay() { return delay; }

    /** Returns the time unit. */
    public TimeUnit unit() { return unit; }

    /**
     * A builder for timer parameters.
     *
     * @param <P> The type of the parent builder, if defined.
     */
    @SuppressWarnings("PackageVisibleField")
    public static class Builder<P> extends AbstractBuilder<P> {

        long delay;
        @CheckForNull TimeUnit unit;

        protected Builder() { }

        /** Selectively parses the given configuration item. */
        public final Builder<P> parse(final TimerParametersCi ci) {
            if (null != ci.delay)
                this.delay = Long.parseLong(resolve(ci.delay, "0"));
            if (null != ci.unit)
                this.unit = TimeUnit.valueOf(
                        resolve(ci.unit).toUpperCase(Locale.ENGLISH));
            return this;
        }

        public final Builder<P> delay(final long delay) {
            this.delay = delay;
            return this;
        }

        public final Builder<P> unit(final TimeUnit unit) {
            this.unit = unit;
            return this;
        }

        @Override
        public TimerParameters build() { return new TimerParameters(this); }
    } // Builder
}
