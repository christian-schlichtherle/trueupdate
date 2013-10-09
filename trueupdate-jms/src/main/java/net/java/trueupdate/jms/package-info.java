/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
/**
 * Provides services to transmit and receive update messages via JMS.
 *
 * @author Christian Schlichtherle
 */
/*@javax.xml.bind.annotation.XmlSchema(
        // Note that on Java SE 7, these namespace prefixes get substituted
        // with the default namespace prefixes "xs" and "xsi" in the namespace
        // declaration on the root element unless a NameSpaceMapper is attached
        // to the Marshaller, too - see class JAXB.
        xmlns = {
            @javax.xml.bind.annotation.XmlNs(prefix = "x", namespaceURI = "http://www.w3.org/2001/XMLSchema"),
            @javax.xml.bind.annotation.XmlNs(prefix = "i", namespaceURI = "http://www.w3.org/2001/XMLSchema-instance"),
        }
)*/
@javax.annotation.ParametersAreNonnullByDefault @javax.annotation.Nonnull
package net.java.trueupdate.jms;
