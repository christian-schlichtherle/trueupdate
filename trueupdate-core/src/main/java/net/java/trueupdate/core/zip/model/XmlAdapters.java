/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip.model;

import java.util.*;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import static net.java.trueupdate.core.zip.model.ZipDiffModel.*;

@Immutable
final class ZipEntryNameAndDigestValueMapAdapter
        extends XmlAdapter<ZipEntryNameAndDigestValueCollectionHolder,
        Map<String, ZipEntryNameAndDigestValue>> {

    @Override public Map<String, ZipEntryNameAndDigestValue> unmarshal(
            ZipEntryNameAndDigestValueCollectionHolder holder) {
        return null == holder ? null : unchangedMap(holder.entries);
    }

    @Override public ZipEntryNameAndDigestValueCollectionHolder marshal(
            final Map<String, ZipEntryNameAndDigestValue> map) {
        if (null == map || map.isEmpty()) return null;
        final ZipEntryNameAndDigestValueCollectionHolder
                holder = new ZipEntryNameAndDigestValueCollectionHolder();
        holder.entries = map.values();
        return holder;
    }
}

final class ZipEntryNameAndDigestValueCollectionHolder {
    @XmlElement(name = "entry")
    public Collection<ZipEntryNameAndDigestValue> entries;
}

@Immutable
final class ZipEntryNameAndTwoDigestValuesMapAdapter
        extends XmlAdapter<ZipEntryNameAndTwoDigestValuesCollectionHolder,
        Map<String, ZipEntryNameAndTwoDigestValues>> {

    @Override public Map<String, ZipEntryNameAndTwoDigestValues> unmarshal(
            ZipEntryNameAndTwoDigestValuesCollectionHolder holder) {
        return null == holder ? null : changedMap(holder.entries);
    }

    @Override public ZipEntryNameAndTwoDigestValuesCollectionHolder marshal(
            final Map<String, ZipEntryNameAndTwoDigestValues> map) {
        if (null == map || map.isEmpty()) return null;
        final ZipEntryNameAndTwoDigestValuesCollectionHolder
                holder = new ZipEntryNameAndTwoDigestValuesCollectionHolder();
        holder.entries = map.values();
        return holder;
    }
}

final class ZipEntryNameAndTwoDigestValuesCollectionHolder {
    @XmlElement(name = "entry")
    public Collection<ZipEntryNameAndTwoDigestValues> entries;
}
