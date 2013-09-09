/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.tomcat;

import javax.management.*;
import org.apache.catalina.*;
import org.apache.catalina.startup.HostConfig;

/**
 * Provides functions for Tomcat.
 *
 * @author Christian Schlichtherle
 */
final class Tomcat {

    static HostConfig resolveHostConfig() throws Exception {
        final ObjectName pattern = new ObjectName("*:type=Engine");
        for (final MBeanServer mbs : MBeanServerFactory.findMBeanServer(null)) {
            for (final ObjectName on : mbs.queryNames(pattern, null)) {
                try {
                    final Engine engine = (Engine) mbs.getAttribute(on, "managedResource");
                    final Host host = (Host) engine.findChild(engine.getDefaultHost());
                    for (final LifecycleListener listener : host.findLifecycleListeners())
                        if (listener instanceof HostConfig)
                            return (HostConfig) listener;
                } catch (InstanceNotFoundException ex) {
                } catch (AttributeNotFoundException ex) {
                }
            }
        }
        throw new InstanceNotFoundException(pattern.toString());
    }

    private Tomcat() { }
}
