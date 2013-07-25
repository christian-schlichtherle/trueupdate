/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jardiff;

import static com.stimulus.archiva.update.server.jardiff.MessageDigests.digestToHexString;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A Data Transfer Object which represents a JAR {@link Diff} as a set of maps
 * from entry names to message digests.
 * The message digests are represented as unsigned hex integer strings as
 * created by {@link MessageDigests#hexString}.
 *
 * @author Christian Schlichtherle
 */
@XmlRootElement
final class Index {

    //@XmlJavaTypeAdapter(Foo.class)
    public final SortedMap<String, String>
            removed = new TreeMap<>(),
            added = new TreeMap<>(),
            unchanged = new TreeMap<>();

    public final SortedMap<String, Change> changed = new TreeMap<>();

    /**
     * Constructs an empty index.
     * Required by JAXB.
     */
    Index() { }

    /**
     * Constructs an index which is populated with the entry names from
     * @param diff
     * @param digest
     * @throws IOException
     */
    Index(final Diff diff, final MessageDigest digest) throws IOException {
        populateRemoved(diff, digest);
        populateAdded(diff, digest);
        populateUnchanged(diff, digest);
        populateChanged(diff, digest);
    }

    private void populateRemoved(final Diff diff, final MessageDigest digest) throws IOException {
        for (final EntryInFile entryInFile1 : diff.entriesInFile1())
            removed.put(entryInFile1.entry().getName(),
                    digestToHexString(digest, entryInFile1));
    }

    private void populateAdded(final Diff diff, final MessageDigest digest) throws IOException {
        for (final EntryInFile entryInFile2 : diff.entriesInFile2())
            added.put(entryInFile2.entry().getName(),
                    digestToHexString(digest, entryInFile2));
    }

    private void populateUnchanged(final Diff diff, final MessageDigest digest) throws IOException {
        for (final PairOfEntriesInFiles pairOfEntriesInFiles : diff.equalEntries()) {
            final EntryInFile entryInFile1 = pairOfEntriesInFiles.entryInFile1();
            final EntryInFile entryInFile2 = pairOfEntriesInFiles.entryInFile2();
            assert entryInFile1.entry().getName().equals(entryInFile2.entry().getName());
            unchanged.put(entryInFile1.entry().getName(),
                    digestToHexString(digest, entryInFile1));
        }
    }

    private void populateChanged(final Diff diff, final MessageDigest digest) throws IOException {
        for (final PairOfEntriesInFiles pairOfEntriesInFiles : diff.differentEntries()) {
            final EntryInFile entryInFile1 = pairOfEntriesInFiles.entryInFile1();
            final EntryInFile entryInFile2 = pairOfEntriesInFiles.entryInFile2();
            assert entryInFile1.entry().getName().equals(entryInFile2.entry().getName());
            changed.put(entryInFile1.entry().getName(), new Change(
                    digestToHexString(digest, entryInFile1),
                    digestToHexString(digest, entryInFile2)));
        }
    }

    @Override public boolean equals(final Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Index)) return false;
        final Index that = (Index) obj;
        return this.removed.equals(that.removed) &&
                this.added.equals(that.added) &&
                this.unchanged.equals(that.unchanged) &&
                this.changed.equals(that.changed);
    }

    @Override public int hashCode() {
        int hashCode = 17;
        hashCode = 31 * hashCode + removed.hashCode();
        hashCode = 31 * hashCode + added.hashCode();
        hashCode = 31 * hashCode + unchanged.hashCode();
        hashCode = 31 * hashCode + changed.hashCode();
        return hashCode;
    }
}

/**
 * A Data Transfer Object which represents a message digest change.
 * The message digests are represented as unsigned hex integer strings as
 * created by {@link MessageDigests#hexString}.
 *
 * @author Christian Schlichtherle
 */
final class Change {

    Change() { }

    Change(final String before, final String after) {
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
