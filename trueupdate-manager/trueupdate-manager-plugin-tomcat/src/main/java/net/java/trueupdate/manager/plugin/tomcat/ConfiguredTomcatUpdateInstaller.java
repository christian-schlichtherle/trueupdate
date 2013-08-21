/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.plugin.tomcat;

import java.io.File;
import java.util.*;
import java.util.logging.*;
import javax.annotation.concurrent.Immutable;
import javax.management.*;
import net.java.trueupdate.manager.api.UpdateDescriptor;
import net.java.trueupdate.manager.api.UpdateMessage;
import net.java.trueupdate.manager.core.UpdateResolver;
import org.apache.catalina.*;

/**
 * Installs updates for applications running in Tomcat.
 *
 * @author Christian Schlichtherle
 */
@Immutable
final class ConfiguredTomcatUpdateInstaller {

    private static final Logger logger =
            Logger.getLogger(ConfiguredTomcatUpdateInstaller.class.getName());

    private final UpdateMessage message;

    ConfiguredTomcatUpdateInstaller(final UpdateMessage updateDescriptor) {
        this.message = Objects.requireNonNull(updateDescriptor);
    }

    void install(final UpdateResolver resolver) throws Exception {
        final File patch = resolver.resolve(updateDescriptor());
        logger.log(Level.FINE, "Resolved ZIP patch file {0}.", patch);
        for (final Context context : contexts())
            logger.log(Level.FINE, "Updating context {0}.", context);
    }

    private UpdateDescriptor updateDescriptor() {
        return message.updateDescriptor();
    }

    Collection<Context> contexts() throws Exception {

        class Finder {
            final String name = name();
            final Collection<Context> matchingContexts = new ArrayList<>();

            boolean findContexts(final Container container) {
                boolean foundContexts = false;
                for (final Container child : container.findChildren()) {
                    if (child instanceof Context) {
                        assert !findContexts(child)
                                : "Recursive contexts are not supported.";
                        final Context context = (Context) child;
                        if (name.equals(context.getName()))
                            matchingContexts.add(context);
                        foundContexts = true;
                    } else {
                        foundContexts |= findContexts(child);
                    }
                }
                return foundContexts;
            }
        } // Finder

        final Finder finder = new Finder();
        final boolean foundContexts = finder.findContexts(engine());
        assert foundContexts
                : "I am running in Tomcat, so there should be some contexts - at least one for me.";
        return finder.matchingContexts;
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
