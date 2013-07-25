/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.util;

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
public final class MessageDigests {

    private MessageDigests() { }

    /** Returns a new SHA-1 digest. */
    public static MessageDigest sha1() { return newDigest("SHA-1"); }

    private static MessageDigest newDigest(final String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException ex) {
            throw new AssertionError(
                    "The JRE doesn't implement the standard message digest " + algorithm + ". See http://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#MessageDigest .",
                    ex);
        }
    }

    /**
     * Computes a message digest of the binary data in the given source, parses
     * the resulting byte array as a big-endian big integer with positive
     * signum and converts it to a string using radix 16.
     * This is the canonical representation of message digests.
     *
     * @param digest the message digest to use.
     * @param source the source for reading the binary data.
     * @return A string representing the message digest as an unsigned
     *         hexadecimal big integer.
     */
    public static String digestToHexString(MessageDigest digest, Source source)
    throws IOException {
        return hexString(digestToByteArray(digest, source));
    }

    private static String hexString(byte[] data) {
        return new BigInteger(1, data).toString(16);
    }

    /**
     * Computes a message digest of the binary data in the given source and
     * returns a byte array with the result.
     *
     * @param digest the message digest to use.
     * @param source the source for reading the binary data.
     * @return A byte array representing the message digest.
     */
    public static byte[] digestToByteArray(
            final MessageDigest digest,
            final Source source)
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
}
