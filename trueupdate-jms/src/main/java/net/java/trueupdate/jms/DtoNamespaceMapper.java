/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms;

import com.sun.xml.internal.bind.marshaller.NamespacePrefixMapper;
import static com.sun.xml.internal.bind.v2.WellKnownNamespace.*;

/**
 * Adds the XML Schema and XML Schema Instance namespaces to the root element.
 *
 * @author Christian Schlichtherle
 */
final class DtoNamespaceMapper extends NamespacePrefixMapper {

    @Override public String getPreferredPrefix(
            final String namespaceUri,
            final String suggestion,
            final boolean requirePrefix) {
        if (XML_SCHEMA.equals(namespaceUri)) return "x";
        else if(XML_SCHEMA_INSTANCE.equals(namespaceUri)) return "i";
        else return suggestion;
    }

    @Override public String[] getPreDeclaredNamespaceUris() {
        return new String[] { XML_SCHEMA, XML_SCHEMA_INSTANCE };
    }
}
