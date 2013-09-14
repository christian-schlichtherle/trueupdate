/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.mini;

import javax.annotation.WillCloseWhenClosed;
import javax.annotation.concurrent.ThreadSafe;
import javax.jms.*;
import javax.naming.Context;
import net.java.trueupdate.manager.core.UpdateManager;
import net.java.trueupdate.jaxrs.client.UpdateClient;
import net.java.trueupdate.manager.spec.*;

/**
 * An implementation of the abstract update manager class with minimal
 * dependencies.
 * The public methods of this class are thread safe, all others are not.
 *
 * @author Christian Schlichtherle
 */
@ThreadSafe
final class MiniUpdateManager extends UpdateManager {

    private final @WillCloseWhenClosed Connection connection;
    private final Context context;
    private final UpdateClient updateClient;
    private final UpdateInstaller updateInstaller;

    MiniUpdateManager(
            final ConnectionFactory factory,
            final Context context,
            final UpdateClient updateClient,
            final UpdateInstaller updateInstaller)
    throws JMSException {
        assert null != context;
        assert null != updateClient;
        assert null != updateInstaller;
        this.connection = factory.createConnection();
        this.context = context;
        this.updateClient = updateClient;
        this.updateInstaller = updateInstaller;
    }

    @Override protected UpdateClient updateClient() { return updateClient; }

    @Override protected UpdateInstaller updateInstaller() {
        return updateInstaller;
    }

    @Override
    protected UpdateMessage send(final UpdateMessage message) throws Exception {
        final Destination destination = (Destination)
                context.lookup(message.to().toString());
        final Session s = connection.createSession(false,
                                                   Session.AUTO_ACKNOWLEDGE);
        try {
            final Message m = s.createObjectMessage(message);
            m.setBooleanProperty("manager", message.type().forManager());
            s.createProducer(destination).send(m);
        } finally {
            s.close();
        }
        return message;
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
