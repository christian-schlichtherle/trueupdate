/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms;

import java.io.*;
import javax.xml.bind.*;
import net.java.trueupdate.message.*;
import net.java.trueupdate.message.dto.CompactUpdateMessageDto;

/**
 * Provides functions for JAXB.
 *
 * @author Christian Schlichtherle
 */
final class JAXB {

    private static final JAXBContext CONTEXT;
    static {
        try {
            CONTEXT = JAXBContext.newInstance(CompactUpdateMessageDto.class);
        } catch (JAXBException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static final UpdateMessageAdapter ADAPTER =
            new UpdateMessageAdapter();

    static String encode(final UpdateMessage message) throws Exception {
        final StringWriter sw = new StringWriter(256);
        CONTEXT.createMarshaller().marshal(ADAPTER.marshal(message), sw);
        return sw.toString();
    }

    static UpdateMessage decode(String string) throws Exception {
        return (UpdateMessage) ADAPTER.unmarshal(
                (CompactUpdateMessageDto) CONTEXT.createUnmarshaller()
                    .unmarshal(new StringReader(string)));
    }

    private JAXB() { }
}
