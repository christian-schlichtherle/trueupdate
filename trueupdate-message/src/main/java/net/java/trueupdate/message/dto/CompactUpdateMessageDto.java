/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.message.dto;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents an update message.
 *
 * @author Christian Schlichtherle
 */
@SuppressWarnings("PublicField")
@XmlRootElement(name = "um")
public final class CompactUpdateMessageDto {

    /** timestamp */
    public long ts;

    /** from */
    public String fr;

    /** to */
    public String to;

    /** type */
    public String ty;

    /** artifactDescriptor */
    public CompactArtifactDescriptorDto ad;

    /** updateVersion */
    public String uv;

    /** currentLocation */
    public String cl;

    /** updateLocation */
    public String ul;

    /** status */
    public String st;
}
