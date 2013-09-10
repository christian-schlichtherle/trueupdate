/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.mini;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.*;
import net.java.trueupdate.installer.core.UpdateManager;
import net.java.trueupdate.jaxrs.client.UpdateClient;
import net.java.trueupdate.manager.spec.*;

/**
 * @author Christian Schlichtherle
 */
final class UpdateManagerPollingDaemon extends UpdateManager implements Runnable {

    private static final Logger
            logger = Logger.getLogger(UpdateManagerPollingDaemon.class.getName());

    private final Connection connection;
    private final Destination destination;
    private final UpdateClient updateClient;
    private final UpdateInstaller updateInstaller;
    private final int checkUpdatesIntervalMinutes;

    UpdateManagerPollingDaemon(
            final Connection connection,
            final Destination destination,
            final UpdateClient updateClient,
            final UpdateInstaller updateInstaller,
            final int checkUpdatesIntervalMinutes) {
        assert null != connection;
        assert null != destination;
        assert null != updateClient;
        assert null != updateInstaller;
        this.connection = connection;
        this.destination = destination;
        this.updateClient = updateClient;
        this.updateInstaller = updateInstaller;
        this.checkUpdatesIntervalMinutes = requirePositive(checkUpdatesIntervalMinutes);
    }

    private static int requirePositive(int n) {
        if (0 >= n) throw new IllegalArgumentException();
        return n;
    }

    @Override public void close() throws Exception {
        try { super.close(); }
        finally { connection.close(); }
    }

    @Override protected UpdateClient updateClient() { return updateClient; }

    @Override protected UpdateInstaller updateInstaller() {
        return updateInstaller;
    }

    @Override protected UpdateMessage send(UpdateMessage message) throws Exception {
        final Session s = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        try {
            final Message m = s.createObjectMessage(message);
            m.setBooleanProperty("manager", message.type().forManager());
            s.createProducer(destination).send(m);
        } finally {
            s.close();
        }
        return message;
    }

    @Override public void run() {
        final long intervalMillis = checkUpdatesIntervalMinutes * 60L * 1000;
        long startedMillis = System.currentTimeMillis();
        while (true) {
            try {
                final long durationMillis = System.currentTimeMillis() - startedMillis;
                final long sleepMillis = intervalMillis - durationMillis;
                if (0 < sleepMillis) Thread.sleep(sleepMillis);
                startedMillis = System.currentTimeMillis();
                checkUpdates();
            } catch (InterruptedException ex) {
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Checking for updates failed.", ex);
            }
        }
    }
}
