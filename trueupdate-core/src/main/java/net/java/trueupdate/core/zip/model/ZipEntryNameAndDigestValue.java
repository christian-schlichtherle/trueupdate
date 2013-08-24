/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip.model;

import java.io.Serializable;
import static java.util.Objects.requireNonNull;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.*;

/**
 * A Value Object which represents a ZIP entry name and message digest in
 * canonical string notation.
 *
 * @author Christian Schlichtherle
 */
@Immutable
@XmlAccessorType(XmlAccessType.FIELD)
public final class ZipEntryNameAndDigestValue implements Serializable {

    private static final long serialVersionUID = 0L;

    @XmlAttribute(required = true)
    private final String entryName, digestValue;

    /** Required for JAXB. */
    private ZipEntryNameAndDigestValue() { entryName = digestValue = ""; }

    public ZipEntryNameAndDigestValue(final String entryName, final String digestValue) {
        this.entryName = requireNonNull(entryName);
        this.digestValue = requireNonNull(digestValue);
    }

    /** Returns the entry name. */
    public String entryName() { return entryName; }

    /** Returns the message digest. */
    public String digestValue() { return digestValue; }

    @Override public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ZipEntryNameAndDigestValue)) return false;
        final ZipEntryNameAndDigestValue that = (ZipEntryNameAndDigestValue) obj;
        return  this.entryName().equals(that.entryName()) &&
                this.digestValue().equals(that.digestValue());
    }

    @Override public int hashCode() {
        int hash = 17;
        hash = 31 * hash + entryName().hashCode();
        hash = 31 * hash + digestValue().hashCode();
        return hash;
    }
}
