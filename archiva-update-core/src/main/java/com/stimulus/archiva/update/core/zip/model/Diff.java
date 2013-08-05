/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.core.zip.model;

import com.stimulus.archiva.update.core.codec.JaxbCodec;
import com.stimulus.archiva.update.core.io.Sink;
import com.stimulus.archiva.update.core.io.Source;

import java.util.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Models an optional unchanged, changed, added and removed map of entry names
 * and message digests in canonical string notation, attributed with the
 * message digest algorithm name and byte length.
 * This model class represents the meta data in a ZIP patch file.
 * Mind you that this class is mutable and may have null fields.
 *
 * @author Christian Schlichtherle
 */
@XmlRootElement
public final class Diff {

    /**
     * The name of the entry which contains the marshalled diff model in a
     * ZIP patch file.
     * This should be the first entry in the ZIP patch file.
     */
    public static final String ENTRY_NAME = "META-INF/diff.xml";

    @XmlAttribute(required = true)
    public String algorithm;

    @XmlAttribute
    public Integer numBytes;

    @XmlJavaTypeAdapter(EntryNameAndDigestMapAdapter.class)
    public Map<String, EntryNameAndDigest> unchanged;

    @XmlJavaTypeAdapter(EntryNameAndTwoDigestsMapAdapter.class)
    public Map<String, EntryNameAndTwoDigests> changed;

    @XmlJavaTypeAdapter(EntryNameAndDigestMapAdapter.class)
    public Map<String, EntryNameAndDigest> added, removed;

    @Deprecated
    public EntryNameAndDigest unchanged(String name) {
        return null == unchanged ? null : unchanged.get(name);
    }

    public EntryNameAndTwoDigests changed(String name) {
        return null == changed ? null : changed.get(name);
    }

    public EntryNameAndDigest added(String name) {
        return null == added ? null : added.get(name);
    }

    @Deprecated
    public EntryNameAndDigest removed(String name) {
        return null == removed ? null : removed.get(name);
    }

    @Override public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Diff)) return false;
        final Diff that = (Diff) obj;
        return  Objects.equals(this.algorithm, that.algorithm) &&
                Objects.equals(this.numBytes, that.numBytes) &&
                Objects.equals(this.unchanged, that.unchanged) &&
                Objects.equals(this.changed, that.changed) &&
                Objects.equals(this.added, that.added) &&
                Objects.equals(this.removed, that.removed);
    }

    @Override public int hashCode() {
        int hash = 17;
        hash = 31 * hash + Objects.hashCode(algorithm);
        hash = 31 * hash + Objects.hashCode(numBytes);
        hash = 31 * hash + Objects.hashCode(unchanged);
        hash = 31 * hash + Objects.hashCode(changed);
        hash = 31 * hash + Objects.hashCode(added);
        hash = 31 * hash + Objects.hashCode(removed);
        return hash;
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

    /** Returns a JAXB context which binds only this class. */
    public static JAXBContext jaxbContext() { return Lazy.JAXB_CONTEXT; }

    private static class Lazy {

        static final JAXBContext JAXB_CONTEXT;

        static {
            try { JAXB_CONTEXT = JAXBContext.newInstance(Diff.class); }
            catch (JAXBException ex) { throw new AssertionError(ex); }
        }
    }
}
