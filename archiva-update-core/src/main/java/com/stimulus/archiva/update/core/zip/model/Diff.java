/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.core.zip.model;

import java.util.*;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Models an optional unchanged, changed, added and removed map of entry names
 * and message digests in canonical string notation, attributed with the
 * message digest algorithm name and byte length.
 * This model class represents the meta data in a ZIP patch file.
 * Mind you that this class is mutable and may have null fields.
 *
 * @author Christian Schlichtherle
 */
@XmlRootElement
public final class Diff {

    @XmlAttribute(required = true)
    public String algorithm;

    @XmlAttribute
    public Integer numBytes;

    @XmlJavaTypeAdapter(EntryNameAndDigestMapAdapter.class)
    public Map<String, EntryNameAndDigest> unchanged;

    @XmlJavaTypeAdapter(EntryNameAndTwoDigestsMapAdapter.class)
    public Map<String, EntryNameAndTwoDigests> changed;

    @XmlJavaTypeAdapter(EntryNameAndDigestMapAdapter.class)
    public Map<String, EntryNameAndDigest> added, removed;

    @Deprecated
    public EntryNameAndDigest unchanged(String name) {
        return null == unchanged ? null : unchanged.get(name);
    }

    public EntryNameAndTwoDigests changed(String name) {
        return null == changed ? null : changed.get(name);
    }

    public EntryNameAndDigest added(String name) {
        return null == added ? null : added.get(name);
    }

    @Deprecated
    public EntryNameAndDigest removed(String name) {
        return null == removed ? null : removed.get(name);
    }

    @Override public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Diff)) return false;
        final Diff that = (Diff) obj;
        return  Objects.equals(this.algorithm, that.algorithm) &&
                Objects.equals(this.numBytes, that.numBytes) &&
                Objects.equals(this.unchanged, that.unchanged) &&
                Objects.equals(this.changed, that.changed) &&
                Objects.equals(this.added, that.added) &&
                Objects.equals(this.removed, that.removed);
    }

    @Override public int hashCode() {
        int hash = 17;
        hash = 31 * hash + Objects.hashCode(algorithm);
        hash = 31 * hash + Objects.hashCode(numBytes);
        hash = 31 * hash + Objects.hashCode(unchanged);
        hash = 31 * hash + Objects.hashCode(changed);
        hash = 31 * hash + Objects.hashCode(added);
        hash = 31 * hash + Objects.hashCode(removed);
        return hash;
    }
}
