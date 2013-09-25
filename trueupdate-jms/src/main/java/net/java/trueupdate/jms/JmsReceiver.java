/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms;

import java.util.concurrent.*;
import javax.annotation.*;
import javax.annotation.concurrent.ThreadSafe;
import javax.jms.*;
import net.java.trueupdate.message.UpdateMessageListener;
import static net.java.trueupdate.util.Objects.requireNonNull;
import net.java.trueupdate.util.builder.AbstractBuilder;

/**
 * Receives JMS messages in a loop and forwards them to the given
 * {@link MessageListener}.
 * This {@link Runnable} should be run by a {@link Thread} in environments
 * where it's not possible to use
 * {@link Session#setMessageListener(MessageListener)}, e.g. in a web&#160;app
 * or an enterprise&#160;app, and it's not desirable to use a message driven
 * bean.
 *
 * @author Christian Schlichtherle
 */
@ThreadSafe
public final class JmsReceiver implements Runnable {

    private static final ExecutorService listeners =
            Executors.newCachedThreadPool(new ThreadFactory() {
                @Override public Thread newThread(Runnable r) {
                    return new Thread(r, "TrueUpdate JMS / Message Listener Thread");
                }
            });

    private static final boolean NO_LOCAL = true;

    private final Object lock = new Object();
    private final @Nullable String subscriptionName;
    private final @CheckForNull String messageSelector;
    private final MessageListener messageListener;
    private final Destination destination;
    private final ConnectionFactory connectionFactory;

    private MessageConsumer messageConsumer;

    private JmsReceiver(final Builder<?> b) {
        this.subscriptionName = b.subscriptionName;
        this.messageSelector = b.messageSelector;
        this.messageListener = requireNonNull(b.messageListener);
        this.destination = requireNonNull(b.destination);
        this.connectionFactory = requireNonNull(b.connectionFactory);
    }

    /** Returns a new builder for JMS receivers. */
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
                while (true) {
                    final Message m = mc.receive();
                    if (null == m) break;
                    synchronized(lock) {
                        if (null == messageConsumer) break;
                        m.acknowledge();
                        listeners.execute(new Runnable() {
                            @Override public void run() {
                                messageListener.onMessage(m);
                            }
                        });
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
    public void stop(long timeout, TimeUnit unit) throws Exception {
        synchronized (lock) {
            if (null == messageConsumer) return;
            // HC SVNT DRACONIS
            messageConsumer.close();
            if (0 != listeners.shutdownNow().size())
                throw new AssertionError();
            listeners.awaitTermination(timeout, unit);
            messageConsumer = null;
            lock.wait();
        }
    }

    /**
     * A builder for a JMS receiver.
     *
     * @param <P> The type of the parent builder, if defined.
     */
    @SuppressWarnings("PackageVisibleField")
    public static class Builder<P> extends AbstractBuilder<P> {

        @CheckForNull ConnectionFactory connectionFactory;
        @CheckForNull Destination destination;
        @CheckForNull String messageSelector, subscriptionName;
        @CheckForNull MessageListener messageListener;

        protected Builder() { }

        public final Builder<P> connectionFactory(
                final @Nullable ConnectionFactory connectionFactory) {
            this.connectionFactory = connectionFactory;
            return this;
        }

        public final Builder<P> destination(
                final @Nullable Destination destination) {
            this.destination = destination;
            return this;
        }

        public final Builder<P> subscriptionName(
                final @Nullable String subscriptionName) {
            this.subscriptionName = subscriptionName;
            return this;
        }

        public final Builder<P> messageSelector(
                final @Nullable String messageSelector) {
            this.messageSelector = messageSelector;
            return this;
        }

        public final Builder<P> messageListener(
                final @Nullable MessageListener messageListener) {
            this.messageListener = messageListener;
            return this;
        }

        public final Builder<P> updateMessageListener(
                final @CheckForNull UpdateMessageListener updateMessageListener) {
            this.messageListener = null == updateMessageListener
                    ? null
                    : JmsListener.create(updateMessageListener);
            return this;
        }

        @Override public final JmsReceiver build() {
            return new JmsReceiver(this);
        }
    } // Builder
}
