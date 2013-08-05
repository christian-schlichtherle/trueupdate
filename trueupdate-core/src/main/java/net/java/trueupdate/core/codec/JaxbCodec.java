/*
 * Copyright (C) 2005-2013 Schlichtherle IT Services.
 * Copyright (C) 2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.codec;

import net.java.trueupdate.core.io.Sink;
import net.java.trueupdate.core.io.Source;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Objects;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.*;

/**
 * A codec which encodes/decodes objects to/from XML with a
 * {@link Marshaller}/{@link Unmarshaller} derived from a {@link JAXBContext}.
 * <p>
 * This type of codec does <em>not</em> support encoding or decoding
 * {@code null}.
 *
 * @author Christian Schlichtherle (copied and edited from TrueLicense Core 2.3.1)
 */
@Immutable
public class JaxbCodec implements Codec {

    /** The JAXB context provided to the constructor. */
    protected final JAXBContext context;

    public JaxbCodec(final JAXBContext context) {
        this.context = Objects.requireNonNull(context);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The implementation in the class {@link JaxbCodec}
     * returns {@code "application/xml; charset=utf-8"}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc3023">RFC 3023</a>
     */
    @Override public String contentType() {
        return "application/xml; charset=utf-8";
    }

    /**
     * {@inheritDoc}
     * <p>
     * The implementation in the class {@link JaxbCodec}
     * returns {@code "8bit"}.
     *
     * @see <a href="http://tools.ietf.org/html/rfc3023">RFC 3023</a>
     */
    @Override public String contentTransferEncoding() { return "8bit"; }

    @Override public void encode(final Sink sink, final Object obj)
    throws Exception {
        try (OutputStream out = sink.output()) {
            marshaller().marshal(obj, out);
        }
    }

    /** Returns a new marshaller. */
    protected Marshaller marshaller() throws JAXBException {
        return context.createMarshaller();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T decode(final Source source, final Type expected)
    throws Exception {
        try (InputStream in = source.input()) {
            return (T) unmarshaller().unmarshal(in);
        }
    }

    /** Returns a new unmarshaller. */
    protected Unmarshaller unmarshaller() throws JAXBException {
        return context.createUnmarshaller();
    }
}
