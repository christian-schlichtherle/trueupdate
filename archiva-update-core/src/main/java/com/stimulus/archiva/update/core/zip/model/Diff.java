/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.core.zip.model;

import java.util.*;
import javax.annotation.CheckForNull;
import javax.annotation.concurrent.NotThreadSafe;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * A ZIP diff bean represents the meta data in a ZIP patch file.
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

    @XmlJavaTypeAdapter(EntryNameWithDigestMapAdapter.class)
    public SortedMap<String, EntryNameWithDigest> unchanged;

    @XmlJavaTypeAdapter(EntryNameWithTwoDigestsMapAdapter.class)
    public SortedMap<String, EntryNameWithTwoDigests> changed;

    @XmlJavaTypeAdapter(EntryNameWithDigestMapAdapter.class)
    public SortedMap<String, EntryNameWithDigest> added, removed;

    @Deprecated
    public @CheckForNull
    EntryNameWithDigest unchanged(String name) {
        return null == unchanged ? null : unchanged.get(name);
    }

    public @CheckForNull
    EntryNameWithTwoDigests changed(String name) {
        return null == changed ? null : changed.get(name);
    }

    public @CheckForNull
    EntryNameWithDigest added(String name) {
        return null == added ? null : added.get(name);
    }

    @Deprecated
    public @CheckForNull
    EntryNameWithDigest removed(String name) {
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
