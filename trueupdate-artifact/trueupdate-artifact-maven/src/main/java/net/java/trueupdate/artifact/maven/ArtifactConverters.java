/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.artifact.maven;

import net.java.trueupdate.artifact.spec.ArtifactDescriptor;
import org.eclipse.aether.artifact.*;

/**
 * Converts {@link ArtifactDescriptor}s to {@link Artifact}s and vice versa.
 *
 * @author Christian Schlichtherle
 */
final class ArtifactConverters {

    private ArtifactConverters() { }

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
                descriptor.packaging(),
                descriptor.version());
    }

    /**
     * Converts an artifact to an artifact descriptor.
     *
     * @param artifact the artifact to convert.
     * @return the artifact descriptor.
     */
    static ArtifactDescriptor descriptor(Artifact artifact) {
        return ArtifactDescriptor.builder()
                .groupId(artifact.getGroupId())
                .artifactId(artifact.getArtifactId())
                .version(artifact.getVersion())
                .classifier(artifact.getClassifier())
                .packaging(artifact.getExtension())
                .build();
    }
}
