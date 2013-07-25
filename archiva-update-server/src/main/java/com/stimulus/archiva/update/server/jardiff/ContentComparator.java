/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jardiff;

import javax.annotation.concurrent.Immutable;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Objects;
import java.util.jar.JarEntry;
import static com.stimulus.archiva.update.server.jardiff.Digests.*;

/**
 * Compares the contents of two JAR entries in two different JAR files.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public class ContentComparator implements Comparator {

    private final MessageDigest digest;

    /**
     * Constructs a content comparator which uses the given message digest.
     *
     * @param digest the message digest.
     */
    public ContentComparator(final MessageDigest digest) {
        this.digest = Objects.requireNonNull(digest);
    }

    /**
     * Returns {@code true} if and only if the contents of the two given JAR
     * entries in different JAR files should be considered to be equal.
     */
    public boolean equals(
            final EntryInFile entryInFile1,
            final EntryInFile entryInFile2)
    throws IOException {
        return fastPathCheck(entryInFile1.entry(), entryInFile2.entry()) &&
                slowPathCheck(entryInFile1, entryInFile2);
    }

    private static boolean fastPathCheck(JarEntry entry1, JarEntry entry2) {
        return entry1.getSize() == entry2.getSize() &&
                entry1.getCrc() == entry2.getCrc();
    }

    private boolean slowPathCheck(
            final EntryInFile entryInFile1,
            final EntryInFile entryInFile2)
    throws IOException {
        // CRC-32 has frequent collisions, so let's consider a
        // cryptographically strong message digest.
        return Arrays.equals(
                digest(digest, entryInFile1),
                digest(digest, entryInFile2));
    }
}
