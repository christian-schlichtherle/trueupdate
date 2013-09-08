/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.tomcat;

import javax.annotation.CheckForNull;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import org.apache.catalina.Container;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.apache.catalina.startup.HostConfig;

/**
 * Provides functions for Tomcat.
 *
 * @author Christian Schlichtherle
 */
final class Tomcat {

    static HostConfig resolveHostConfig() throws Exception {
        final ObjectName pattern = new ObjectName("*:type=Server");
        for (final MBeanServer mbs : MBeanServerFactory.findMBeanServer(null)) {
            for (final ObjectName on : mbs.queryNames(pattern, null)) {
                try {
                    final Server server = (Server) mbs.getAttribute(on, "managedResource");
                    for (final Service service : server.findServices()) {
                        final HostConfig config = findHostConfig(service.getContainer());
                        if (null != config) return config;
                    }
                } catch (InstanceNotFoundException ex) {
                } catch (AttributeNotFoundException ex) {
                }
            }
        }
        throw new InstanceNotFoundException(pattern.toString());
    }

    private static @CheckForNull HostConfig findHostConfig(Container container) {
        if (container instanceof Host) {
            final Host host = (Host) container;
            for (final LifecycleListener listener : host.findLifecycleListeners())
                if (listener instanceof HostConfig)
                    return (HostConfig) listener;
        } else {
            for (Container child : container.findChildren()) {
                final HostConfig config = findHostConfig(child);
                if (null != config) return config;
            }
        }
        return null;
    }

    private Tomcat() { }
}
