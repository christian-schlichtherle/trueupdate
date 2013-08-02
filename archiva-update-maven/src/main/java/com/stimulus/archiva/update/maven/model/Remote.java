/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.maven.model;

import java.util.Objects;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;

/**
 * Models a remote repository.
 * Mind you that this class is mutable.
 *
 * @author Christian Schlichtherle
 */
public class Remote {

    public @Nullable String id, type;

    @XmlElement(required = true)
    public @Nullable String url;

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
