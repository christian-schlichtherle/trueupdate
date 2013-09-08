/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.tomcat;

import org.apache.catalina.startup.HostConfig;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.*;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Christian Schlichtherle
 */
@RunWith(Arquillian.class)
public class TomcatIT {

    @Deployment
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
            .addClass(Tomcat.class);
    }

    @Test
    public void testResolveHostConfig() throws Exception {
        final HostConfig config = Tomcat.resolveHostConfig();
        assert null != config;
    }
}
