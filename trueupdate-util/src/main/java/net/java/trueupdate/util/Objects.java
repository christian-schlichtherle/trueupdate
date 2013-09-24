/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.util;

import javax.annotation.*;
import javax.annotation.concurrent.Immutable;

/**
 * Duplicates some features of {@code java.util.Objects} in JSE 7.
 * May be removed when migrating to JSE 7 one day.
 *
 * @author Christian Schlichtherle (copied from TrueLicense Core 2.3.1)
 */
@Immutable
public final class Objects {

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

    public static @Nullable <T> T nonDefaultOrNull(T obj, @CheckForNull T def) {
        return obj.equals(def) ? null : obj;
    }

    private Objects() { }
}
