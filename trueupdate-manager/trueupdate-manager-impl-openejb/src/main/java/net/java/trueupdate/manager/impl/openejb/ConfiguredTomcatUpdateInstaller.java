/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.impl.openejb;

import java.io.File;
import java.util.*;
import java.util.logging.*;
import javax.management.*;
import net.java.trueupdate.manager.spec.*;
import org.apache.catalina.*;

/**
 * Installs updates for applications running in Tomcat.
 *
 * @author Christian Schlichtherle
 */
final class ConfiguredTomcatUpdateInstaller {

    private static final Logger logger =
            Logger.getLogger(ConfiguredTomcatUpdateInstaller.class.getName());

    private final UpdateMessage message;

    ConfiguredTomcatUpdateInstaller(final UpdateMessage updateDescriptor) {
        this.message = Objects.requireNonNull(updateDescriptor);
    }

    void install(final UpdateResolver resolver) throws Exception {
        final Context context = context();
        logger.log(Level.FINE, "The resolved context is {0}.", context);
        final File patch = resolver.resolve(updateDescriptor());
        logger.log(Level.FINE, "The ZIP patch file is {0}.", patch);
    }

    private UpdateDescriptor updateDescriptor() {
        return message.updateDescriptor();
    }

    Context context() throws Exception {
        try {
            final Engine engine = engine();
            final Host host = (Host) engine.findChild(engine.getDefaultHost());
            final Context context = (Context) host.findChild(name());
            if (null == context) throw new NullPointerException();
            return context;
        } catch (RuntimeException ex) {
            throw (Exception) new InstanceNotFoundException(ex.toString())
                    .initCause(ex);
        }
    }

    Engine engine() throws Exception {
        final ObjectName pattern = new ObjectName("*", "type", "Engine");
        final String[] attributes = new String[] { "managedResource" };
        for (final MBeanServer mbs : MBeanServerFactory.findMBeanServer(null)) {
            for (final ObjectName name : mbs.queryNames(pattern, null)) {
                for (final Object attr : mbs.getAttributes(name, attributes)) {
                    final Object value = ((Attribute) attr).getValue();
                    if (value instanceof Engine) return (Engine) value;
                }
            }
        }
        throw new InstanceNotFoundException("Cannot find Tomcat engine.");
    }

    private String name() {
        return message.currentLocation().toString();
    }
}
