/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms;

import javax.annotation.*;
import javax.annotation.concurrent.Immutable;
import javax.jms.*;
import javax.naming.*;
import net.java.trueupdate.jms.ci.*;
import static net.java.trueupdate.util.SystemProperties.resolve;
import net.java.trueupdate.util.builder.AbstractBuilder;

/**
 * Messaging parameters.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class JmsParameters {

    private final Context namingContext;
    private final ConnectionFactory connectionFactory;
    private final String fromName;
    private final Destination fromDestination;
    private final @CheckForNull String toName;

    JmsParameters(final Builder<?> b) {
        try {
            // HC SVNT DRACONIS
            this.namingContext = null != b.namingContext
                    ? b.namingContext
                    : namingContext(new JndiParametersCi());
            this.connectionFactory = lookup(b.connectionFactory);
            this.fromDestination = lookup(this.fromName = b.from);
            this.toName = b.to;
        } catch (NamingException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    static Context namingContext(final JndiParametersCi ci) {
        try {
            final Context context = null == ci.initialContextClass
                    ? new InitialContext()
                    : (Context) Thread
                        .currentThread()
                        .getContextClassLoader()
                        .loadClass(resolve(ci.initialContextClass))
                        .newInstance();
            return null == ci.contextLookup
                    ? context
                    : (Context) context.lookup(resolve(ci.contextLookup));
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T lookup(String name) throws NamingException {
        return (T) namingContext.lookup(resolve(name));
    }

    /** Parses the given configuration item. */
    public static JmsParameters parse(JmsParametersCi messaging) {
        return builder().parse(messaging).build();
    }

    /** Returns a new builder for messaging parameters. */
    public static Builder<Void> builder() { return new Builder<Void>(); }

    /** Returns the naming context. */
    public Context namingContext() { return namingContext; }

    /** Returns the connection factory. */
    public ConnectionFactory connectionFactory() { return connectionFactory; }

    /** Returns the JNDI name of the JMS destination for the sender. */
    public String fromName() { return fromName; }

    /** Returns the JMS destination for the sender. */
    public Destination fromDestination() { return fromDestination; }

    /** Returns the nullable JNDI name of the JMS destination for the recipient. */
    public @Nullable String toName() { return toName; }

    /**
     * A builder for messaging parameters.
     *
     * @param <P> The type of the parent builder, if defined.
     */
    @SuppressWarnings("PackageVisibleField")
    public static class Builder<P> extends AbstractBuilder<P> {

        @CheckForNull Context namingContext;
        @CheckForNull String connectionFactory, from, to;

        protected Builder() { }

        /** Selectively parses the given configuration item. */
        public final Builder<P> parse(final JmsParametersCi ci) {
            if (null != ci.naming) namingContext =
                    JmsParameters.namingContext(ci.naming);
            connectionFactory = resolve(ci.connectionFactory, connectionFactory);
            from = resolve(ci.from, from);
            to = resolve(ci.to, to);
            return this;
        }

        public final Builder<P> namingContext(
                final @Nullable Context namingContext) {
            this.namingContext = namingContext;
            return this;
        }

        public final Builder<P> connectionFactory(
                final @Nullable String connectionFactory) {
            this.connectionFactory = connectionFactory;
            return this;
        }

        public final Builder<P> from(final @Nullable String from) {
            this.from = from;
            return this;
        }

        public final Builder<P> to(final @Nullable String to) {
            this.to = to;
            return this;
        }

        @Override public final JmsParameters build() {
            return new JmsParameters(this);
        }
    } // Builder
}
