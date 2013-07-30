/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jar.model;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * Provides utilities for JAR {@link Diff} beans.
 *
 * @author Christian Schlichtherle
 */
public final class Diffs {

    private Diffs() { }

    /**
     * The name of the entry which contains the serialized JAR diff bean in a
     * JAR diff file.
     * This should be the first entry in the JAR diff file.
     */
    public static final String DIFF_ENTRY_NAME = "META-INF/diff.xml";

    /** Returns a JAXB context which binds only the {@link Diff} class. */
    public static JAXBContext jaxbContext() { return Lazy.JAXB_CONTEXT; }

    private static class Lazy {

        static final JAXBContext JAXB_CONTEXT;

        static {
            try { JAXB_CONTEXT = JAXBContext.newInstance(Diff.class); }
            catch (JAXBException ex) { throw new AssertionError(ex); }
        }
    }
}
