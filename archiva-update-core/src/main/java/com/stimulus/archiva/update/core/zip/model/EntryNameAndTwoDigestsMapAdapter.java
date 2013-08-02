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
final class EntryNameAndTwoDigestsMapAdapter
extends XmlAdapter<EntryNameAndTwoDigestsCollectionHolder,
                   SortedMap<String, EntryNameAndTwoDigests>> {

    @Override
    public SortedMap<String, EntryNameAndTwoDigests> unmarshal(
            final @CheckForNull EntryNameAndTwoDigestsCollectionHolder holder) {
        if (null == holder) return null;
        final SortedMap<String, EntryNameAndTwoDigests>
                map = new TreeMap<>();
        for (EntryNameAndTwoDigests entryNameAndTwoDigests : holder.entry)
            map.put(entryNameAndTwoDigests.name, entryNameAndTwoDigests);
        return map;
    }

    @Override
    public EntryNameAndTwoDigestsCollectionHolder marshal(
            final @CheckForNull SortedMap<String, EntryNameAndTwoDigests> map) {
        if (null == map) return null;
        final EntryNameAndTwoDigestsCollectionHolder
                holder = new EntryNameAndTwoDigestsCollectionHolder();
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
final class EntryNameAndTwoDigestsCollectionHolder {
    public Collection<EntryNameAndTwoDigests> entry;
}
