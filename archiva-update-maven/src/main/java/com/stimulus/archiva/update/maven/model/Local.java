/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.maven.model;

import java.util.Objects;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;

/**
 * Models a local repository.
 * Mind you that this class is mutable.
 *
 * @author Christian Schlichtherle
 */
public final class Local {

    @XmlElement(required = true)
    public @Nullable String basedir;

    public @Nullable String type;

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
