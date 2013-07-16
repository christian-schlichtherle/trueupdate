/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.finder;

/**
 * A basic artifact descriptor.
 *
 * @author Christian Schlichtherle
 */
public abstract class BasicArtifactDescriptor implements ArtifactDescriptor {

    @Override public final boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ArtifactDescriptor)) return false;
        final ArtifactDescriptor that = (ArtifactDescriptor) obj;
        return  this.groupId().equals(that.groupId()) &&
                this.artifactId().equals(that.artifactId()) &&
                this.version().equals(that.version()) &&
                this.packaging().equals(that.packaging()) &&
                this.classifier().equals(that.classifier());
    }

    @Override public final int hashCode() {
        int hash = 17;
        hash = hash * 31 + groupId().hashCode();
        hash = hash * 31 + artifactId().hashCode();
        hash = hash * 31 + version().hashCode();
        hash = hash * 31 + packaging().hashCode();
        hash = hash * 31 + classifier().hashCode();
        return hash;
    }

    @Override public final String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb      .append(groupId())
                .append(':').append(artifactId())
                .append(':').append(packaging());
        if (0 < classifier().length())
            sb.append(':').append(classifier());
        sb.append(':').append(version());
        return sb.toString();
    }
}
