/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.shed;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.concurrent.Immutable;

/**
 * Replaces references to system properties of the form {@code ${key}} with
 * their values.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public class SystemProperties {

    private static Pattern
            REFERENCE_PATTERN = Pattern.compile("\\$\\{([^\\}]*)\\}");

    private SystemProperties() { }

    /**
     * Replaces references to system properties of the form {@code ${key}} with
     * their values.
     *
     * @param string the string to process
     * @return the resulting string
     */
    public static String resolve(final String string) {
        final StringBuffer result = new StringBuffer(string.length());
        final Matcher matcher = REFERENCE_PATTERN.matcher(string);
        while (matcher.find())
            matcher.appendReplacement(result, replacement(matcher));
        return matcher.appendTail(result).toString();
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
