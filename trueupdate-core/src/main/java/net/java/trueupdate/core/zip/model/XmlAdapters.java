/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip.model;

import java.util.*;
import javax.annotation.CheckForNull;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

@Immutable
final class EntryNameAndDigestMapAdapter
        extends XmlAdapter<EntryNameAndDigestCollectionHolder,
        Map<String, EntryNameAndDigest>> {

    @Override public Map<String, EntryNameAndDigest> unmarshal(
            final EntryNameAndDigestCollectionHolder holder) {
        if (null == holder) return null;
        final Collection<EntryNameAndDigest> entries = holder.entries;
        assert null != entries;
        return Diff.unchangedMap(entries);
    }

    @Override public EntryNameAndDigestCollectionHolder marshal(
            final Map<String, EntryNameAndDigest> map) {
        if (null == map || map.isEmpty()) return null;
        final EntryNameAndDigestCollectionHolder
                holder = new EntryNameAndDigestCollectionHolder();
        holder.entries = map.values();
        return holder;
    }
}

final class EntryNameAndDigestCollectionHolder {
    @XmlElement(name = "entry")
    public @CheckForNull Collection<EntryNameAndDigest> entries;
}

@Immutable
final class EntryNameAndTwoDigestsMapAdapter
        extends XmlAdapter<EntryNameAndTwoDigestsCollectionHolder,
        Map<String, EntryNameAndTwoDigests>> {

    @Override public Map<String, EntryNameAndTwoDigests> unmarshal(
            final EntryNameAndTwoDigestsCollectionHolder holder) {
        if (null == holder) return null;
        final Collection<EntryNameAndTwoDigests> entries = holder.entries;
        assert null != entries;
        return Diff.changedMap(entries);
    }

    @Override public EntryNameAndTwoDigestsCollectionHolder marshal(
            final Map<String, EntryNameAndTwoDigests> map) {
        if (null == map || map.isEmpty()) return null;
        final EntryNameAndTwoDigestsCollectionHolder
                holder = new EntryNameAndTwoDigestsCollectionHolder();
        holder.entries = map.values();
        return holder;
    }
}

final class EntryNameAndTwoDigestsCollectionHolder {
    @XmlElement(name = "entry")
    public @CheckForNull Collection<EntryNameAndTwoDigests> entries;
}
