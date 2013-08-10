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
import javax.annotation.CheckForNull;
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
@XmlRootElement(name = "diff")
@XmlAccessorType(XmlAccessType.FIELD)
public final class DiffModel implements Serializable {

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
    private final @CheckForNull Integer numBytes;

    @XmlJavaTypeAdapter(EntryNameAndDigestMapAdapter.class)
    private final Map<String, EntryNameAndDigest> unchanged;

    @XmlJavaTypeAdapter(EntryNameAndTwoDigestsMapAdapter.class)
    private final Map<String, EntryNameAndTwoDigests> changed;

    @XmlJavaTypeAdapter(EntryNameAndDigestMapAdapter.class)
    private final Map<String, EntryNameAndDigest> added, removed;

    /** Required for JAXB. */
    private DiffModel() {
        algorithm = "";
        numBytes = null;
        unchanged = added = removed = emptyMap();
        changed = emptyMap();
    }

    DiffModel(final Builder b) {
        this.algorithm = b.digestAlgorithmName();
        this.numBytes = b.digestLengthBytes();
        this.unchanged = b.unchanged();
        this.changed = b.changed();
        this.added = b.added();
        this.removed = b.removed();
    }

    /** Returns the message digest algorithm name. */
    public String digestAlgorithmName() { return algorithm; }

    /**
     * Returns the message digest byte length.
     * This is {@code null} if and only if the byte length of the message
     * digest used to build this diff model is the default value for the
     * algorithm.
     */
    public @Nullable Integer digestByteLength() { return numBytes; }

    /**
     * Returns a collection of the entry name and message digest for the
     * <i>unchanged</i> entries.
     */
    public Collection<EntryNameAndDigest> unchanged() {
        return unchanged.values();
    }

    /** Looks up the given entry name in the <i>unchanged</i> entries. */
    @Deprecated public EntryNameAndDigest unchanged(String name) {
        return unchanged.get(name);
    }

    /**
     * Returns a collection of the entry name and two message digests for the
     * <i>changed</i> entries.
     */
    public Collection<EntryNameAndTwoDigests> changed() {
        return changed.values();
    }

    /** Looks up the given entry name in the <i>changed</i> entries. */
    public EntryNameAndTwoDigests changed(String name) {
        return changed.get(name);
    }

    /**
     * Returns a collection of the entry name and message digest for the
     * <i>added</i> entries.
     */
    public Collection<EntryNameAndDigest> added() {
        return added.values();
    }

    /** Looks up the given entry name in the <i>added</i> entries. */
    public EntryNameAndDigest added(String name) {
        return added.get(name);
    }

    /**
     * Returns a collection of the entry name and message digest for the
     * <i>removed</i> entries.
     */
    public Collection<EntryNameAndDigest> removed() {
        return removed.values();
    }

    /** Looks up the given entry name in the <i>removed</i> entries. */
    @Deprecated public EntryNameAndDigest removed(String name) {
        return removed.get(name);
    }

    @Override public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DiffModel)) return false;
        final DiffModel that = (DiffModel) obj;
        return  this.algorithm.equals(that.algorithm) &&
                Objects.equals(this.numBytes, that.numBytes) &&
                this.unchanged.equals(that.unchanged) &&
                this.changed.equals(that.changed) &&
                this.added.equals(that.added) &&
                this.removed.equals(that.removed);
    }

    @Override public int hashCode() {
        int hash = 17;
        hash = 31 * hash + algorithm.hashCode();
        hash = 31 * hash + Objects.hashCode(numBytes);
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
    public static DiffModel decodeFromXml(Source source) throws Exception {
        return new JaxbCodec(jaxbContext()).decode(source, DiffModel.class);
    }

    /** Returns a JAXB context which binds only this class. */
    public static JAXBContext jaxbContext() { return Lazy.JAXB_CONTEXT; }

    private static class Lazy {

        static final JAXBContext JAXB_CONTEXT;

        static {
            try { JAXB_CONTEXT = JAXBContext.newInstance(DiffModel.class); }
            catch (JAXBException ex) { throw new AssertionError(ex); }
        }
    }

    /**
     * A builder for a diff model.
     * The default value for the collection of <i>unchanged</i>, <i>changed</i>,
     * <i>added</i> and <i>removed</i> entry names and message digests is an
     * empty collection.
     */
    public static final class Builder {

        private MessageDigest digest;
        private Collection<EntryNameAndDigest>
                unchanged = emptyList(),
                added = emptyList(),
                removed = emptyList();
        private Collection<EntryNameAndTwoDigests>
                changed = emptyList();

        String digestAlgorithmName() { return digest.getAlgorithm(); }

        @Nullable Integer digestLengthBytes() {
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

        public Builder digest(final MessageDigest digest) {
            this.digest = requireNonNull(digest);
            return this;
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

        public DiffModel build() { return new DiffModel(this); }
    }
}
