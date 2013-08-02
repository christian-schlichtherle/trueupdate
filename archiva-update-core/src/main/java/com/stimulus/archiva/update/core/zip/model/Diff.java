/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.core.zip.model;

import java.util.*;
import javax.annotation.CheckForNull;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Models an optional unchanged, changed, added and removed map of entry names
 * and message digests in canonical string notation, attributed with the
 * message digest algorithm name and byte length.
 * This model class represents the meta data in a ZIP patch file.
 * Mind you that this class is mutable.
 *
 * @author Christian Schlichtherle
 */
@XmlRootElement
public final class Diff {

    @XmlAttribute
    public String algorithm;

    @XmlAttribute
    public Integer numBytes;

    @XmlJavaTypeAdapter(EntryNameAndDigestMapAdapter.class)
    public SortedMap<String, EntryNameAndDigest> unchanged;

    @XmlJavaTypeAdapter(EntryNameAndTwoDigestsMapAdapter.class)
    public SortedMap<String, EntryNameAndTwoDigests> changed;

    @XmlJavaTypeAdapter(EntryNameAndDigestMapAdapter.class)
    public SortedMap<String, EntryNameAndDigest> added, removed;

    @Deprecated
    public @CheckForNull
    EntryNameAndDigest unchanged(String name) {
        return null == unchanged ? null : unchanged.get(name);
    }

    public @CheckForNull
    EntryNameAndTwoDigests changed(String name) {
        return null == changed ? null : changed.get(name);
    }

    public @CheckForNull
    EntryNameAndDigest added(String name) {
        return null == added ? null : added.get(name);
    }

    @Deprecated
    public @CheckForNull
    EntryNameAndDigest removed(String name) {
        return null == removed ? null : removed.get(name);
    }

    @Override public boolean equals(final Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Diff)) return false;
        final Diff that = (Diff) obj;
        return  Objects.equals(this.unchanged, that.unchanged) &&
                Objects.equals(this.changed, that.changed) &&
                Objects.equals(this.added, that.added) &&
                Objects.equals(this.removed, that.removed);
    }

    @Override public int hashCode() {
        int hashCode = 17;
        hashCode = 31 * hashCode + Objects.hashCode(unchanged);
        hashCode = 31 * hashCode + Objects.hashCode(changed);
        hashCode = 31 * hashCode + Objects.hashCode(added);
        hashCode = 31 * hashCode + Objects.hashCode(removed);
        return hashCode;
    }
}
