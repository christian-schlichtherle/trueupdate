/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.artifact.spec.ci;

/**
 * Configures an artifact.
 *
 * @author Christian Schlichtherle
 */
@SuppressWarnings("PublicField")
public class ArtifactDescriptorDto {
    public String groupId, artifactId, version, classifier, extension;
}
