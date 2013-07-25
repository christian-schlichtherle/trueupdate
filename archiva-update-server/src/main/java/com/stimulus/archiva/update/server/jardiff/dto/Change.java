/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jardiff.dto;

/**
 * A Data Transfer Object which represents a message digest change.
 * The message digests are represented as unsigned hex integer strings as
 * created by {@link com.stimulus.archiva.update.server.jardiff.util.MessageDigests#hexString}.
 *
 * @author Christian Schlichtherle
 */
public final class Change {

    public Change() { }

    public Change(final String before, final String after) {
        assert null != before;
        this.before = before;
        assert null != after;
        this.after = after;
    }

    public String before, after;

    @Override public boolean equals(final Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Change)) return false;
        final Change that = (Change) obj;
        return this.before.equals(that.before) &&
                this.after.equals(that.after);
    }

    @Override public int hashCode() {
        int hashCode = 17;
        hashCode = 31 * hashCode + before.hashCode();
        hashCode = 31 * hashCode + after.hashCode();
        return hashCode;
    }
}
