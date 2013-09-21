/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.jms;

import java.net.URI;
import java.net.URL;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.JAXB;
import net.java.trueupdate.jms.*;
import net.java.trueupdate.manager.jms.JmsUpdateManagerParameters;
import net.java.trueupdate.manager.jms.dto.JmsUpdateManagerParametersDto;
import net.java.trueupdate.manager.spec.UpdateInstaller;
import net.java.trueupdate.manager.core.UpdateManagerException;
import static net.java.trueupdate.util.Resources.locate;

/**
 * Provides the objects required for this package.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class JmsUpdateManagerContext {

    private static final String CONFIGURATION = "META-INF/update/manager.xml";

    private final JmsUpdateManagerParameters parameters;
    private final JmsUpdateManager manager;
    private final JmsUpdateTimer timer;
    private final JmsMessageReceiver receiver;

    public static JmsUpdateManagerContext load() { return load(CONFIGURATION); }

    static JmsUpdateManagerContext load(final String resourceName) {
        try {
            return new JmsUpdateManagerContext(parameters(locate(resourceName)));
        } catch (Exception ex) {
            throw new java.lang.IllegalStateException(String.format(
                    "Failed to load configuration from %s .", resourceName),
                    ex);
        }
    }

    private static JmsUpdateManagerParameters parameters(final URL source)
    throws Exception {
        return JmsUpdateManagerParameters.parse(
                JAXB.unmarshal(source, JmsUpdateManagerParametersDto.class));
    }

    public JmsUpdateManagerContext(final JmsUpdateManagerParameters ump) {
        // HC SVNT DRACONIS
        this.parameters = ump;
        final MessagingParameters mp = ump.messagingParameters();
        manager = new JmsUpdateManager(ump);
        timer = new JmsUpdateTimer(manager, ump.checkUpdatesIntervalMinutes());
        receiver = JmsMessageReceiver
                .builder()
                .connectionFactory(mp.connectionFactory())
                .destination(mp.fromDestination())
                .subscriptionName(mp.fromName())
                .messageSelector("manager = true")
                .messageListener(manager)
                .build();
    }

    public URI updateServiceBaseUri() {
        return parameters.updateServiceBaseUri();
    }

    public int checkUpdatesIntervalMinutes() {
        return parameters.checkUpdatesIntervalMinutes();
    }

    public UpdateInstaller updateInstaller() {
        return manager.updateInstaller();
    }

    public void start() throws UpdateManagerException {
        timer().start();
        receiver().start();
    }

    private Thread timer() {
        return new Thread(timer, "TrueUpdate Manager Mini / Timer Daemon") {
            { super.setDaemon(true); }
        };
    }

    private Thread receiver() {
        return new Thread(receiver, "TrueUpdate Manager Mini / Receiver Daemon") {
            { super.setDaemon(true); }
        };
    }

    public void stop() throws UpdateManagerException {
        try {
            // HC SUNT DRACONIS!
            receiver.stop();
            timer.stop();
            manager.close();
        } catch (Exception ex) {
            throw new UpdateManagerException(ex);
        }
    }
}
