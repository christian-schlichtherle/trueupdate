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
 * Adapts a map of entry names and digests to a collection so that JAXB can
 * marshall / unmarshall it.
 *
 * @author Christian Schlichtherle
 */
@Immutable
final class EntryNameAndDigestMapAdapter
extends XmlAdapter<EntryNameAndDigestCollectionHolder,
                   Map<String, EntryNameAndDigest>> {

    @Override public Map<String, EntryNameAndDigest> unmarshal(
            final EntryNameAndDigestCollectionHolder holder) {
        if (null == holder) return null;
        final Collection<EntryNameAndDigest> entries = holder.entries;
        assert null != entries;
        final Map<String, EntryNameAndDigest>
                map = new LinkedHashMap<>(HashMaps.initialCapacity(entries.size()));
        for (EntryNameAndDigest entryNameAndDigest : entries)
            map.put(entryNameAndDigest.name, entryNameAndDigest);
        return map;
    }

    @Override public EntryNameAndDigestCollectionHolder marshal(
            final Map<String, EntryNameAndDigest> map) {
        if (null == map) return null;
        final EntryNameAndDigestCollectionHolder
                holder = new EntryNameAndDigestCollectionHolder();
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
final class EntryNameAndDigestCollectionHolder {
    @XmlElement(name = "entry")
    public Collection<EntryNameAndDigest> entries;
}