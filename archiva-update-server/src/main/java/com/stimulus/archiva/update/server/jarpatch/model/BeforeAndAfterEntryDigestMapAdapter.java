/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jarpatch.model;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Adapts a sorted map of pairs of entry digests to a collection so that JAXB
 * can marshall / unmarshall it.
 *
 * @author Christian Schlichtherle
 */
@Immutable
final class BeforeAndAfterEntryDigestMapAdapter extends XmlAdapter<BeforeAndAfterEntryDigestCollectionHolder, SortedMap<String, BeforeAndAfterEntryDigest>> {
    @Override
    public SortedMap<String, BeforeAndAfterEntryDigest> unmarshal(BeforeAndAfterEntryDigestCollectionHolder holder) {
        if (null == holder) return null;
        final SortedMap<String, BeforeAndAfterEntryDigest> map = new TreeMap<>();
        for (final BeforeAndAfterEntryDigest beforeAndAfterEntryDigest : holder.entry)
            map.put(beforeAndAfterEntryDigest.name, beforeAndAfterEntryDigest);
        return map;
    }

    @Override
    public BeforeAndAfterEntryDigestCollectionHolder marshal(SortedMap<String, BeforeAndAfterEntryDigest> map) {
        if (null == map) return null;
        final BeforeAndAfterEntryDigestCollectionHolder holder = new BeforeAndAfterEntryDigestCollectionHolder();
        holder.entry = map.values();
        return holder;
    }
}

/**
 * A Data Transfer Object (DTO) for JAXB.
 *
 * @author Christian Schlichtherle
 */
final class BeforeAndAfterEntryDigestCollectionHolder {
    public Collection<BeforeAndAfterEntryDigest> entry;
}
