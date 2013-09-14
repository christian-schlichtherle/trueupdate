/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.util;

import javax.annotation.concurrent.Immutable;

/**
 * Provides string functions.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public class Strings {

    public static String requireNonEmpty(final String string) {
        if (string.isEmpty()) throw new IllegalArgumentException();
        return string;
    }

    public static String nonEmptyOr(String string, String def) {
        return null != string && !string.isEmpty() ? string : def;
    }

    private Strings() { }
}
