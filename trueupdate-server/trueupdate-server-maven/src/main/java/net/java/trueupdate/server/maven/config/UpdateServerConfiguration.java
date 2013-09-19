/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.server.maven.config;

import javax.xml.bind.*;
import javax.xml.bind.annotation.XmlRootElement;
import net.java.trueupdate.artifact.maven.MavenArtifactResolver;
import net.java.trueupdate.core.codec.JaxbCodec;
import net.java.trueupdate.core.io.Source;

/**
 * Configures an update server.
 *
 * @author Christian Schlichtherle
 */
@XmlRootElement(name = "server")
@SuppressWarnings("PublicField")
public class UpdateServerConfiguration {

    public MavenArtifactResolver repositories;

    /**
     * Decodes an update server configuration from XML.
     *
     * @param source the source for reading the XML.
     * @return the decoded update server configuration.
     * @throws Exception at the discretion of the JAXB codec, e.g. if the
     *         source isn't readable.
     */
    public static UpdateServerConfiguration decodeFromXml(Source source)
    throws Exception {
        return new JaxbCodec(jaxbContext())
                .decode(source, UpdateServerConfiguration.class);
    }

    /** Returns a JAXB context which binds this class. */
    public static JAXBContext jaxbContext() { return Lazy.JAXB_CONTEXT; }

    private static class Lazy {

        static final JAXBContext JAXB_CONTEXT;

        static {
            try {
                JAXB_CONTEXT = JAXBContext
                        .newInstance(UpdateServerConfiguration.class);
            } catch (JAXBException ex) {
                throw new AssertionError(ex);
            }
        }
    } // Lazy
}
