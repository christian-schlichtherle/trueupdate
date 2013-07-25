/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jarpatch.model;

/**
 * @author Christian Schlichtherle
 */
public final class Entry {

    public Entry() { }

    public Entry(String name, String digest) {
        assert null != name;
        this.name = name;
        assert null != digest;
        this.digest = digest;
    }

    public String name, digest;

    @Override public boolean equals(final Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Entry)) return false;
        final Entry that = (Entry) obj;
        return this.name.equals(that.name) &&
                this.digest.equals(that.digest);
    }

    @Override public int hashCode() {
        int hashCode = 17;
        hashCode = 31 * hashCode + name.hashCode();
        hashCode = 31 * hashCode + digest.hashCode();
        return hashCode;
    }
}
