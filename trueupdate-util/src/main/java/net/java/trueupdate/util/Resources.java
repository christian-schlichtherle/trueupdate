/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.util;

import java.io.FileNotFoundException;
import java.net.URL;
import javax.annotation.concurrent.Immutable;

/**
 * Provides functions for dealing with resources on the class path.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class Resources {

    public static URL locate(final String name) throws FileNotFoundException {
        final URL url = Thread.currentThread().getContextClassLoader()
                .getResource(name);
        if (null == url)
            throw new FileNotFoundException(String.format(
                    "Cannot locate resource %s on the class path.", name));
        return url;
    }

    private Resources() { }
}
