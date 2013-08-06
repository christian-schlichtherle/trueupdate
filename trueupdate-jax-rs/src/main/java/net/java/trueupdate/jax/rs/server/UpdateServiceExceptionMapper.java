/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jax.rs.server;

import net.java.trueupdate.jax.rs.UpdateServiceException;

import java.util.Objects;
import javax.annotation.concurrent.Immutable;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import static javax.ws.rs.core.MediaType.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

/**
 * Maps an update service exception to a HTTP response.
 *
 * @author Christian Schlichtherle
 */
@Provider
@Produces({ APPLICATION_JSON, APPLICATION_XML, TEXT_XML, TEXT_PLAIN })
@Immutable
public final class UpdateServiceExceptionMapper
implements ExceptionMapper<UpdateServiceException> {

    private static final String MESSAGE = "message";

    private static final QName message = new QName(MESSAGE);

    private final HttpHeaders headers;

    public UpdateServiceExceptionMapper(final @Context HttpHeaders headers) {
        this.headers = Objects.requireNonNull(headers);
    }

    @Override public Response toResponse(final UpdateServiceException ex) {
        final String msg = ex.getMessage();
        final MediaType mt = headers.getMediaType();
        final ResponseBuilder rb = Response.status(ex.getStatus());
        if (APPLICATION_JSON_TYPE.equals(mt))
            rb.type(APPLICATION_JSON_TYPE)
              .entity('"' + msg + '"');
        else if (APPLICATION_XML_TYPE.equals(mt))
            rb.type(APPLICATION_XML_TYPE)
              .entity(new JAXBElement<>(message, String.class, msg));
        else if (TEXT_XML_TYPE.equals(mt))
            rb.type(TEXT_XML_TYPE)
              .entity(new JAXBElement<>(message, String.class, msg));
        else
            rb.type(TEXT_PLAIN_TYPE)
              .entity(msg);
        return rb.build();
    }
}
