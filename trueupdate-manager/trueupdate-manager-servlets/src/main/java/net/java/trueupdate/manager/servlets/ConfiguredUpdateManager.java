/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.servlets;

import javax.annotation.WillCloseWhenClosed;
import javax.annotation.concurrent.ThreadSafe;
import javax.jms.*;
import javax.naming.Context;
import net.java.trueupdate.jaxrs.client.UpdateClient;
import net.java.trueupdate.jms.JmsMessageSender;
import net.java.trueupdate.manager.core.UpdateManager;
import net.java.trueupdate.manager.spec.*;

/**
 * An implementation of the abstract update manager class with minimal
 * dependencies.
 * The public methods of this class are thread safe, all others are not.
 *
 * @author Christian Schlichtherle
 */
@ThreadSafe
final class ConfiguredUpdateManager extends UpdateManager {

    private final @WillCloseWhenClosed Connection connection;
    private final Context namingContext;
    private final UpdateClient updateClient;
    private final UpdateInstaller updateInstaller;

    ConfiguredUpdateManager(
            final ConnectionFactory factory,
            final Context namingContext,
            final UpdateClient updateClient,
            final UpdateInstaller updateInstaller)
    throws JMSException {
        assert null != namingContext;
        assert null != updateClient;
        assert null != updateInstaller;
        this.connection = factory.createConnection();
        this.namingContext = namingContext;
        this.updateClient = updateClient;
        this.updateInstaller = updateInstaller;
    }

    @Override protected UpdateClient updateClient() { return updateClient; }

    @Override protected UpdateInstaller updateInstaller() {
        return updateInstaller;
    }

    @Override
    protected void send(UpdateMessage message) throws Exception {
        JmsMessageSender.create(namingContext, connection).send(message);
    }

    @Override public synchronized void onUpdateMessage(UpdateMessage message)
    throws Exception {
        super.onUpdateMessage(message);
    }

    @Override public synchronized void checkUpdates() throws Exception {
        super.checkUpdates();
    }

    @Override public synchronized void close() throws Exception {
        // HC SUNT DRACONIS!
        super.close();
        connection.close();
    }
}
