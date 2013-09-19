/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import static net.java.trueupdate.util.Strings.nonEmptyOr;
import net.java.trueupdate.util.SystemProperties;

/**
 * Provides functions for Data Transfer Objects (DTO).
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class DTO {

    private static final String
            INITIAL_CONTEXT_CLASS = InitialContext.class.getName(),
            LOOKUP = "java:comp/env";

    /** Creates new JMS parameters from the given DTOs. */
    public static JmsParameters parameters(
            final MessagingDto messaging,
            final @CheckForNull NamingDto naming) {
        return parameters(null != naming ? naming : new NamingDto(), messaging);
    }

    private static JmsParameters parameters(final NamingDto naming,
                                            final MessagingDto messaging) {
        final class Builder {

            final Context context;

            Builder() {
                try {
                    context = (Context) (
                            (Context) ctccl()
                                .loadClass(resolve(naming.initialContextClass, INITIAL_CONTEXT_CLASS))
                                .newInstance()
                            ).lookup(resolve(naming.lookup, LOOKUP));
                } catch (Exception ex) {
                    throw new IllegalArgumentException(ex);
                }
            }

            ClassLoader ctccl() {
                return Thread.currentThread().getContextClassLoader();
            }

            String resolve(@CheckForNull String string, String defaultValue) {
                return nonEmptyOr(null == string ? null : SystemProperties.resolve(string), defaultValue);
            }

            JmsParameters build() {
                try {
                    return JmsParameters
                            .builder()
                            .namingContext(context)
                            .connectionFactory((ConnectionFactory) lookup(messaging.connectionFactory))
                            .agent((Destination) lookup(messaging.agent))
                            .manager((Destination) lookup(messaging.manager))
                            .build();
                } catch (NamingException ex) {
                    throw new IllegalArgumentException(ex);
                }
            }

            @Nullable Object lookup(@CheckForNull String name) throws NamingException {
                return null == name ? null : context.lookup(SystemProperties.resolve(name));
            }
        } // Builder

        return new Builder().build();
    }

    private DTO() { }
}
