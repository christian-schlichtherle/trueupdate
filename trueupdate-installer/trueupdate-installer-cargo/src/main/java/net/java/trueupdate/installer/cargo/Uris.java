/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.cargo;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Provides functions for {@link URI}s.
 *
 * @author Christian Schlichtherle
 */
final class Uris {

    /**
     * Returns the decoded query parameters of the given URI as a multi-valued
     * map.
     * Every list value has at least one element, which may be an empty string.
     */
    static Map<String, List<String>> queryParameters(final URI uri) {
        final Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();
        final String rawQuery = uri.getRawQuery();
        if (null != rawQuery) {
            for (final String parameter : rawQuery.split("&")) {
                final int i = parameter.indexOf('=');
                final String name;
                final String value;
                if (0 <= i) {
                    name = decode(parameter.substring(0, i));
                    value = decode(parameter.substring(i + 1));
                } else {
                    name = decode(parameter);
                    value = "";
                }
                List<String> list = map.get(name);
                if (null == list)
                    map.put(name, list = new LinkedList<String>());
                list.add(value);
            }
        }
        return map;
    }

    private static String decode(final String queryParameter) {
        try {
            return new URI("?" + queryParameter).getQuery();
        } catch (URISyntaxException ex) {
            throw new AssertionError(ex);
        }
    }
}
