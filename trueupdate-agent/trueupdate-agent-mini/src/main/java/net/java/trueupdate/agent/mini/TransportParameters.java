/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.mini;

import javax.annotation.*;
import javax.annotation.concurrent.Immutable;
import javax.naming.*;
import static net.java.trueupdate.util.Strings.requireNonEmpty;

/**
 * Transport parameters for JMS.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class TransportParameters {

    private final Context context;
    private final String connectionFactory, from, to;

    TransportParameters(final Builder<?> b) {
        this.context = nonNullOrNewInitialContext(b.context);
        this.connectionFactory = requireNonEmpty(b.connectionFactory);
        this.from = requireNonEmpty(b.from);
        this.to = requireNonEmpty(b.to);
    }

    private static Context nonNullOrNewInitialContext(final Context context) {
        try {
            return null != context ? context : new InitialContext();
        } catch (NamingException ex) {
            throw new IllegalStateException(
                    "Cannot create a new javax.naming.InitialContext() for you, so you need to inject a javax.naming.Context.",
                    ex);
        }
    }

    /** Returns a new builder for transport parameters. */
    public static Builder<Void> builder() { return new Builder<Void>(); }

    public Context context() { return context; }

    public String connectionFactory() { return connectionFactory; }

    public String from() { return from; }

    public String to() { return to; }

    /**
     * A builder for transport parameters.
     *
     * @param <P> The type of the parent builder.
     */
    @SuppressWarnings("PackageVisibleField")
    public static class Builder<P> {

        @CheckForNull Context context;
        @CheckForNull String connectionFactory, from, to;

        protected Builder() { }

        public Builder<P> context(final @Nullable Context context) {
            this.context = context;
            return this;
        }

        public Builder<P> connectionFactory(
                final @Nullable String connectionFactory) {
            this.connectionFactory = connectionFactory;
            return this;
        }

        public Builder<P> from(final @Nullable String from) {
            this.from = from;
            return this;
        }

        public Builder<P> to(final @Nullable String to) {
            this.to = to;
            return this;
        }

        public TransportParameters build() {
            return new TransportParameters(this);
        }

        /**
         * Injects the product of this builder into the parent builder, if
         * defined.
         *
         * @throws IllegalStateException if there is no parent builder defined.
         */
        public P inject() throws NamingException {
            throw new IllegalStateException("No parent builder defined.");
        }
    } // Builder
}
