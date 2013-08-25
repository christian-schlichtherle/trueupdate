/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip.model;

import java.io.Serializable;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.*;
import static net.java.trueupdate.shed.Objects.requireNonNull;

/**
 * A Value Object which represents a ZIP entry name and message digest in
 * canonical string notation.
 *
 * @author Christian Schlichtherle
 */
@Immutable
@XmlAccessorType(XmlAccessType.FIELD)
public final class EntryNameAndDigest implements Serializable {

    private static final long serialVersionUID = 0L;

    @XmlAttribute(required = true)
    private final String name, digest;

    /** Required for JAXB. */
    private EntryNameAndDigest() { name = digest = ""; }

    public EntryNameAndDigest(final String name, final String digest) {
        this.name = requireNonNull(name);
        this.digest = requireNonNull(digest);
    }

    /** Returns the entry name. */
    public String name() { return name; }

    /** Returns the value of the message digest. */
    public String digest() { return digest; }

    @Override public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof EntryNameAndDigest)) return false;
        final EntryNameAndDigest that = (EntryNameAndDigest) obj;
        return  this.name().equals(that.name()) &&
                this.digest().equals(that.digest());
    }

    @Override public int hashCode() {
        int hash = 17;
        hash = 31 * hash + name().hashCode();
        hash = 31 * hash + digest().hashCode();
        return hash;
    }
}
