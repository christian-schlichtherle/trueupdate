/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.core.zip.model;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * Provides utilities for ZIP {@link Diff} beans.
 *
 * @author Christian Schlichtherle
 */
public final class Diffs {

    private Diffs() { }

    /**
     * The name of the entry which contains the serialized ZIP diff bean in a
     * ZIP diff file.
     * This should be the first entry in the ZIP diff file.
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