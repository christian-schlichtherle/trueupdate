/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.maven.model;

import java.util.Objects;
import javax.xml.bind.annotation.XmlElement;

/**
 * Models a local repository.
 * Mind you that this class is mutable and may have null fields.
 *
 * @author Christian Schlichtherle
 */
public final class Local {

    @XmlElement(required = true)
    public String basedir;

    public String type;

    /** Required by JAXB. */
    public Local() { }

    /** Courtesy constructor. */
    public Local(final String basedir, final String type) {
        this.basedir = basedir;
        this.type = type;
    }

    @Override public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Local)) return false;
        final Local that = (Local) obj;
        return  Objects.equals(this.basedir, that.basedir) &&
                Objects.equals(this.type, that.type);
    }

    @Override public int hashCode() {
        int hash = 17;
        hash = 31 * hash + Objects.hashCode(basedir);
        hash = 31 * hash + Objects.hashCode(type);
        return hash;
    }
}
