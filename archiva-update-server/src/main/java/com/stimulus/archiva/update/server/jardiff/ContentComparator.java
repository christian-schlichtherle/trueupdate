/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jardiff;

import com.stimulus.archiva.update.core.io.Source;

import javax.annotation.concurrent.Immutable;
import java.io.*;
import java.security.*;
import java.util.Arrays;
import java.util.jar.JarEntry;

/**
 * Compares the contents of two JAR entries in two different JAR files.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public class ContentComparator implements Comparator {

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

    private static boolean slowPathCheck(
            final EntryInFile entryInFile1,
            final EntryInFile entryInFile2)
    throws IOException {
        // CRC-32 has frequent collisions, so let's consider a
        // cryptographically strong message digest.
        final MessageDigest digest = sha1();
        return Arrays.equals(digest(digest, entryInFile1), digest(digest, entryInFile2));
    }

    private static MessageDigest sha1() { return digest("SHA-1"); }

    private static MessageDigest digest(final String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException ex) {
            throw new AssertionError(
                    "JRE doesn't implement standard message digest SHA-1. See http://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#MessageDigest .",
                    ex);
        }
    }

    private static byte[] digest(
            final MessageDigest digest,
            final Source source)
    throws IOException {
        digest.reset();
        final byte[] buffer = new byte[8 * 1024];
        try (InputStream in = source.input()) {
            int read;
            do {
                read = in.read(buffer);
                if (0 < read) digest.update(buffer, 0, read);
            } while (0 <= read);
        }
        return digest.digest();
    }
}
