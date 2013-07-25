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

    /**
     * Constructs an empty index.
     * Required by JAXB.
     */
    public Index() { }

    /**
     * Constructs an index which is populated from the given JAR diff.
     *
     * @param diff the JAR diff.
     * @param digest the message digest to use.
     */
    public Index(final Diff diff, final MessageDigest digest) throws IOException {
        initRemoved(diff, digest);
        initAdded(diff, digest);
        initUnchanged(diff, digest);
        initChanged(diff, digest);
    }

    private void initRemoved(final Diff diff, final MessageDigest digest) throws IOException {
        removed = new TreeMap<>();
        for (final EntryInFile entryInFile1 : diff.entriesInFile1()) {
            final String name1 = entryInFile1.entry().getName();
            removed.put(name1, new EntryDigest(name1,
                    digestToHexString(digest, entryInFile1)));
        }
    }

    private void initAdded(final Diff diff, final MessageDigest digest) throws IOException {
        added = new TreeMap<>();
        for (final EntryInFile entryInFile2 : diff.entriesInFile2()) {
            final String name2 = entryInFile2.entry().getName();
            added.put(name2, new EntryDigest(name2,
                    digestToHexString(digest, entryInFile2)));
        }
    }

    private void initUnchanged(final Diff diff, final MessageDigest digest) throws IOException {
        unchanged = new TreeMap<>();
        for (final PairOfEntriesInFiles pairOfEntriesInFiles : diff.equalEntries()) {
            final EntryInFile entryInFile1 = pairOfEntriesInFiles.entryInFile1();
            final EntryInFile entryInFile2 = pairOfEntriesInFiles.entryInFile2();
            final String name1 = entryInFile1.entry().getName();
            assert name1.equals(entryInFile2.entry().getName());
            unchanged.put(name1, new EntryDigest(name1,
                    digestToHexString(digest, entryInFile1)));
        }
    }

    private void initChanged(final Diff diff, final MessageDigest digest) throws IOException {
        changed = new TreeMap<>();
        for (final PairOfEntriesInFiles pairOfEntriesInFiles : diff.differentEntries()) {
            final EntryInFile entryInFile1 = pairOfEntriesInFiles.entryInFile1();
            final EntryInFile entryInFile2 = pairOfEntriesInFiles.entryInFile2();
            final String name1 = entryInFile1.entry().getName();
            assert name1.equals(entryInFile2.entry().getName());
            changed.put(name1, new BeforeAndAfterEntryDigest(name1,
                    digestToHexString(digest, entryInFile1),
                    digestToHexString(digest, entryInFile2)));
        }
    }

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

