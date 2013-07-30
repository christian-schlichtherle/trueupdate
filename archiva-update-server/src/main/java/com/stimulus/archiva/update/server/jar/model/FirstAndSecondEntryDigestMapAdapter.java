/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jar.model;

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
final class FirstAndSecondEntryDigestMapAdapter
extends XmlAdapter<FirstAndSecondEntryDigestCollectionHolder, SortedMap<String, FirstAndSecondEntryDigest>> {

    @Override
    public SortedMap<String, FirstAndSecondEntryDigest> unmarshal(FirstAndSecondEntryDigestCollectionHolder holder) {
        if (null == holder) return null;
        final SortedMap<String, FirstAndSecondEntryDigest> map = new TreeMap<>();
        for (final FirstAndSecondEntryDigest firstAndSecondEntryDigest : holder.entry)
            map.put(firstAndSecondEntryDigest.name, firstAndSecondEntryDigest);
        return map;
    }

    @Override
    public FirstAndSecondEntryDigestCollectionHolder marshal(SortedMap<String, FirstAndSecondEntryDigest> map) {
        if (null == map) return null;
        final FirstAndSecondEntryDigestCollectionHolder holder = new FirstAndSecondEntryDigestCollectionHolder();
        holder.entry = map.values();
        return holder;
    }
}

/**
 * A Data Transfer Object (DTO) for JAXB.
 *
 * @author Christian Schlichtherle
 */
final class FirstAndSecondEntryDigestCollectionHolder {
    public Collection<FirstAndSecondEntryDigest> entry;
}
