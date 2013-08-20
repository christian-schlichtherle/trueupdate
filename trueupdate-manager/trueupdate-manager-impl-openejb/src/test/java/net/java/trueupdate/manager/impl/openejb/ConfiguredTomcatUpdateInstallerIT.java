/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.impl.openejb;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.java.trueupdate.manager.spec.UpdateMessage;
import org.apache.catalina.Context;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ConfiguredTomcatUpdateInstallerIT {

    private static final String NAME = ConfiguredTomcatUpdateInstallerIT.class.getSimpleName();

    private static final String CONTEXT_PATH = '/' + NAME;

    private static final String ARCHIVE_NAME = NAME + ".war";

    private static final Logger
            logger = Logger.getLogger(ConfiguredTomcatUpdateInstallerIT.class.getName());

    @Deployment
    public static WebArchive createDeployment() {
        final WebArchive archive = ShrinkWrap
                .create(WebArchive.class, ARCHIVE_NAME);
        logger.config(archive.toString(true));
        return archive;
    }

    @Test
    public void testContext() throws Exception {
        final Context context = configuredTomEeInstaller().context();
        assert CONTEXT_PATH.equals(context.getPath());
        logger.log(Level.INFO, "The resolved context is {0}.", context);
    }

    private static ConfiguredTomcatUpdateInstaller configuredTomEeInstaller() {
        return new ConfiguredTomcatUpdateInstaller(installationRequest());
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
