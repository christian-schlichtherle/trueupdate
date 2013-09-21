/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.message.dto;

/**
 * Represents an artifact.
 *
 * @author Christian Schlichtherle
 */
@SuppressWarnings("PublicField")
public final class CompactArtifactDescriptorDto {

    /** groupId */
    public String g;

    /** artifactId */
    public String a;

    /** version */
    public String v;

    /** classifier */
    public String c;

    /** extension */
    public String e;
}
