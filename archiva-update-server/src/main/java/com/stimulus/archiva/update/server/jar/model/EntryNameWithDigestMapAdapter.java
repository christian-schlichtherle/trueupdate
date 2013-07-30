/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jar.model;

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
final class EntryNameWithDigestMapAdapter
extends XmlAdapter<EntryNameWithDigestCollectionHolder,
                   SortedMap<String, EntryNameWithDigest>> {

    @Override
    public SortedMap<String, EntryNameWithDigest> unmarshal(
            final @CheckForNull EntryNameWithDigestCollectionHolder holder) {
        if (null == holder) return null;
        final SortedMap<String, EntryNameWithDigest> map = new TreeMap<>();
        for (final EntryNameWithDigest entryNameWithDigest : holder.entry)
            map.put(entryNameWithDigest.name, entryNameWithDigest);
        return map;
    }

    @Override
    public EntryNameWithDigestCollectionHolder marshal(
            final @CheckForNull SortedMap<String, EntryNameWithDigest> map) {
        if (null == map) return null;
        final EntryNameWithDigestCollectionHolder
                holder = new EntryNameWithDigestCollectionHolder();
        holder.entry = map.values();
        return holder;
    }
}

/**
 * A Data Transfer Object (DTO) for JAXB.
 *
 * @author Christian Schlichtherle
 */
final class EntryNameWithDigestCollectionHolder {
    public Collection<EntryNameWithDigest> entry;
}
