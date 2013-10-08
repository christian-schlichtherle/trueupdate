/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.jms;

import java.net.URI;
import javax.annotation.WillCloseWhenClosed;
import javax.annotation.concurrent.ThreadSafe;
import javax.jms.*;
import javax.naming.Context;
import net.java.trueupdate.jms.*;
import net.java.trueupdate.manager.core.*;
import net.java.trueupdate.message.UpdateMessage;

/**
 * An implementation of the abstract update manager class with minimal
 * dependencies.
 * The public methods of this class are thread safe, all others are not.
 *
 * @author Christian Schlichtherle
 */
@ThreadSafe
final class JmsUpdateManager extends CoreUpdateManager {

    private final Object lock = new Object();

    private final URI updateServiceBaseUri;
    private final Context namingContext;
    private final ConnectionFactory connectionFactory;

    private volatile @WillCloseWhenClosed Connection connection;

    JmsUpdateManager(final JmsUpdateManagerParameters parameters) {
        final UpdateServiceParameters usp = parameters.updateService();
        updateServiceBaseUri = usp.uri();
        final JmsParameters mp = parameters.messaging();
        namingContext = mp.namingContext();
        connectionFactory = mp.connectionFactory();
    }

    @Override
    protected URI updateServiceUri() { return updateServiceBaseUri; }

    @Override protected void send(UpdateMessage message) throws Exception {
        JmsSender.create(namingContext, connection()).send(message);
    }

    private Connection connection() throws JMSException {
        if (null == connection) {
            synchronized (lock) {
                if (null == connection)
                    connection = connectionFactory.createConnection();
            }
        }
        return connection;
    }

    @Override public void close() throws Exception {
        // HC SVNT DRACONIS!
        try {
            super.close();
        } finally {
            synchronized (lock) {
                if (null != connection) connection.close();
            }
        }
    }
}
