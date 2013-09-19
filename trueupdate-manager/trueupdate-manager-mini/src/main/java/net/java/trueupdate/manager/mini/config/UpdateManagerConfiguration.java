/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.mini.config;

import javax.xml.bind.*;
import javax.xml.bind.annotation.XmlRootElement;
import net.java.trueupdate.core.codec.JaxbCodec;
import net.java.trueupdate.core.io.Source;
import net.java.trueupdate.jms.config.*;

/**
 * Configures an update manager.
 *
 * @author Christian Schlichtherle
 */
@XmlRootElement(name = "manager")
@SuppressWarnings("PublicField")
public class UpdateManagerConfiguration {

    public String updateServiceBaseUri, checkUpdatesIntervalMinutes;
    public NamingConfiguration naming;
    public MessagingConfiguration messaging;

    /**
     * Decodes an update manager configuation from XML.
     *
     * @param source the source for reading the XML.
     * @return the decoded update manager configuration.
     * @throws Exception at the discretion of the JAXB codec, e.g. if the
     *         source isn't readable.
     */
    public static UpdateManagerConfiguration decodeFromXml(Source source)
    throws Exception {
        return new JaxbCodec(jaxbContext())
                .decode(source, UpdateManagerConfiguration.class);
    }

    /** Returns a JAXB context which binds only this class. */
    public static JAXBContext jaxbContext() { return Lazy.JAXB_CONTEXT; }

    private static class Lazy {

        static final JAXBContext JAXB_CONTEXT;

        static {
            try {
                JAXB_CONTEXT = JAXBContext
                        .newInstance(UpdateManagerConfiguration.class);
            } catch (JAXBException ex) {
                throw new AssertionError(ex);
            }
        }
    } // Lazy
}
