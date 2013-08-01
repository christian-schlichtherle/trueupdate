/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.core.zip.model;

import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.CheckForNull;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Adapts a sorted map of pairs of entry digests to a collection so that JAXB
 * can marshall / unmarshall it.
 *
 * @author Christian Schlichtherle
 */
@Immutable
final class EntryNameWithTwoDigestsMapAdapter
extends XmlAdapter<EntryNameWithTwoDigestsCollectionHolder,
                   SortedMap<String, EntryNameWithTwoDigests>> {

    @Override
    public SortedMap<String, EntryNameWithTwoDigests> unmarshal(
            final @CheckForNull EntryNameWithTwoDigestsCollectionHolder holder) {
        if (null == holder) return null;
        final SortedMap<String, EntryNameWithTwoDigests>
                map = new TreeMap<>();
        for (final EntryNameWithTwoDigests entryNameWithTwoDigests : holder.entry)
            map.put(entryNameWithTwoDigests.name, entryNameWithTwoDigests);
        return map;
    }

    @Override
    public EntryNameWithTwoDigestsCollectionHolder marshal(
            final @CheckForNull SortedMap<String, EntryNameWithTwoDigests> map) {
        if (null == map) return null;
        final EntryNameWithTwoDigestsCollectionHolder
                holder = new EntryNameWithTwoDigestsCollectionHolder();
        holder.entry = map.values();
        return holder;
    }
}

/**
 * A Data Transfer Object (DTO) for JAXB.
 * Mind you that this class is mutable.
 *
 * @author Christian Schlichtherle
 */
final class EntryNameWithTwoDigestsCollectionHolder {
    public Collection<EntryNameWithTwoDigests> entry;
}
