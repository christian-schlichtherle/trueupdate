/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.util;

import java.util.regex.*;
import javax.annotation.*;
import javax.annotation.concurrent.Immutable;

/**
 * Replaces references to system properties of the form {@code ${key}} with
 * their values.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class SystemProperties {

    private static Pattern
            REFERENCE_PATTERN = Pattern.compile("\\$\\{([^\\}]*)\\}");

    private SystemProperties() { }

    /**
     * Replaces references to system properties of the form {@code ${key}} with
     * their values.
     *
     * @param string the nullable string to process.
     * @param defaultValue the nullable default value to return if and only if
     *        {@code string} is {@code null}.
     *        Note that this parameter does not get processed.
     * @return the resulting string
     */
    public static @Nullable String resolve(@CheckForNull String string,
                                           @Nullable String defaultValue) {
        return null == string ? defaultValue : resolve(string);
    }

    /**
     * Replaces references to system properties of the form {@code ${key}} with
     * their values.
     *
     * @param string the string to process.
     * @return the resulting string
     */
    public static String resolve(final String string) {
        final StringBuffer sb = new StringBuffer(string.length());
        final Matcher matcher = REFERENCE_PATTERN.matcher(string);
        boolean found = false;
        while (matcher.find()) {
            found = true;
            matcher.appendReplacement(sb, replacement(matcher));
        }
        return found ? matcher.appendTail(sb).toString() : string;
    }

    private static String replacement(final Matcher matcher) {
        final String key = matcher.group(1);
        final String value = System.getProperty(key);
        if (null == value)
            throw new IllegalArgumentException(
                    "Unknown system property key \"" + key + "\".");
        return value;
    }
}
