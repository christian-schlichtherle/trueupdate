/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.core.zip.model;

import java.util.Objects;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * Models a ZIP entry name and message digest in canonical string notation.
 * Mind you that this class is mutable.
 *
 * @author Christian Schlichtherle
 */
public final class EntryNameAndDigest {

    /** Used by JAXB. */
    public EntryNameAndDigest() { }

    /** Default constructor. */
    public EntryNameAndDigest(
            final @CheckForNull String name,
            final @CheckForNull String digest) {
        this.name = name;
        this.digest = digest;
    }

    @XmlAttribute
    public @Nullable String name, digest;

    @Override public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof EntryNameAndDigest)) return false;
        final EntryNameAndDigest that = (EntryNameAndDigest) obj;
        return  Objects.equals(this.name, that.name) &&
                Objects.equals(this.digest, that.digest);
    }

    @Override public int hashCode() {
        int hash = 17;
        hash = 31 * hash + Objects.hashCode(name);
        hash = 31 * hash + Objects.hashCode(digest);
        return hash;
    }
}
