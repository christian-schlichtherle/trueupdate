/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.jms;

import java.util.ServiceLoader;
import javax.annotation.WillCloseWhenClosed;
import javax.annotation.concurrent.ThreadSafe;
import javax.jms.*;
import javax.naming.Context;
import net.java.trueupdate.jaxrs.client.UpdateClient;
import net.java.trueupdate.jms.*;
import net.java.trueupdate.manager.core.*;
import net.java.trueupdate.manager.spec.*;
import net.java.trueupdate.message.UpdateMessage;

/**
 * An implementation of the abstract update manager class with minimal
 * dependencies.
 * The public methods of this class are thread safe, all others are not.
 *
 * @author Christian Schlichtherle
 */
@ThreadSafe
final class JmsUpdateManager extends BasicUpdateManager {

    private final UpdateClient updateClient;
    private final UpdateInstaller updateInstaller;
    private final ConnectionFactory connectionFactory;
    private final Context namingContext;

    private volatile @WillCloseWhenClosed Connection connection;

    JmsUpdateManager(final JmsUpdateManagerParameters parameters) {
        this.updateClient = new UpdateClient(parameters.updateServiceBaseUri());
        this.updateInstaller = ServiceLoader.load(
                UpdateInstaller.class,
                Thread.currentThread().getContextClassLoader()
                ).iterator().next();
        final MessagingParameters mp = parameters.messagingParameters();
        this.connectionFactory = mp.connectionFactory();
        this.namingContext = mp.namingContext();
    }

    @Override protected UpdateClient updateClient() { return updateClient; }

    @Override protected UpdateInstaller updateInstaller() {
        return updateInstaller;
    }

    @Override protected void send(UpdateMessage message) throws Exception {
        JmsMessageSender.create(namingContext, connection()).send(message);
    }

    private Connection connection() throws JMSException {
        if (null == connection) {
            synchronized (this) {
                if (null == connection)
                    connection = connectionFactory.createConnection();
            }
        }
        return connection;
    }

    @Override public synchronized void onUpdateMessage(UpdateMessage message)
    throws Exception {
        super.onUpdateMessage(message);
    }

    @Override
    public synchronized void checkUpdates() throws Exception {
        super.checkUpdates();
    }

    @Override public synchronized void close() throws Exception {
        // HC SVNT DRACONIS!
        super.close();
        if (null != connection) try {
            connection.close();
        } catch (JMSException ex) {
            throw new Exception(ex);
        }
    }
}
