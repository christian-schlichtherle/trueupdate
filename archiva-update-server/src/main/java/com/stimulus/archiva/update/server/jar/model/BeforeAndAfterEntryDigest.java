/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jar.model;

import javax.xml.bind.annotation.XmlAttribute;
import java.util.Objects;

/**
 * Represents the digest before and after patching a JAR entry.
 *
 * @author Christian Schlichtherle
 */
public final class BeforeAndAfterEntryDigest {

    /** Constructor for use with JAXB. */
    public BeforeAndAfterEntryDigest() { }

    /** Default constructor. */
    public BeforeAndAfterEntryDigest(final String name, final String before, final String after) {
        this.name = name;
        this.before = before;
        this.after = after;
    }

    @XmlAttribute
    public String name, before, after;

    @Override public boolean equals(final Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof BeforeAndAfterEntryDigest)) return false;
        final BeforeAndAfterEntryDigest that = (BeforeAndAfterEntryDigest) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.before, that.before) &&
                Objects.equals(this.after, that.after);
    }

    @Override public int hashCode() {
        int hashCode = 17;
        hashCode = 31 * hashCode + Objects.hashCode(name);
        hashCode = 31 * hashCode + Objects.hashCode(before);
        hashCode = 31 * hashCode + Objects.hashCode(after);
        return hashCode;
    }
}
