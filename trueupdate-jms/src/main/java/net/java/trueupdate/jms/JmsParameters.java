/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms;

import javax.annotation.*;
import javax.annotation.concurrent.Immutable;
import javax.jms.*;
import javax.naming.*;
import static net.java.trueupdate.util.SystemProperties.resolve;

/**
 * JMS parameters.
 *
 * @author Christian Schlichtherle
 */
@Immutable
@SuppressWarnings("unchecked")
public final class JmsParameters {

    private final Context namingContext;
    private final ConnectionFactory connectionFactory;
    private final @CheckForNull Destination agent, manager;

    public JmsParameters(final NamingDescriptor namingDesriptor,
                         final MessagingDescriptor messagingDescriptor) {
        try {
            namingContext = (Context) loadContext(namingDesriptor.initialContextClass())
                    .lookup(resolve(namingDesriptor.lookup()));
            connectionFactory = lookup(messagingDescriptor.connectionFactory());
            agent = lookup(messagingDescriptor.agent());
            manager = lookup(messagingDescriptor.manager());
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    private static Context loadContext(String name) throws Exception {
        return (Context) Thread
                .currentThread()
                .getContextClassLoader()
                .loadClass(resolve(name))
                .newInstance();
    }

    private <T> T lookup(@CheckForNull String name) throws NamingException {
        return null == name ? null : (T) namingContext.lookup(resolve(name));
    }

    /** Returns the naming context. */
    public Context namingContext() { return namingContext; }

    /** Returns the connection factory. */
    public ConnectionFactory connectionFactory() { return connectionFactory; }

    /** Returns the nullable destination for the update agent. */
    public @Nullable Destination agent() { return agent; }

    /** Returns the nullable destination for the update manager. */
    public @Nullable Destination manager() { return manager; }
}
