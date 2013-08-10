/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip.model;

import java.io.Serializable;
import java.security.MessageDigest;
import java.util.*;
import static java.util.Collections.*;
import static java.util.Objects.requireNonNull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.*;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import net.java.trueupdate.core.codec.JaxbCodec;
import net.java.trueupdate.core.io.*;
import net.java.trueupdate.core.util.*;

/**
 * A Value Object which represents the meta data in a ZIP patch file.
 * It encapsulates maps of unchanged, changed, added and removed entry names
 * and message digests in canonical string notation, attributed with the
 * message digest algorithm name and byte length.
 *
 * @author Christian Schlichtherle
 */
@Immutable
@XmlRootElement(name =  "diff")
@XmlAccessorType(XmlAccessType.FIELD)
public final class Diff implements Serializable {

    private static final long serialVersionUID = 0L;

    /**
     * The name of the entry which contains the marshalled diff model in a
     * ZIP patch file.
     * This should be the first entry in the ZIP patch file.
     */
    public static final String ENTRY_NAME = "META-INF/diff.xml";

    @XmlAttribute(required = true)
    private final String algorithm;

    @XmlAttribute
    private final @Nullable Integer numBytes;

    @XmlJavaTypeAdapter(EntryNameAndDigestMapAdapter.class)
    private final Map<String, EntryNameAndDigest> unchanged;

    @XmlJavaTypeAdapter(EntryNameAndTwoDigestsMapAdapter.class)
    private final Map<String, EntryNameAndTwoDigests> changed;

    @XmlJavaTypeAdapter(EntryNameAndDigestMapAdapter.class)
    private final Map<String, EntryNameAndDigest> added, removed;

    /** Required for JAXB. */
    private Diff() {
        algorithm = "";
        numBytes = null;
        unchanged = added = removed = emptyMap();
        changed = emptyMap();
    }

    Diff(final Builder b) {
        this.algorithm = b.algorithm();
        this.numBytes = b.numBytes();
        this.unchanged = b.unchanged();
        this.changed = b.changed();
        this.added = b.added();
        this.removed = b.removed();
    }

    static Map<String, EntryNameAndDigest> unchangedMap(
            final Collection<EntryNameAndDigest> entries) {
        final Map<String, EntryNameAndDigest> map = new LinkedHashMap<>(
                initialCapacity(entries));
        for (EntryNameAndDigest entryNameAndDigest : entries)
            map.put(entryNameAndDigest.name, entryNameAndDigest);
        return unmodifiableMap(map);
    }

    static Map<String, EntryNameAndTwoDigests> changedMap(
            final Collection<EntryNameAndTwoDigests> entries) {
        final Map<String, EntryNameAndTwoDigests> map = new LinkedHashMap<>(
                initialCapacity(entries));
        for (EntryNameAndTwoDigests entryNameAndTwoDigests : entries)
            map.put(entryNameAndTwoDigests.name, entryNameAndTwoDigests);
        return unmodifiableMap(map);
    }

    private static int initialCapacity(Collection<?> c) {
        return HashMaps.initialCapacity(c.size());
    }

    public String algorithm() { return algorithm; }
    public @Nullable Integer numBytes() { return numBytes; }

    public Collection<EntryNameAndDigest> unchanged() {
        return unchanged.values();
    }

    public Collection<EntryNameAndTwoDigests> changed() {
        return changed.values();
    }

    public Collection<EntryNameAndDigest> added() {
        return added.values();
    }

    public Collection<EntryNameAndDigest> removed() {
        return removed.values();
    }

    @Deprecated public EntryNameAndDigest unchanged(String name) {
        return unchanged.get(name);
    }

    public EntryNameAndTwoDigests changed(String name) {
        return changed.get(name);
    }

    public EntryNameAndDigest added(String name) {
        return added.get(name);
    }

    @Deprecated public EntryNameAndDigest removed(String name) {
        return removed.get(name);
    }

    @Override public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Diff)) return false;
        final Diff that = (Diff) obj;
        return  this.algorithm().equals(that.algorithm()) &&
                Objects.equals(this.numBytes(), that.numBytes()) &&
                this.unchanged.equals(that.unchanged) &&
                this.changed.equals(that.changed) &&
                this.added.equals(that.added) &&
                this.removed.equals(that.removed);
    }

    @Override public int hashCode() {
        int hash = 17;
        hash = 31 * hash + Objects.hashCode(algorithm());
        hash = 31 * hash + Objects.hashCode(numBytes());
        hash = 31 * hash + unchanged.hashCode();
        hash = 31 * hash + changed.hashCode();
        hash = 31 * hash + added.hashCode();
        hash = 31 * hash + removed.hashCode();
        return hash;
    }

    /**
     * Encodes this diff model to XML.
     *
     * @param sink the sink for writing the XML.
     * @throws Exception at the discretion of the JAXB codec, e.g. if the
     *         sink isn't writable.
     */
    public void encodeToXml(Sink sink) throws Exception {
        new JaxbCodec(jaxbContext()).encode(sink, this);
    }

    /**
     * Decodes a diff model from XML.
     *
     * @param source the source for reading the XML.
     * @return the decoded diff model.
     * @throws Exception at the discretion of the JAXB codec, e.g. if the
     *         source isn't readable.
     */
    public static Diff decodeFromXml(Source source) throws Exception {
        return new JaxbCodec(jaxbContext()).decode(source, Diff.class);
    }

    /** Returns a JAXB context which binds only this class. */
    public static JAXBContext jaxbContext() { return Lazy.JAXB_CONTEXT; }

    private static class Lazy {

        static final JAXBContext JAXB_CONTEXT;

        static {
            try { JAXB_CONTEXT = JAXBContext.newInstance(Diff.class); }
            catch (JAXBException ex) { throw new AssertionError(ex); }
        }
    }

    /** A builder for a diff model. */
    @SuppressWarnings("PackageVisibleField")
    public static final class Builder {

        private MessageDigest digest;
        private Collection<EntryNameAndDigest>
                unchanged = emptyList(),
                added = emptyList(),
                removed = emptyList();
        private Collection<EntryNameAndTwoDigests>
                changed = emptyList();

        public Builder digest(final MessageDigest digest) {
            this.digest = requireNonNull(digest);
            return this;
        }

        String algorithm() { return digest.getAlgorithm(); }

        Integer numBytes() {
            try {
                final MessageDigest
                        clone = MessageDigests.newDigest(digest.getAlgorithm());
                if (clone.getDigestLength() == digest.getDigestLength())
                    return null;
            } catch (IllegalArgumentException fallThrough) {
            }
            return digest.getDigestLength();
        }

        Map<String, EntryNameAndDigest> unchanged() {
            return unchangedMap(unchanged);
        }

        Map<String, EntryNameAndTwoDigests> changed() {
            return changedMap(changed);
        }

        Map<String, EntryNameAndDigest> added() {
            return unchangedMap(added);
        }

        Map<String, EntryNameAndDigest> removed() {
            return unchangedMap(removed);
        }

        public Builder unchanged(final Collection<EntryNameAndDigest> unchanged) {
            this.unchanged = requireNonNull(unchanged);
            return this;
        }

        public Builder changed(final Collection<EntryNameAndTwoDigests> changed) {
            this.changed = requireNonNull(changed);
            return this;
        }

        public Builder added(final Collection<EntryNameAndDigest> added) {
            this.added = requireNonNull(added);
            return this;
        }

        public Builder removed(final Collection<EntryNameAndDigest> removed) {
            this.removed = requireNonNull(removed);
            return this;
        }

        public Diff build() { return new Diff(this); }
    }
}
