/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms;

import javax.xml.bind.annotation.*;

/**
 * Represents an update message.
 *
 * @author Christian Schlichtherle
 */
@SuppressWarnings({ "PublicField", "PackageVisibleField" })
@XmlRootElement(name = "um")
@XmlAccessorType(XmlAccessType.FIELD)
final class CompactUpdateMessageDto {

    /** timestamp */
    long ts;

    /** from */
    String fr;

    /** to */
    String to;

    /** type */
    String ty;

    /** artifactDescriptor */
    CompactArtifactDescriptorDto ad;

    /** updateVersion */
    String uv;

    /** currentLocation */
    String cl;

    /** updateLocation */
    String ul;

    /** status */
    String st;
}
