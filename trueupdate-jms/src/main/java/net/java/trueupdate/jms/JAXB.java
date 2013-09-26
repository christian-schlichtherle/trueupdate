/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms;

import java.io.*;
import javax.xml.bind.*;
import net.java.trueupdate.message.*;

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

    static String encode(final UpdateMessage message) throws Exception {
        final StringWriter sw = new StringWriter(1024);
        final Marshaller m = marshaller();
        //m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        try {
            if (!message.attachedLogs().isEmpty())
                m.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper",
                              new CompactNamespaceMapper());
        } catch(PropertyException aDifferentJaxbImplementationIsUsed) {
        }
        m.marshal(adapter().marshal(message), sw);
        return sw.toString();
    }

    static UpdateMessage decode(String string) throws Exception {
        return (UpdateMessage) adapter().unmarshal(
                (CompactUpdateMessageDto) unmarshaller().unmarshal(
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
