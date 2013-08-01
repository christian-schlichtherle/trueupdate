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
 * Represents a ZIP entry name with two message digests.
 * Mind you that this class is mutable.
 *
 * @author Christian Schlichtherle
 */
public final class EntryNameWithTwoDigests {

    /** Used by JAXB. */
    public EntryNameWithTwoDigests() { }

    /**
     * Default constructor.
     * The first and second digest should not be equal.
     */
    public EntryNameWithTwoDigests(
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

    /** Returns the entry name with the first digest. */
    @Deprecated
    public EntryNameWithDigest entryNameWithFirstDigest() {
        return new EntryNameWithDigest(name, first);
    }

    /** Returns the entry name with the second digest. */
    public EntryNameWithDigest entryNameWithSecondDigest() {
        return new EntryNameWithDigest(name, second);
    }

    @Override public boolean equals(final Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof EntryNameWithTwoDigests)) return false;
        final EntryNameWithTwoDigests that = (EntryNameWithTwoDigests) obj;
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
