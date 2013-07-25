/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jarpatch.model;

import java.util.*;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Adapts a sorted map of entry digests to a collection so that JAXB can
 * marshall / unmarshall it.
 *
 * @author Christian Schlichtherle
 */
@Immutable
final class EntryDigestMapAdapter
extends XmlAdapter<EntryDigestCollectionHolder, SortedMap<String, EntryDigest>> {
    @Override
    public SortedMap<String, EntryDigest> unmarshal(final EntryDigestCollectionHolder holder) {
        if (null == holder) return null;
        final SortedMap<String, EntryDigest> map = new TreeMap<>();
        for (final EntryDigest entryDigest : holder.entry)
            map.put(entryDigest.name, entryDigest);
        return map;
    }

    @Override
    public EntryDigestCollectionHolder marshal(final SortedMap<String, EntryDigest> map) {
        if (null == map) return null;
        final EntryDigestCollectionHolder holder = new EntryDigestCollectionHolder();
        holder.entry = map.values();
        return holder;
    }
}

/**
 * A Data Transfer Object (DTO) for JAXB.
 *
 * @author Christian Schlichtherle
 */
final class EntryDigestCollectionHolder {
    public Collection<EntryDigest> entry;
}
