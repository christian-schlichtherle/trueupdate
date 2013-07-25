/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jardiff.dto;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.*;

/**
 * @author Christian Schlichtherle
 */
@Immutable
public class Foo extends XmlAdapter<List<Entry>, SortedMap<String, String>> {
    @Override
    public SortedMap<String, String> unmarshal(final List<Entry> list) {
        final SortedMap<String, String> map = new TreeMap<>();
        for (final Entry entry : list)
            map.put(entry.name, entry.digest);
        return map;
    }

    @Override public List<Entry> marshal(final SortedMap<String, String> map) {
        final List<Entry> entries = new ArrayList<>(map.size());
        for (final Map.Entry<String, String> entry : map.entrySet())
            entries.add(new Entry(entry.getKey(), entry.getValue()));
        return entries;
    }
}
