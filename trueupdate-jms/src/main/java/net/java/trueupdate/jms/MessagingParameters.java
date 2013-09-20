/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms;

import javax.annotation.*;
import javax.annotation.concurrent.Immutable;
import javax.jms.*;
import javax.naming.*;
import net.java.trueupdate.jms.ci.MessagingDto;
import net.java.trueupdate.jms.ci.NamingDto;
import static net.java.trueupdate.util.Objects.requireNonNull;
import static net.java.trueupdate.util.SystemProperties.resolve;

/**
 * Messaging parameters.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class MessagingParameters {

    private final Context namingContext;
    private final ConnectionFactory connectionFactory;
    private final String fromName;
    private final Destination fromDestination;
    private final @CheckForNull String toName;
    private final @CheckForNull Destination toDestination;

    MessagingParameters(final Builder<?> b) {
        try {
            // HC SVNT DRACONIS
            this.namingContext = requireNonNull(b.namingContext);
            this.connectionFactory = lookup(b.connectionFactory);
            this.fromDestination = lookup(this.fromName = b.from);
            this.toDestination = lookupNullable(this.toName = b.to);
        } catch (NamingException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    private @Nullable <T> T lookupNullable(@CheckForNull String name) throws NamingException {
        return null == name ? null : (T) lookup(name);
    }

    @SuppressWarnings("unchecked")
    private <T> T lookup(String name) throws NamingException {
        return (T) namingContext.lookup(resolve(name));
    }

    /** Returns a new builder for messaging parameters. */
    public static Builder<Void> builder() { return new Builder<Void>(); }

    /** Returns the naming context. */
    public Context namingContext() { return namingContext; }

    /** Returns the connection factory. */
    public ConnectionFactory connectionFactory() { return connectionFactory; }

    /** Returns the JNDI name of the sender. */
    public String fromName() { return fromName; }

    /** Returns the destination for the sender. */
    public Destination fromDestination() { return fromDestination; }

    /** Returns the nullable JNDI name for the recipient. */
    public @Nullable String toName() { return toName; }

    /** Returns the nullable destination for the recipient. */
    public @Nullable Destination toDestination() { return toDestination; }

    /**
     * A builder for messaging parameters.
     *
     * @param <P> The type of the parent builder.
     */
    @SuppressWarnings("PackageVisibleField")
    public static class Builder<P> {

        private static final Context defaultContext;
        static {
            try {
                defaultContext = (Context) new InitialContext()
                        .lookup("java:comp/env");
            } catch (NamingException ex) {
                throw new java.lang.IllegalStateException(ex);
            }
        }

        @CheckForNull Context namingContext = defaultContext;
        @CheckForNull String connectionFactory, from, to;

        protected Builder() { }

        public Builder<P> namingContext(final @Nullable Context namingContext) {
            this.namingContext = namingContext;
            return this;
        }

        public Builder<P> connectionFactory(final @Nullable String connectionFactory) {
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

        /**
         * Parses the given nullable configuration for the naming context.
         */
        public Builder<P> parseNaming(final @CheckForNull NamingDto ci) {
            if (null != ci) {
                try {
                    namingContext = (Context) (
                            (Context) Thread
                                .currentThread()
                                .getContextClassLoader()
                                .loadClass(resolve(ci.initialContextClass))
                                .newInstance()
                            ).lookup(resolve(ci.relativePath));
                } catch (Exception ex) {
                    throw new IllegalArgumentException(ex);
                }
            }
            return this;
        }

        /**
         * Parses the given nullable configuration for the JMS administered
         * objects.
         * Prior to calling this method, the naming context must be already
         * configured.
         *
         * @see #namingContext(Context)
         * @see #parseNaming(NamingDto)
         */
        public Builder<P> parseMessaging(final MessagingDto ci) {
            connectionFactory = resolve(ci.connectionFactory, connectionFactory);
            from = resolve(ci.from, from);
            to = resolve(ci.to, to);
            return this;
        }

        public MessagingParameters build() {
            return new MessagingParameters(this);
        }

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
