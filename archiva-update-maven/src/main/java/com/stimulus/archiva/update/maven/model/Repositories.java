/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.maven.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Models a local repository and an optional list of remote repositories.
 * Mind you that this class is mutable.
 *
 * @author Christian Schlichtherle
 */
@XmlRootElement
public class Repositories {

    @XmlElement(required = true)
    public @Nullable Local local;

    public final List<Remote> remote = new ArrayList<>();

    @Override public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Repositories)) return false;
        final Repositories that = (Repositories) obj;
        return  Objects.equals(this.local, that.local) &&
                Objects.equals(this.remote, that.remote);
    }

    @Override public int hashCode() {
        int hash = 17;
        hash = 31 * hash + Objects.hashCode(local);
        hash = 31 * hash + Objects.hashCode(remote);
        return hash;
    }
}
