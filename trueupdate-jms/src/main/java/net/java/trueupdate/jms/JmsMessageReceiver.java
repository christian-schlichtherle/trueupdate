/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms;

import java.lang.reflect.UndeclaredThrowableException;
import javax.annotation.*;
import javax.annotation.concurrent.ThreadSafe;
import javax.jms.*;
import net.java.trueupdate.manager.spec.UpdateMessageListener;
import static net.java.trueupdate.util.Objects.requireNonNull;

/**
 * Receives JMS messages in a loop and forwards them to the given
 * {@link MessageListener}.
 * This {@link Runnable} may be run by a daemon {@link Thread} in environments
 * where it's not possible to use
 * {@link Session#setMessageListener(MessageListener)}, e.g. in a web&#160;app
 * or an enterprise&#160;app, and it's not desirable to use a message driven
 * bean.
 *
 * @author Christian Schlichtherle
 */
@ThreadSafe
public final class JmsMessageReceiver implements Runnable {

    private static final boolean NO_LOCAL = true;

    private final @WillCloseWhenClosed Connection connection;
    private final Destination destination;
    private final @CheckForNull String subscriptionName, messageSelector;
    private final MessageListener messageListener;

    private MessageConsumer messageConsumer;

    private JmsMessageReceiver(final Builder<?> b) throws JMSException {
        this.subscriptionName = b.subscriptionName;
        this.messageSelector = b.messageSelector;
        this.messageListener = requireNonNull(b.messageListener);
        this.destination = requireNonNull(b.destination);
        this.connection = b.connectionFactory.createConnection();
    }

    /** Returns a new builder for JMS message loops. */
    public static Builder<Void> builder() { return new Builder<Void>(); }

    @Override public void run() {
        /*if (null != messageConsumer)
            throw new java.lang.IllegalStateException();*/
        try {
            final Connection c = connection;
            try {
                final Session s = c.createSession(false, Session.CLIENT_ACKNOWLEDGE);
                final Destination d = destination;
                messageConsumer = d instanceof Topic
                        ? s.createDurableSubscriber((Topic) d, subscriptionName, messageSelector, NO_LOCAL)
                        : s.createConsumer(d, messageSelector);
                c.start();
                for (Message m; null != (m = messageConsumer.receive()); ) {
                    synchronized(this) {
                        if (null == messageConsumer) break;
                        m.acknowledge();
                        messageListener.onMessage(m);
                    }
                }
            } finally {
                c.close();
            }
        } catch (JMSException ex) {
            throw new UndeclaredThrowableException(ex);
        }
    }

    /**
     * Stops this runnable from a different thread.
     * After the call to this method, the client may safely assume that this
     * runnable will not call {@link MessageListener#onMessage} anymore and
     * will terminate as soon as possible.
     */
    public synchronized void stop() throws JMSException {
        // HC SUNT DRACONIS!
        final MessageConsumer mc = messageConsumer;
        if (null != mc) {
            mc.close();
            messageConsumer = null;
        }
    }

    @SuppressWarnings("PackageVisibleField")
    public static class Builder<T> {

        @Nullable ConnectionFactory connectionFactory;
        @CheckForNull Destination destination;
        @CheckForNull String messageSelector, subscriptionName;
        @CheckForNull MessageListener messageListener;

        protected Builder() { }

        public Builder<T> connectionFactory(final @Nullable ConnectionFactory connectionFactory) {
            this.connectionFactory = connectionFactory;
            return this;
        }

        public Builder<T> destination(final @Nullable Destination destination) {
            this.destination = destination;
            return this;
        }

        public Builder<T> subscriptionName(final @Nullable String subscriptionName) {
            this.subscriptionName = subscriptionName;
            return this;
        }

        public Builder<T> messageSelector(final @Nullable String messageSelector) {
            this.messageSelector = messageSelector;
            return this;
        }

        public Builder<T> messageListener(final @Nullable MessageListener messageListener) {
            this.messageListener = messageListener;
            return this;
        }

        public Builder<T> messageListener(final @CheckForNull UpdateMessageListener updateMessageListener) {
            this.messageListener = null == updateMessageListener
                    ? null
                    : JmsMessageListener.create(updateMessageListener);
            return this;
        }

        public JmsMessageReceiver build() throws JMSException {
            return new JmsMessageReceiver(this);
        }

        public T inject() {
            throw new java.lang.IllegalStateException("No target for injection.");
        }
    } // Builder
}
