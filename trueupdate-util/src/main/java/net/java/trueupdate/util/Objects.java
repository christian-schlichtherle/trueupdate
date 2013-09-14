/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.util;

import javax.annotation.CheckForNull;
import javax.annotation.concurrent.Immutable;

/**
 * Duplicates some features of {@code java.util.Objects} in JSE 7.
 * May be removed when migrating to JSE 7 one day.
 *
 * @author Christian Schlichtherle (copied from TrueLicense Core 2.3.1)
 */
@Immutable
public class Objects {

    public static boolean equals(@CheckForNull Object a, @CheckForNull Object b) {
        return a == b || null != a && a.equals(b);
    }

    public static int hashCode(@CheckForNull Object o) {
        return null == o ? 0 : o.hashCode();
    }

    public static <T> T requireNonNull(final @CheckForNull T obj) {
        if (null == obj) throw new NullPointerException();
        return obj;
    }

    public static <T> T nonNullOr(@CheckForNull T obj, T def) {
        return null != obj ? obj : def;
    }

    private Objects() { }
}
