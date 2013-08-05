/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip.model;

import java.util.Objects;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * Models a ZIP entry name and message digest in canonical string notation.
 * Mind you that this class is mutable and may have null fields.
 *
 * @author Christian Schlichtherle
 */
public final class EntryNameAndDigest {

    @XmlAttribute(required = true)
    public String name, digest;

    /** Required by JAXB. */
    public EntryNameAndDigest() { }

    /** Courtesy constructor. */
    public EntryNameAndDigest(final String name, final String digest) {
        this.name = name;
        this.digest = digest;
    }

    @Override public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof EntryNameAndDigest)) return false;
        final EntryNameAndDigest that = (EntryNameAndDigest) obj;
        return  Objects.equals(this.name, that.name) &&
                Objects.equals(this.digest, that.digest);
    }

    @Override public int hashCode() {
        int hash = 17;
        hash = 31 * hash + Objects.hashCode(name);
        hash = 31 * hash + Objects.hashCode(digest);
        return hash;
    }
}
