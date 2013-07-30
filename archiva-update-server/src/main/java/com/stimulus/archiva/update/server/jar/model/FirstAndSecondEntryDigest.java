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
 * Represents the digests of two equal named JAR entries in different JAR files.
 *
 * @author Christian Schlichtherle
 */
public final class FirstAndSecondEntryDigest {

    /** Constructor for use with JAXB. */
    public FirstAndSecondEntryDigest() { }

    /** Default constructor. */
    public FirstAndSecondEntryDigest(
            final @CheckForNull String name,
            final @CheckForNull String first,
            final @CheckForNull String second) {
        this.name = name;
        assert !Objects.equals(first, second);
        this.first = first;
        this.second = second;
    }

    @XmlAttribute
    public @Nullable
    String name, first, second;

    @Override public boolean equals(final Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof FirstAndSecondEntryDigest)) return false;
        final FirstAndSecondEntryDigest that = (FirstAndSecondEntryDigest) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.first, that.first) &&
                Objects.equals(this.second, that.second);
    }

    @Override public int hashCode() {
        int hashCode = 17;
        hashCode = 31 * hashCode + Objects.hashCode(name);
        hashCode = 31 * hashCode + Objects.hashCode(first);
        hashCode = 31 * hashCode + Objects.hashCode(second);
        return hashCode;
    }
}
