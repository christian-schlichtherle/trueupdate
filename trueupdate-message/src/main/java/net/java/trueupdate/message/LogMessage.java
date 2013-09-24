/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.message;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import static net.java.trueupdate.util.Objects.requireNonNull;
import static net.java.trueupdate.util.Strings.requireNonEmpty;

/**
 * A log message encapsulates the parameters for the method
 * {@link Logger#log(Level, String, Object[])}.
 * This class implements an immutable value object, so you can easily share its
 * instances with anyone or use them as map keys.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class LogMessage {

    private static final Object[] EMPTY = new Object[0];

    private final Level level;
    private final String message;
    private final Object[] parameters;

    @SuppressWarnings("null")
    LogMessage(final Builder<?> b) {
        this.level = requireNonNull(b.level);
        this.message = requireNonEmpty(b.message);
        this.parameters = null == b.parameters ? EMPTY : b.parameters.clone();
    }

    /** Returns a new log message which encapsulates the given parameters. */
    public static LogMessage create(Level level,
                                    String message,
                                    Object... parameters) {
        return builder()
                .level(level)
                .message(message)
                .parameters(parameters)
                .build();
    }

    /** Returns a new builder for a log record. */
    public static Builder<Void> builder() { return new Builder<Void>(); }

    /** Returns the logging level. */
    public Level level() { return level; }

    /** Returns the message. */
    public String message() { return message; }

    /** Returns a protective copy of the parameters. */
    public Object[] parameters() { return parameters.clone(); }

    /** Returns the number of parameters. */
    public int numberOfParameters() { return parameters.length; }

    /** Logs this message to the given logger. */
    public void log(Logger logger) {
        logger.log(level, message, parameters);
    }

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
                this.message.equals(that.message) &&
                Arrays.equals(this.parameters, that.parameters);
    }

    /** Returns a hash code which is consistent with {@link #equals(Object)}. */
    @Override public int hashCode() {
        int hash = 17;
        hash = 31 * hash + level.hashCode();
        hash = 31 * hash + message.hashCode();
        hash = 31 * hash + Arrays.hashCode(parameters);
        return hash;
    }

    /**
     * A builder for a log record.
     *
     * @param <P> The type of the parent builder.
     */
    @SuppressWarnings("PackageVisibleField")
    public static class Builder<P> {

        @CheckForNull Level level;
        @CheckForNull String message;
        @CheckForNull Object[] parameters;

        protected Builder() { }

        public Builder<P> level(final @Nullable Level level) {
            this.level = level;
            return this;
        }

        public Builder<P> message(final @Nullable String message) {
            this.message = message;
            return this;
        }

        @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
        public Builder<P> parameters(final @Nullable Object... parameters) {
            this.parameters = parameters;
            return this;
        }

        public LogMessage build() { return new LogMessage(this); }

        /**
         * Injects the product of this builder into the parent builder, if
         * defined.
         *
         * @throws IllegalStateException if there is no parent builder defined.
         */
        public P inject() {
            throw new IllegalStateException("No parent builder defined.");
        }
    } // Builder
}
