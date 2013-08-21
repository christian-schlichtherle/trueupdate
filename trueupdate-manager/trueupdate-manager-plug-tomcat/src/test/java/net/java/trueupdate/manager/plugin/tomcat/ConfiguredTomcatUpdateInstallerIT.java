/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.plugin.tomcat;

import java.net.URI;
import java.util.Collection;
import java.util.logging.*;
import javax.management.JMException;
import net.java.trueupdate.manager.api.UpdateMessage;
import org.apache.catalina.Context;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.*;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ConfiguredTomcatUpdateInstallerIT {

    private static final String
            NAME = ConfiguredTomcatUpdateInstallerIT.class.getSimpleName();

    private static final String CONTEXT_PATH = '/' + NAME;

    private static final String ARCHIVE_NAME = NAME + ".war";

    private static final Logger
            logger = Logger.getLogger(ConfiguredTomcatUpdateInstallerIT.class.getName());

    public static @Deployment WebArchive createDeployment() {
        final WebArchive archive = ShrinkWrap
                .create(WebArchive.class, ARCHIVE_NAME);
        logger.config(archive.toString(true));
        return archive;
    }

    private final ConfiguredTomcatUpdateInstaller installer =
            new ConfiguredTomcatUpdateInstaller(installationRequest());

    public @Test void testContexts() throws JMException {
        final Collection<Context> contexts = installer.contexts();
        assert !contexts.isEmpty();
        for (final Context context : contexts) {
            assert CONTEXT_PATH.equals(context.getPath());
            logger.log(Level.INFO, "The resolved context is {0} at {1}.",
                    new Object[] { context, context.getDocBase() });
        }
    }

    private static UpdateMessage installationRequest() {
        return UpdateMessage
                .builder()
                    .type(UpdateMessage.Type.INSTALLATION_REQUEST)
                    .from(URI.create("agent"))
                    .to(URI.create("manager"))
                    .artifactDescriptor()
                        .groupId("net.java.trueupdate")
                        .artifactId("truevfs-kernel-spec")
                        .version("0.9")
                        .inject()
                    .updateVersion("0.10.3")
                    .currentLocation(URI.create(CONTEXT_PATH))
                    .build();
    }
}
