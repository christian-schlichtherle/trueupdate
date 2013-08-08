/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.agent.ejb;

import java.util.concurrent.Callable;
import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * @author Christian Schlichtherle
 */
public final class Main implements Callable<Void> {

    private final ConnectionFactory connectionFactory;
    private final Queue queue;

    public static void main(String[] args) throws Exception {
        new Main(connectionFactory(), queue()).call();
    }

    private static ConnectionFactory connectionFactory() throws NamingException {
        return (ConnectionFactory) InitialContext.doLookup("java:comp/DefaultJMSConnectionFactory");
    }

    private static Queue queue() throws NamingException {
        return (Queue) InitialContext.doLookup("java:comp/jms/trueupdate-manager");
    }

    public Main(final ConnectionFactory connectionFactory, final Queue queue) {
        this.connectionFactory = connectionFactory;
        this.queue = queue;
    }

    @Override public Void call() throws Exception {
        return new MessageProducerTask<Void>() {
            @Override
            public Void use(final MessageProducer mp, final Session s, final Connection c)
            throws Exception {
                final Message m = s.createTextMessage("Hello world!");
                mp.send(m);
                return null;
            }
        }.call();
    }

    private abstract class MessageProducerTask<V> implements Callable<V> {

        @Override public V call() throws Exception {
            return new SessionTask<V>() {
                @Override public V use(final Session s, final Connection c)
                throws Exception {
                    final MessageProducer mp = s.createProducer(queue);
                    try {
                        return use(mp, s, c);
                    } finally {
                        mp.close();
                    }
                }
            }.call();
        }

        protected abstract V use(MessageProducer mp, Session s, Connection c)
        throws Exception;
    }

    private abstract class MessageConsumerTask<V> implements Callable<V> {

        @Override public V call() throws Exception {
            return new SessionTask<V>() {
                @Override public V use(final Session s, final Connection c)
                throws Exception {
                    final MessageConsumer mc = s.createConsumer(queue);
                    try {
                        return use(mc, s, c);
                    } finally {
                        mc.close();
                    }
                }
            }.call();
        }

        protected abstract V use(MessageConsumer mc, Session s, Connection c)
        throws Exception;
    }

    private abstract class SessionTask<V> implements Callable<V> {

        @Override public final V call() throws Exception {
            return new ConnectionTask<V>() {
                @Override public V use(Connection c) throws Exception {
                    final Session s = c
                            .createSession(false, Session.CLIENT_ACKNOWLEDGE);
                    try {
                        return use(s, c);
                    } finally {
                        s.close();
                    }
                }
            }.call();
        }

        protected abstract V use(Session s, Connection c) throws Exception;
    }

    private abstract class ConnectionTask<V> implements Callable<V> {

        @Override public final V call() throws Exception {
            final Connection c = connectionFactory.createConnection();
            try {
                return use(c);
            } finally {
                c.close();
            }
        }

        protected abstract V use(Connection c) throws Exception;
    }
}
