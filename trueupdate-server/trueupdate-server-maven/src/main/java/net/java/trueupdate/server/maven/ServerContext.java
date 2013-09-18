/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.server.maven;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.*;
import javax.xml.bind.annotation.*;
import net.java.trueupdate.artifact.maven.MavenArtifactResolver;
import net.java.trueupdate.core.codec.JaxbCodec;
import net.java.trueupdate.core.io.Source;
import net.java.trueupdate.util.Objects;

/**
 * A server context.
 *
 * @author Christian Schlichtherle
 */
@Immutable
@XmlRootElement(name = "server")
@XmlAccessorType(XmlAccessType.FIELD)
final class ServerContext {

    @XmlElement(name = "repositories")
    private final @Nullable MavenArtifactResolver mavenArtifactResolver;

    /** Required for JAXB. */
    private ServerContext() { mavenArtifactResolver = null; }

    ServerContext(final MavenArtifactResolver mavenArtifactResolver) {
        this.mavenArtifactResolver = Objects.requireNonNull(mavenArtifactResolver);
    }

    MavenArtifactResolver mavenArtifactResolver() {
        return mavenArtifactResolver;
    }

    /**
     * Decodes a server context from XML.
     *
     * @param source the source for reading the XML.
     * @return the decoded server context.
     * @throws Exception at the discretion of the JAXB codec, e.g. if the
     *         source isn't readable.
     */
    static ServerContext decodeFromXml(Source source) throws Exception {
        return new JaxbCodec(jaxbContext()).decode(source, ServerContext.class);
    }

    /** Returns a JAXB context which binds this class. */
    static JAXBContext jaxbContext() { return Lazy.JAXB_CONTEXT; }

    private static class Lazy {

        static final JAXBContext JAXB_CONTEXT;

        static {
            try {
                JAXB_CONTEXT = JAXBContext
                        .newInstance(ServerContext.class);
            } catch (JAXBException ex) {
                throw new AssertionError(ex);
            }
        }
    } // Lazy
} // ServerContext
