/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.plugin.tomcat;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.*;
import javax.annotation.concurrent.Immutable;
import javax.management.*;
import net.java.trueupdate.artifact.api.ArtifactDescriptor;
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
        final Collection<Context> contexts = contexts();
        if (contexts.isEmpty())
            throw new Exception(String.format(
                    "There are no contexts with the name %s.", name()));
        for (Context context : contexts()) apply(patch, context);
    }

    void apply(final File patch, final Context context) throws Exception {
        final String docBaseString = context.getDocBase();
        try {
            final URI docBaseUri = new URI(docBaseString);
            if (docBaseUri.isAbsolute()) {
                try {
                    apply(patch, new File(docBaseUri));
                } catch (IllegalArgumentException notAFileBasedUri) {
                    logger.log(Level.WARNING,
                            "Not updating {0} to version {1} at {2} because the document base URI is not file based.",
                            new Object[] { artifactDescriptor(),
                                           updateVersion(), docBaseUri });
                }
                return;
            }
        } catch (URISyntaxException ex) {
        }
        apply(patch, new File(docBaseString));
    }

    void apply(final File patch, final File location) throws Exception {
        logger.log(Level.INFO, "Updating {0} with {1} to version {2} using {3}.",
                new Object[] { location, artifactDescriptor(), updateVersion(),
                               patch });
        // TODO...
    }

    private ArtifactDescriptor artifactDescriptor() {
        return updateDescriptor().artifactDescriptor();
    }

    private String updateVersion() {
        return updateDescriptor().updateVersion();
    }

    private UpdateDescriptor updateDescriptor() {
        return message.updateDescriptor();
    }

    Collection<Context> contexts() throws JMException {

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

    private Engine engine() throws JMException {
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
