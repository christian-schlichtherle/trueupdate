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
 * Models a ZIP entry name and two message digests in canonical string notation.
 * Mind you that this class is mutable.
 *
 * @author Christian Schlichtherle
 */
public final class EntryNameAndTwoDigests {

    /** Used by JAXB. */
    private EntryNameAndTwoDigests() { }

    /**
     * Default constructor.
     * The first and second digest should not be equal.
     */
    public EntryNameAndTwoDigests(
            final @CheckForNull String name,
            final @CheckForNull String first,
            final @CheckForNull String second) {
        this.name = name;
        assert !Objects.equals(first, second);
        this.first = first;
        this.second = second;
    }

    @XmlAttribute(required = true)
    public @Nullable String name, first, second;

    /** Returns the entry name with the first digest. */
    @Deprecated
    public EntryNameAndDigest entryNameWithFirstDigest() {
        return new EntryNameAndDigest(name, first);
    }

    /** Returns the entry name with the second digest. */
    public EntryNameAndDigest entryNameWithSecondDigest() {
        return new EntryNameAndDigest(name, second);
    }

    @Override public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof EntryNameAndTwoDigests)) return false;
        final EntryNameAndTwoDigests that = (EntryNameAndTwoDigests) obj;
        return  Objects.equals(this.name, that.name) &&
                Objects.equals(this.first, that.first) &&
                Objects.equals(this.second, that.second);
    }

    @Override public int hashCode() {
        int hash = 17;
        hash = 31 * hash + Objects.hashCode(name);
        hash = 31 * hash + Objects.hashCode(first);
        hash = 31 * hash + Objects.hashCode(second);
        return hash;
    }
}
