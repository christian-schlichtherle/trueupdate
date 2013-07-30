/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jar.model;

import java.util.*;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * The JAR diff bean represents the meta data in a JAR diff file.
 *
 * @author Christian Schlichtherle
 */
@XmlRootElement
public final class Diff {

    @XmlAttribute
    public String algorithm;

    @XmlAttribute
    public Integer numBytes;

    @XmlJavaTypeAdapter(EntryDigestMapAdapter.class)
    public SortedMap<String, EntryDigest> removed, added, unchanged;

    @XmlJavaTypeAdapter(FirstAndSecondEntryDigestMapAdapter.class)
    public SortedMap<String, FirstAndSecondEntryDigest> changed;

    public EntryDigest removed(String name) {
        return null == removed ? null : removed.get(name);
    }

    public EntryDigest added(String name) {
        return null == added ? null : added.get(name);
    }

    public EntryDigest unchanged(String name) {
        return null == unchanged ? null : unchanged.get(name);
    }

    public FirstAndSecondEntryDigest changed(String name) {
        return null == changed ? null : changed.get(name);
    }

    @Override public boolean equals(final Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Diff)) return false;
        final Diff that = (Diff) obj;
        return Objects.equals(this.removed, that.removed) &&
                Objects.equals(this.added, that.added) &&
                Objects.equals(this.unchanged, that.unchanged) &&
                Objects.equals(this.changed, that.changed);
    }

    @Override public int hashCode() {
        int hashCode = 17;
        hashCode = 31 * hashCode + Objects.hashCode(removed);
        hashCode = 31 * hashCode + Objects.hashCode(added);
        hashCode = 31 * hashCode + Objects.hashCode(unchanged);
        hashCode = 31 * hashCode + Objects.hashCode(changed);
        return hashCode;
    }
}
