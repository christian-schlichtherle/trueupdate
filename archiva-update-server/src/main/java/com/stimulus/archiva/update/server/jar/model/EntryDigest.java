/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jar.model;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAttribute;
import java.util.Objects;

/**
 * Represents the digest of a JAR entry.
 *
 * @author Christian Schlichtherle
 */
public final class EntryDigest {

    /** Used by JAXB. */
    public EntryDigest() { }

    /** Default constructor. */
    public EntryDigest(
            final @CheckForNull String name,
            final @CheckForNull String digest) {
        this.name = name;
        this.digest = digest;
    }

    @XmlAttribute
    public @Nullable String name, digest;

    @Override public boolean equals(final Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof EntryDigest)) return false;
        final EntryDigest that = (EntryDigest) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.digest, that.digest);
    }

    @Override public int hashCode() {
        int hashCode = 17;
        hashCode = 31 * hashCode + Objects.hashCode(name);
        hashCode = 31 * hashCode + Objects.hashCode(digest);
        return hashCode;
    }
}
