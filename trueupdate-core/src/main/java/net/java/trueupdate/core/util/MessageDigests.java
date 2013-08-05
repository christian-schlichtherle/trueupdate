/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.util;

import net.java.trueupdate.core.io.Source;
import net.java.trueupdate.core.io.Store;

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import javax.annotation.concurrent.Immutable;

/**
 * Provides message digest functions.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class MessageDigests {

    private MessageDigests() { }

    /** Returns a new SHA-1 message digest. */
    public static MessageDigest sha1() { return newDigest("SHA-1"); }

    /**
     * Returns a new message digest with the given algorithm name.
     *
     * @param algorithm the algorithm name.
     * @throws IllegalArgumentException if no implementation of the algorithm
     *         is found.
     */
    public static MessageDigest newDigest(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalArgumentException(ex);
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

    /**
     * Transforms the given byte array to a positive, big-endian integer in
     * hexadecimal string notation.
     * This is the canonical representation of message digests.
     *
     * @param digest an array of bytes representing the message digest.
     * @return a positive, big-endian integer in hexadecimal string notation
     *         representing the message digest.
     */
    public static String hexString(byte[] digest) {
        return new BigInteger(1, digest).toString(16);
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
