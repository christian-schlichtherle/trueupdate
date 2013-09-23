/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms;

import javax.xml.bind.annotation.*;

/**
 * Represents an artifact.
 *
 * @author Christian Schlichtherle
 */
@SuppressWarnings({ "PublicField", "PackageVisibleField" })
@XmlAccessorType(XmlAccessType.FIELD)
final class CompactArtifactDescriptorDto {

    /** groupId */
    String g;

    /** artifactId */
    String a;

    /** version */
    String v;

    /** classifier */
    String c;

    /** extension */
    String e;
}
