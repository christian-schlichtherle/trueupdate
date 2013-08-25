/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.codec;

import javax.xml.bind.*;

/**
 * @author Christian Schlichtherle
 */
public class TestJaxbCodec extends JaxbCodec {

    public TestJaxbCodec(JAXBContext context) { super(context); }

    @Override protected Marshaller marshaller() throws JAXBException {
        final Marshaller marshaller = super.marshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        return marshaller;
    }
}
