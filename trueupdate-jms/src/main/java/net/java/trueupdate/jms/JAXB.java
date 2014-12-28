/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms;

import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import net.java.trueupdate.message.UpdateMessage;

/**
 * Provides functions for JAXB.
 *
 * @author Christian Schlichtherle
 */
final class JAXB {

    private static final JAXBContext CONTEXT;
    static {
        try {
            CONTEXT = JAXBContext.newInstance(UpdateMessageDto.class);
        } catch (JAXBException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch"})
    static String encode(final UpdateMessage message) throws Exception {
        final StringWriter sw = new StringWriter(1024);
        marshaller().marshal(adapter().marshal(message), sw);
        return sw.toString();
    }

    static UpdateMessage decode(String string) throws Exception {
        return adapter().unmarshal(
                (UpdateMessageDto) unmarshaller().unmarshal(
                    new StringReader(string)));
    }

    private static Marshaller marshaller() throws JAXBException {
        return CONTEXT.createMarshaller();
    }

    private static Unmarshaller unmarshaller() throws JAXBException {
        return CONTEXT.createUnmarshaller();
    }

    private static UpdateMessageAdapter adapter() {
        return new UpdateMessageAdapter();
    }

    private JAXB() { }
}
