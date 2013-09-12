/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.mini;

import javax.annotation.concurrent.ThreadSafe;
import javax.jms.*;
import net.java.trueupdate.installer.core.UpdateManager;
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

    private final Connection connection;
    private final Destination destination;
    private final UpdateClient updateClient;
    private final UpdateInstaller updateInstaller;

    MiniUpdateManager(
            final Connection connection,
            final Destination destination,
            final UpdateClient updateClient,
            final UpdateInstaller updateInstaller) {
        assert null != connection;
        assert null != destination;
        assert null != updateClient;
        assert null != updateInstaller;
        this.connection = connection;
        this.destination = destination;
        this.updateClient = updateClient;
        this.updateInstaller = updateInstaller;
    }

    @Override protected UpdateClient updateClient() { return updateClient; }

    @Override protected UpdateInstaller updateInstaller() {
        return updateInstaller;
    }

    @Override
    protected UpdateMessage send(UpdateMessage message) throws Exception {
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

    @Override public synchronized void checkUpdates() throws Exception {
        super.checkUpdates();
    }

    @Override public synchronized void close() throws Exception {
        try { super.close(); }
        finally { connection.close(); }
    }

    @Override public synchronized void onUpdateMessage(UpdateMessage message)
    throws Exception {
        super.onUpdateMessage(message);
    }
}
