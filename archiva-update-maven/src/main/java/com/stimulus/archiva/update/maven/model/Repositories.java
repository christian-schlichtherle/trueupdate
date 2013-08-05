/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.maven.model;

import com.stimulus.archiva.update.core.codec.JaxbCodec;
import com.stimulus.archiva.update.core.io.Source;
import java.util.*;
import javax.xml.bind.*;
import javax.xml.bind.annotation.*;

/**
 * Models a local repository and some remote repositories.
 * Mind you that this class is mutable and may have null fields.
 *
 * @author Christian Schlichtherle
 */
@XmlRootElement
public final class Repositories {

    @XmlElement(required = true)
    public Local local;

    @XmlElement(name = "remote")
    public List<Remote> remotes;

    /** Required by JAXB. */
    public Repositories() {
        this.remotes = new LinkedList<>();
    }

    /** Courtesy constructor. */
    public Repositories(final Local local, final List<Remote> remotes) {
        this.local = local;
        this.remotes = remotes;
    }

    @Override public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Repositories)) return false;
        final Repositories that = (Repositories) obj;
        return  Objects.equals(this.local, that.local) &&
                Objects.equals(this.remotes, that.remotes);
    }

    @Override public int hashCode() {
        int hash = 17;
        hash = 31 * hash + Objects.hashCode(local);
        hash = 31 * hash + Objects.hashCode(remotes);
        return hash;
    }

    /**
     * Decodes repositories from XML.
     *
     * @param source the source for reading the XML.
     * @return the decoded repositories.
     * @throws Exception at the discretion of the JAXB codec, e.g. if the
     *         source isn't readable.
     */
    public static Repositories decode(Source source) throws Exception {
        return new JaxbCodec(jaxbContext()).decode(source, Repositories.class);
    }

    /** Returns a JAXB context which binds only this class. */
    public static JAXBContext jaxbContext() { return Lazy.JAXB_CONTEXT; }

    private static class Lazy {

        static final JAXBContext JAXB_CONTEXT;

        static {
            try { JAXB_CONTEXT = JAXBContext.newInstance(Repositories.class); }
            catch (JAXBException ex) { throw new AssertionError(ex); }
        }
    }
}
