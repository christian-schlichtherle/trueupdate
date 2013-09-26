/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.message;

import java.util.Arrays;
import java.util.logging.Level;
import javax.annotation.*;
import javax.annotation.concurrent.Immutable;
import static net.java.trueupdate.util.Objects.requireNonNull;
import static net.java.trueupdate.util.Strings.requireNonEmpty;
import net.java.trueupdate.util.builder.AbstractBuilder;

/**
 * A log message encapsulates the data for the logging messages sent from
 * update managers to update agents.
 * This class implements an immutable value object, so you can easily share its
 * instances with anyone or use them as map keys.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class LogMessage {

    private static final Object[] EMPTY = new Object[0];

    private final Level level;
    private final String code;
    private final Object[] args;

    @SuppressWarnings("null")
    LogMessage(final Builder<?> b) {
        this.level = requireNonNull(b.level);
        this.code = requireNonEmpty(b.code);
        this.args = null == b.args ? EMPTY : b.args.clone();
    }

    /** Returns a new log message which encapsulates the given parameters. */
    public static LogMessage create(Level level,
                                    String code,
                                    Object... parameters) {
        return builder()
                .level(level)
                .code(code)
                .args(parameters)
                .build();
    }

    /** Returns a new builder for a log record. */
    public static Builder<Void> builder() { return new Builder<Void>(); }

    /** Returns the message level. */
    public Level level() { return level; }

    /** Returns the message code, which is a key for the message catalog. */
    public String code() { return code; }

    /** Returns a protective copy of the message parameters. */
    public Object[] args() { return args.clone(); }

    /** Returns the number of message parameters. */
    public int argsCount() { return args.length; }

    /**
     * Returns {@code true} if and only if the given object is a
     * {@code LogMessage} with equal properties.
     */
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    @Override public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof LogMessage)) return false;
        final LogMessage that = (LogMessage) obj;
        return  this.level.equals(that.level) &&
                this.code.equals(that.code) &&
                Arrays.equals(this.args, that.args);
    }

    /** Returns a hash code which is consistent with {@link #equals(Object)}. */
    @Override public int hashCode() {
        int hash = 17;
        hash = 31 * hash + level.hashCode();
        hash = 31 * hash + code.hashCode();
        hash = 31 * hash + Arrays.hashCode(args);
        return hash;
    }

    /**
     * A builder for a log record.
     *
     * @param <P> The type of the parent builder, if defined.
     */
    @SuppressWarnings("PackageVisibleField")
    public static class Builder<P> extends AbstractBuilder<P> {

        @CheckForNull Level level;
        @CheckForNull String code;
        @CheckForNull Object[] args;

        protected Builder() { }

        public final Builder<P> level(final @Nullable Level level) {
            this.level = level;
            return this;
        }

        public final Builder<P> code(final @Nullable String code) {
            this.code = code;
            return this;
        }

        @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
        public final Builder<P> args(final @Nullable Object... args) {
            this.args = args;
            return this;
        }

        @Override public final LogMessage build() {
            return new LogMessage(this);
        }
    } // Builder
}
