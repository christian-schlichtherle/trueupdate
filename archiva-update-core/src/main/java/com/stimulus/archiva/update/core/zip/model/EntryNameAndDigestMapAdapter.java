/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.core.zip.model;

import java.util.*;
import javax.annotation.CheckForNull;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Adapts a sorted map of entry digests to a collection so that JAXB can
 * marshall / unmarshall it.
 *
 * @author Christian Schlichtherle
 */
@Immutable
final class EntryNameAndDigestMapAdapter
extends XmlAdapter<EntryNameAndDigestCollectionHolder,
                   SortedMap<String, EntryNameAndDigest>> {

    @Override
    public SortedMap<String, EntryNameAndDigest> unmarshal(
            final @CheckForNull EntryNameAndDigestCollectionHolder holder) {
        if (null == holder) return null;
        final SortedMap<String, EntryNameAndDigest> map = new TreeMap<>();
        for (EntryNameAndDigest entryNameAndDigest : holder.entry)
            map.put(entryNameAndDigest.name, entryNameAndDigest);
        return map;
    }

    @Override
    public EntryNameAndDigestCollectionHolder marshal(
            final @CheckForNull SortedMap<String, EntryNameAndDigest> map) {
        if (null == map) return null;
        final EntryNameAndDigestCollectionHolder
                holder = new EntryNameAndDigestCollectionHolder();
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
final class EntryNameAndDigestCollectionHolder {
    public Collection<EntryNameAndDigest> entry;
}
