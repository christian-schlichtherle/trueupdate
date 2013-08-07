/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.client;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.Callable;
import javax.annotation.Resource;
import javax.jms.*;
import javax.servlet.*;

/**
 * @author Christian Schlichtherle
 */
public final class Main implements ServletContextListener {

    @Resource // Source: http://activemq.apache.org/tomcat.html
    //@Resource(lookup = "jms/ConnectionFactory") // Doesn't work with GlassFish! Source: http://docs.oracle.com/javaee/6/tutorial/doc/bnceh.html#bncek
    //@Resource(lookup = "java:comp/DefaultJMSConnectionFactory") // Source: http://docs.oracle.com/javaee/7/tutorial/doc/jms-concepts003.htm#BNCEK
    private ConnectionFactory connectionFactory;

    @Resource(lookup = "jms/updates")
    private Queue updates;

    @Override public void contextInitialized(final ServletContextEvent sce) {
        wrap(new MessageConsumerTask<Void>() {
            @Override
            public Void use(final MessageConsumer mc, final Session s, final Connection c)
            throws Exception {
                c.start();
                try {
                    System.out.println("--- BEGIN MESSAGES ---");
                    Message m;
                    while (null != (m = mc.receiveNoWait())) {
                        System.out.println("Reply To: " + m.getJMSReplyTo());
                        if (m instanceof TextMessage)
                            System.out.println("Body: " + ((TextMessage) m).getText());
                        m.acknowledge();
                    }
                    System.out.println("---  END MESSAGES  ---");
                } finally {
                    //c.stop();
                }
                return null;
            }
        });
    }

    @Override public void contextDestroyed(final ServletContextEvent sce) {
        wrap(new MessageProducerTask<Void>() {
            @Override
            public Void use(final MessageProducer mp, final Session s, final Connection c)
            throws Exception {
                final Message m = s.createTextMessage("Hello world!");
                mp.send(m);
                return null;
            }
        });
    }

    private static <V> V wrap(final Callable<V> task) {
        try { return task.call(); }
        catch (Exception ex) { throw new UndeclaredThrowableException(ex); }
    }

    private abstract class MessageProducerTask<V> implements Callable<V> {

        @Override public V call() throws Exception {
            return new SessionTask<V>() {
                @Override public V use(final Session s, final Connection c)
                throws Exception {
                    final MessageProducer mp = s.createProducer(updates);
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
                    final MessageConsumer mc = s.createConsumer(updates);
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
