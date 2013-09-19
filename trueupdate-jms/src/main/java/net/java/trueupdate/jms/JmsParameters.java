/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms;

import javax.annotation.*;
import javax.annotation.concurrent.Immutable;
import javax.jms.*;
import javax.naming.*;
import static net.java.trueupdate.util.Objects.requireNonNull;

/**
 * JMS parameters.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class JmsParameters {

    private final Context namingContext;
    private final ConnectionFactory connectionFactory;
    private final @CheckForNull Destination agent, manager;

    JmsParameters(final Builder<?> b) {
        this.namingContext = requireNonNull(b.namingContext);
        this.connectionFactory = requireNonNull(b.connectionFactory);
        this.agent = b.agent;
        this.manager = b.manager;
    }

    /** Returns a new builder for messaging parameters. */
    public static Builder<Void> builder() { return new Builder<Void>(); }

    /** Returns the naming context. */
    public Context namingContext() { return namingContext; }

    /** Returns the connection factory. */
    public ConnectionFactory connectionFactory() { return connectionFactory; }

    /** Returns the nullable destination for the update agent. */
    public @Nullable Destination agent() { return agent; }

    /** Returns the nullable destination for the update manager. */
    public @Nullable Destination manager() { return manager; }

    /**
     * A builder for messaging parameters.
     *
     * @param <P> The type of the parent builder.
     */
    @SuppressWarnings("PackageVisibleField")
    public static class Builder<P> {

        @CheckForNull Context namingContext;
        @CheckForNull ConnectionFactory connectionFactory;
        @CheckForNull Destination agent, manager;

        protected Builder() { }

        public Builder<P> namingContext(final @Nullable Context namingContext) {
            this.namingContext = namingContext;
            return this;
        }

        public Builder<P> connectionFactory(
                final @Nullable ConnectionFactory connectionFactory) {
            this.connectionFactory = connectionFactory;
            return this;
        }

        public Builder<P> agent(final @Nullable Destination agent) {
            this.agent = agent;
            return this;
        }

        public Builder<P> manager(final @Nullable Destination manager) {
            this.manager = manager;
            return this;
        }

        public JmsParameters build() { return new JmsParameters(this); }

        /**
         * Injects the product of this builder into the parent builder, if
         * defined.
         *
         * @throws IllegalStateException if there is no parent builder defined.
         */
        public P inject() {
            throw new java.lang.IllegalStateException("No parent builder defined.");
        }
    } // Builder
}
