/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jar;

import com.stimulus.archiva.update.server.jar.engine.*;
import com.stimulus.archiva.update.server.jarpatch.model.*;
import static com.stimulus.archiva.update.server.util.MessageDigests.digestToHexString;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.*;
import static java.util.Objects.requireNonNull;
import java.util.jar.JarFile;
import javax.annotation.*;
import javax.annotation.concurrent.Immutable;

/**
 * Computes a {@link Diff} from two JAR files.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class JarDiff {

    private final @WillNotClose JarFile file1, file2;

    /**
     * Constructs a JAR diff.
     *
     * @param file1 the first JAR file.
     * @param file2 the second JAR file.
     */
    public JarDiff(final @WillNotClose JarFile file1,
                   final @WillNotClose JarFile file2) {
        this.file1 = requireNonNull(file1);
        this.file2 = requireNonNull(file2);
    }

    /**
     * Computes a JAR diff of the two JAR files using the given message digest.
     *
     * @param digest the message digest to use.
     * @return the computed JAR diff.
     */
    public Diff compute(final MessageDigest digest) throws IOException {
        final SortedMap<String, EntryDigest>
                removed = new TreeMap<>(),
                added = new TreeMap<>(),
                unchanged = new TreeMap<>();
        final SortedMap<String, BeforeAndAfterEntryDigest>
                changed = new TreeMap<>();
        class JarVisitor implements Visitor<IOException> {
            @Override
            public void visitEntryInFile1(EntryInFile entryInFile1)
            throws IOException {
                final String name1 = entryInFile1.entry().getName();
                removed.put(name1, new EntryDigest(name1,
                        digestToHexString(digest, entryInFile1)));
            }

            @Override
            public void visitEntryInFile2(EntryInFile entryInFile2)
            throws IOException {
                final String name2 = entryInFile2.entry().getName();
                added.put(name2, new EntryDigest(name2,
                        digestToHexString(digest, entryInFile2)));
            }

            @Override
            public void visitEntriesInFiles(EntryInFile entryInFile1,
                                            EntryInFile entryInFile2)
            throws IOException {
                final String name1 = entryInFile1.entry().getName();
                assert name1.equals(entryInFile2.entry().getName());
                final String digest1 = digestToHexString(digest, entryInFile1);
                final String digest2 = digestToHexString(digest, entryInFile2);
                if (digest1.equals(digest2))
                    unchanged.put(name1, new EntryDigest(name1,
                            digest1));
                else
                    changed.put(name1, new BeforeAndAfterEntryDigest(name1,
                            digest1, digest2));
            }
        }
        new Engine(file1, file2).accept(new JarVisitor());
        final Diff diff = new Diff();
        diff.removed = nonEmptyOrNull(removed);
        diff.added = nonEmptyOrNull(added);
        diff.unchanged = nonEmptyOrNull(unchanged);
        diff.changed = nonEmptyOrNull(changed);
        return diff;
    }

    private static @Nullable <X> SortedMap<String, X>
    nonEmptyOrNull(SortedMap<String, X> map) {
        return map.isEmpty() ? null : map;
    }
}
