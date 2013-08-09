/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip.model;

import net.java.trueupdate.core.util.HashMaps;

import java.util.*;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Adapts a map of entry names and two digests to a collection so that JAXB
 * can marshall / unmarshall it.
 *
 * @author Christian Schlichtherle
 */
@Immutable
final class EntryNameAndTwoDigestsMapAdapter
extends XmlAdapter<EntryNameAndTwoDigestsCollectionHolder,
                   Map<String, EntryNameAndTwoDigests>> {

    @Override public Map<String, EntryNameAndTwoDigests> unmarshal(
            final EntryNameAndTwoDigestsCollectionHolder holder) {
        if (null == holder) return null;
        final Collection<EntryNameAndTwoDigests> entries = holder.entries;
        assert null != entries;
        final Map<String, EntryNameAndTwoDigests>
                map = new LinkedHashMap<>(HashMaps.initialCapacity(entries.size()));
        for (EntryNameAndTwoDigests entryNameAndTwoDigests : entries)
            map.put(entryNameAndTwoDigests.name, entryNameAndTwoDigests);
        return map;
    }

    @Override public EntryNameAndTwoDigestsCollectionHolder marshal(
            final Map<String, EntryNameAndTwoDigests> map) {
        if (null == map) return null;
        final EntryNameAndTwoDigestsCollectionHolder
                holder = new EntryNameAndTwoDigestsCollectionHolder();
        holder.entries = map.values();
        return holder;
    }
}

/**
 * A Data Transfer Object (DTO) for JAXB.
 * Mind you that this class is mutable and may have null fields.
 *
 * @author Christian Schlichtherle
 */
final class EntryNameAndTwoDigestsCollectionHolder {
    @XmlElement(name = "entry")
    public Collection<EntryNameAndTwoDigests> entries;
}