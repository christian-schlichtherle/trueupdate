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
 * A Value Object which represents a ZIP entry name and two message digests in
 * canonical string notation.
 *
 * @author Christian Schlichtherle
 */
@Immutable
@XmlAccessorType(XmlAccessType.FIELD)
public final class ZipEntryNameAndTwoDigestValues implements Serializable {

    private static final long serialVersionUID = 0L;

    @XmlAttribute(required = true)
    private final String entryName, firstDigestValue, secondDigestValue;

    /** Required for JAXB. */
    private ZipEntryNameAndTwoDigestValues() {
        entryName = firstDigestValue = secondDigestValue = "";
    }

    /**
     * Default constructor.
     * The first and second message digest should not be equal.
     */
    public ZipEntryNameAndTwoDigestValues(
            final String entryName,
            final String firstDigestValue,
            final String secondDigestValue) {
        this.entryName = requireNonNull(entryName);
        this.firstDigestValue = requireNonNull(firstDigestValue);
        this.secondDigestValue = requireNonNull(secondDigestValue);
        assert !firstDigestValue.equals(secondDigestValue);
    }

    /** Returns the entry name. */
    public String entryName() { return entryName; }

    /** Returns the first message digest. */
    public String firstDigestValue() { return firstDigestValue; }

    /** Returns the second message digest. */
    public String secondDigestValue() { return secondDigestValue; }

    /** Returns the entry name with the first digest. */
    @Deprecated
    public ZipEntryNameAndDigestValue entryNameWithFirstDigest() {
        return new ZipEntryNameAndDigestValue(entryName(), firstDigestValue());
    }

    /** Returns the entry name with the second digest. */
    public ZipEntryNameAndDigestValue entryNameWithSecondDigest() {
        return new ZipEntryNameAndDigestValue(entryName(), secondDigestValue());
    }

    @Override public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ZipEntryNameAndTwoDigestValues)) return false;
        final ZipEntryNameAndTwoDigestValues that = (ZipEntryNameAndTwoDigestValues) obj;
        return  this.entryName().equals(that.entryName()) &&
                this.firstDigestValue().equals(that.firstDigestValue()) &&
                this.secondDigestValue().equals(that.secondDigestValue());
    }

    @Override public int hashCode() {
        int hash = 17;
        hash = 31 * hash + entryName().hashCode();
        hash = 31 * hash + firstDigestValue().hashCode();
        hash = 31 * hash + secondDigestValue().hashCode();
        return hash;
    }
}
