/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jarpatch.model;

import com.stimulus.archiva.update.server.jardiff.model.*;
import static com.stimulus.archiva.update.server.util.MessageDigests.digestToHexString;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Represents the meta data of a JAR patch.
 *
 * @author Christian Schlichtherle
 */
@XmlRootElement
public final class Index {

    @XmlJavaTypeAdapter(EntryDigestMapAdapter.class)
    public SortedMap<String, EntryDigest> removed, added, unchanged;

    @XmlJavaTypeAdapter(BeforeAndAfterEntryDigestMapAdapter.class)
    public SortedMap<String, BeforeAndAfterEntryDigest> changed;

    @Override public boolean equals(final Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Index)) return false;
        final Index that = (Index) obj;
        return Objects.equals(this.removed, that.removed) &&
                Objects.equals(this.added, that.added) &&
                Objects.equals(this.unchanged, that.unchanged) &&
                Objects.equals(this.changed, that.changed);
    }

    @Override public int hashCode() {
        int hashCode = 17;
        hashCode = 31 * hashCode + Objects.hashCode(removed);
        hashCode = 31 * hashCode + Objects.hashCode(added);
        hashCode = 31 * hashCode + Objects.hashCode(unchanged);
        hashCode = 31 * hashCode + Objects.hashCode(changed);
        return hashCode;
    }
}
