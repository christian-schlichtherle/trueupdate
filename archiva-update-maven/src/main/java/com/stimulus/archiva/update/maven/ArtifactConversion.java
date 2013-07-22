/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.maven;

import com.stimulus.archiva.update.commons.ArtifactDescriptor;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

/**
 * Converts {@link ArtifactDescriptor}s to {@link Artifact}s and vice versa.
 *
 * @author Christian Schlichtherle
 */
final class ArtifactConversion {

    private ArtifactConversion() { }

    /**
     * Converts an artifact descriptor to a ranged artifact which covers all
     * update versions.
     *
     * @param descriptor the artifact descriptor to convert.
     * @return the artifact.
     */
    static Artifact updateRangeArtifact(ArtifactDescriptor descriptor) {
        return artifact(descriptor).setVersion(updateRangeVersion(descriptor));
    }

    private static String updateRangeVersion(ArtifactDescriptor descriptor) {
        return String.format("[%s,)", descriptor.version());
    }

    /**
     * Converts an artifact descriptor to an artifact.
     *
     * @param descriptor the artifact descriptor to convert.
     * @return the artifact.
     */
    static Artifact artifact(ArtifactDescriptor descriptor) {
        return new DefaultArtifact(
                descriptor.groupId(),
                descriptor.artifactId(),
                descriptor.classifier(),
                descriptor.extension(),
                descriptor.version());
    }

    /**
     * Converts an artifact to an artifact descriptor.
     *
     * @param artifact the artifact to convert.
     * @return the artifact descriptor.
     */
    static ArtifactDescriptor descriptor(Artifact artifact) {
        return new ArtifactDescriptor.Builder()
                .groupId(artifact.getGroupId())
                .artifactId(artifact.getArtifactId())
                .version(artifact.getVersion())
                .classifier(artifact.getClassifier())
                .extension(artifact.getExtension())
                .build();
    }
}
