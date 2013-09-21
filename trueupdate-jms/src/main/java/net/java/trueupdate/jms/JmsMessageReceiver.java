/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms;

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

    private final Object lock = new Object();
    private final @Nullable String subscriptionName;
    private final @CheckForNull String messageSelector;
    private final MessageListener messageListener;
    private final Destination destination;
    private final ConnectionFactory connectionFactory;

    private MessageConsumer messageConsumer;

    private JmsMessageReceiver(final Builder<?> b) {
        this.subscriptionName = b.subscriptionName;
        this.messageSelector = b.messageSelector;
        this.messageListener = requireNonNull(b.messageListener);
        this.destination = requireNonNull(b.destination);
        this.connectionFactory = requireNonNull(b.connectionFactory);
    }

    /** Returns a new builder for JMS message loops. */
    public static Builder<Void> builder() { return new Builder<Void>(); }

    @Override public void run() {
        try {
            Connection c = null;
            try {
                MessageConsumer mc;
                synchronized (lock) {
                    mc = messageConsumer;
                    if (null != mc)
                        throw new java.lang.IllegalStateException("Already running.");
                    c = connectionFactory.createConnection();
                    final Session s = c.createSession(false, Session.CLIENT_ACKNOWLEDGE);
                    final Destination d = destination;
                    messageConsumer = mc = d instanceof Topic
                            ? s.createDurableSubscriber((Topic) d, subscriptionName, messageSelector, NO_LOCAL)
                            : s.createConsumer(d, messageSelector);
                }
                c.start();
                for (Message m; null != (m = mc.receive()); ) {
                    synchronized(lock) {
                        if (null == messageConsumer) break;
                        m.acknowledge();
                        messageListener.onMessage(m);
                    }
                }
            } finally {
                if (null != c) {
                    c.close();
                    synchronized (lock) { lock.notifyAll(); }
                }
            }
        } catch (JMSException ex) {
            throw new java.lang.IllegalStateException(ex);
        }
    }

    /**
     * Stops this runnable from a different thread.
     * When returning from this method, the client may safely assume that the
     * other thread is not executing the {@link #run()} method anymore.
     */
    public void stop() throws JMSException {
        synchronized (lock) {
            if (null == messageConsumer) return;
            messageConsumer.close();
            messageConsumer = null;
            while (true) try {
                lock.wait();
                break;
            } catch (InterruptedException dontStopTillYouDrop) {
            }
        }
    }

    @SuppressWarnings("PackageVisibleField")
    public static class Builder<T> {

        @CheckForNull ConnectionFactory connectionFactory;
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

        public Builder<T> updateMessageListener(
                final @CheckForNull UpdateMessageListener updateMessageListener) {
            this.messageListener = null == updateMessageListener
                    ? null
                    : JmsMessageListener.create(updateMessageListener);
            return this;
        }

        public JmsMessageReceiver build() {
            return new JmsMessageReceiver(this);
        }

        public T inject() {
            throw new java.lang.IllegalStateException("No target for injection.");
        }
    } // Builder
}
