/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.maven.model;

import java.util.Objects;
import javax.xml.bind.annotation.XmlElement;

/**
 * Models a remote repository.
 * Mind you that this class is mutable and may have null fields.
 *
 * @author Christian Schlichtherle
 */
public final class Remote {

    public String id, type;

    @XmlElement(required = true)
    public String url;

    /** Required by JAXB. */
    public Remote() { }

    /** Courtesy constructor. */
    public Remote(final String id, final String type, final String url) {
        this.id = id;
        this.type = type;
        this.url = url;
    }

    @Override public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Remote)) return false;
        final Remote that = (Remote) obj;
        return  Objects.equals(this.id, that.id) &&
                Objects.equals(this.type, that.type) &&
                Objects.equals(this.url, that.url);
    }

    @Override public int hashCode() {
        int hash = 17;
        hash = 31 * hash + Objects.hashCode(id);
        hash = 31 * hash + Objects.hashCode(type);
        hash = 31 * hash + Objects.hashCode(url);
        return hash;
    }
}
