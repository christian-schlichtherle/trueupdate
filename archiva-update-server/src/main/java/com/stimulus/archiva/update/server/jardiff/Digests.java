/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jardiff;

import com.stimulus.archiva.update.core.io.*;
import javax.annotation.concurrent.Immutable;
import java.io.*;
import java.math.BigInteger;
import java.security.*;

/**
 * Provides digest functions.
 *
 * @author Christian Schlichtherle
 */
@Immutable
final class Digests {

    private Digests() { }

    /** Returns a new SHA-1 digest. */
    static MessageDigest sha1() { return digest("SHA-1"); }

    private static MessageDigest digest(final String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException ex) {
            throw new AssertionError(
                    "The JRE doesn't implement the standard message digest " + algorithm + ". See http://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#MessageDigest .",
                    ex);
        }
    }

    /** Computes a digest of the data in the given source. */
    static byte[] digest(final MessageDigest digest, final Source source)
    throws IOException {
        digest.reset();
        final byte[] buffer = new byte[Store.BUFSIZE];
        try (InputStream in = source.input()) {
            int read;
            do {
                read = in.read(buffer);
                if (0 < read) digest.update(buffer, 0, read);
            } while (0 <= read);
        }
        return digest.digest();
    }

    /**
     * Converts the given binary data to a hex string.
     * The binary data is parsed as a big integer with positive signum in
     * big-endian byte-order and then converted into a string representation
     * with radix 16.
     */
    static String hexString(byte[] data) {
        return new BigInteger(1, data).toString(16);
    }
}
