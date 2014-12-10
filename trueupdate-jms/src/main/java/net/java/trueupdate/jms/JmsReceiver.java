/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.Topic;
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

    public static final ThreadFactory LISTENER_THREAD_FACTORY =
            new ThreadFactory() {
                @Override public Thread newThread(Runnable r) {
                    return new Thread(r, "TrueUpdate JMS / Listener");
                }
            };

    private static final boolean NO_LOCAL = true;

    private final Object lock = new Object();
    private final @Nullable String subscriptionName;
    private final @CheckForNull String messageSelector;
    private final MessageListener messageListener;
    private final Destination destination;
    private final ConnectionFactory connectionFactory;
    private final ExecutorService executorService;

    private Connection connection;

    // Work around https://java.net/jira/browse/GLASSFISH-20836 .
    private volatile Thread thread;

    private JmsReceiver(final Builder<?> b) {
        this.subscriptionName = b.subscriptionName;
        this.messageSelector = b.messageSelector;
        this.messageListener = new JmsListener(b.updateMessageListener);
        this.destination = requireNonNull(b.destination);
        this.connectionFactory = requireNonNull(b.connectionFactory);
        this.executorService = requireNonNull(b.executorService);
    }

    /** Returns a new builder for JMS receivers. */
    public static Builder<Void> builder() { return new Builder<Void>(); }

    @Override public void run() {
        try {
            Connection c = null;
            try {
                MessageConsumer mc;
                synchronized (lock) {
                    c = connection;
                    if (null != c)
                        throw new java.lang.IllegalStateException("Already running.");
                    c = connectionFactory.createConnection();
                    final Session s = c.createSession(false, Session.CLIENT_ACKNOWLEDGE);
                    final Destination d = destination;
                    mc = d instanceof Topic
                            ? s.createDurableSubscriber((Topic) d, subscriptionName, messageSelector, NO_LOCAL)
                            : s.createConsumer(d, messageSelector);
                    thread = Thread.currentThread();
                    connection = c;
                }
                c.start();
                while (true) {
                    final Message m = mc.receive();
                    if (null == m) break;
                    synchronized(lock) {
                        if (null == connection) break;
                        m.acknowledge();
                        executorService.execute(new Runnable() {
                            @Override public void run() {
                                messageListener.onMessage(m);
                            }
                        });
                    }
                }
            } finally {
                if (null != c) {
                    // c.close(); // see https://issues.apache.org/jira/browse/AMQ-4769 .
                    synchronized (lock) {
                        thread = null;
                        lock.notifyAll();
                    }
                }
            }
        } catch (JMSException ex) {
            throw new java.lang.IllegalStateException(ex);
        }
    }

    /** Stops this runnable from a different thread. */
    public void stop(long timeout, TimeUnit unit) throws Exception {
        // HC SVNT DRACONIS
        // The following code is a fucking mess, but that's just because the
        // JMS implementations Apache ActiveMQ 5.8.0 and Open MQ 5.0 are
        // riddled with bugs - SCNR!

        final long stop = System.currentTimeMillis() + unit.toMillis(timeout);
        unit = TimeUnit.MILLISECONDS;

        synchronized (lock) {
            final Connection c = connection;
            if (null == c) return;
            connection = null;

            // Work around https://java.net/jira/browse/GLASSFISH-20836 .
            executorService.submit(new Runnable() {
                @SuppressWarnings("SleepWhileInLoop")
                @Override public void run() {
                    for (Thread t; null != (t = thread); ) {
                        // Wait first to give c.close() priority and avoid a
                        // JMSException with a wrapped InterruptedException
                        // when using ActiveMQ 5.8.0.
                        try { Thread.sleep(100); }
                        catch (InterruptedException stop) { }
                        t.interrupt();
                    }
                }
            });

            try {
                c.close();
            } finally {
                try {
                    timeout = Math.max(1, stop - System.currentTimeMillis());
                    lock.wait(timeout);
                    if (null != thread) throw new TimeoutException();
                } finally {
                    if (0 != executorService.shutdownNow().size())
                        throw new AssertionError();
                    timeout = Math.max(1, stop - System.currentTimeMillis());
                    if (!executorService.awaitTermination(timeout, unit))
                        throw new TimeoutException();
                }
            }
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
        @CheckForNull UpdateMessageListener updateMessageListener;
        @CheckForNull ExecutorService executorService;

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

        public final Builder<P> updateMessageListener(
                final @Nullable UpdateMessageListener updateMessageListener) {
            this.updateMessageListener = updateMessageListener;
            return this;
        }

        public final Builder<P> executorService(
                final @Nullable ExecutorService executorService) {
            this.executorService = executorService;
            return this;
        }

        @Override public final JmsReceiver build() {
            return new JmsReceiver(this);
        }
    } // Builder
}
