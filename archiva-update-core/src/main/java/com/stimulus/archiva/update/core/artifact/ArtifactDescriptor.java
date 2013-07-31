/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.core.artifact;

import java.io.Serializable;
import static java.util.Objects.requireNonNull;
import javax.annotation.concurrent.Immutable;

/**
 * An immutable description of an artifact by its Maven coordinates.
 *
 * @see    <a href="http://maven.apache.org/pom.html#Maven_Coordinates">Maven - POM Reference - Maven Coordinates</a>
 * @author Christian Schlichtherle
 */
@Immutable
public final class ArtifactDescriptor implements Serializable {

    private static final long serialVersionUID = 0L;

    private final String groupId, artifactId, version, classifier, extension;

    public ArtifactDescriptor() {
        this("", "", "", "", "jar");
    }

    private ArtifactDescriptor(
            final String groupId,
            final String artifactId,
            final String version,
            final String classifier,
            final String extension
    ) {
        this.groupId = requireNonNull(groupId);
        this.artifactId = requireNonNull(artifactId);
        this.version = requireNonNull(version);
        this.classifier = requireNonNull(classifier);
        this.extension = requireNonNull(extension);
    }

    /**
     * Returns the group id, e.g. {@code com.stimulus.archiva}.
     * The default value of this property is {@code ""}.
     */
    public String groupId() { return groupId; }

    /** Returns a new artifact descriptor with the given group id. */
    public ArtifactDescriptor groupId(String groupId) {
        return new ArtifactDescriptor(groupId, artifactId, version, classifier, extension);
    }

    /**
     * Returns the artifact id, e.g. {@code mailarchiva}.
     * The default value of this property is {@code ""}.
     */
    public String artifactId() { return artifactId; }

    /** Returns a new artifact descriptor with the given artifact id. */
    public ArtifactDescriptor artifactId(String artifactId) {
        return new ArtifactDescriptor(groupId, artifactId, version, classifier, extension);
    }

    /**
     * Returns the version, e.g. {@code 3.2.1}.
     * The default value of this property is {@code ""}.
     */
    public String version() { return version; }

    /** Returns a new artifact descriptor with the given version. */
    public ArtifactDescriptor version(String version) {
        return new ArtifactDescriptor(groupId, artifactId, version, classifier, extension);
    }

    /**
     * Returns the classifier.
     * The default value of this property is {@code ""}.
     */
    public String classifier() { return classifier; }

    /** Returns a new artifact descriptor with the given classifier. */
    public ArtifactDescriptor classifier(String classifier) {
        return new ArtifactDescriptor(groupId, artifactId, version, classifier, extension);
    }

    /**
     * Returns the extension, e.g. {@code war}.
     * In the Maven realm, this may also be referred to as the artifact
     * <i>type</i> or <i>extension</i>.
     * The default value of this property is {@code "jar"}.
     */
    public String extension() { return extension; }

    /** Returns a new artifact descriptor with the given extension. */
    public ArtifactDescriptor extension(String extension) {
        return new ArtifactDescriptor(groupId, artifactId, version, classifier, extension);
    }

    /**
     * Returns {@code true} if and only if the given object is an
     * {@link ArtifactDescriptor} with equal properties.
     */
    @Override public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ArtifactDescriptor)) return false;
        final ArtifactDescriptor that = (ArtifactDescriptor) obj;
        return  this.groupId().equals(that.groupId()) &&
                this.artifactId().equals(that.artifactId()) &&
                this.version().equals(that.version()) &&
                this.classifier().equals(that.classifier()) &&
                this.extension().equals(that.extension());
    }

    /**
     * Returns a hash code for this artifact descriptor which is consistent
     * with {@link #equals(Object)}.
     */
    @Override public int hashCode() {
        int hash = 17;
        hash = hash * 31 + groupId().hashCode();
        hash = hash * 31 + artifactId().hashCode();
        hash = hash * 31 + version().hashCode();
        hash = hash * 31 + classifier().hashCode();
        hash = hash * 31 + extension().hashCode();
        return hash;
    }

    /** Returns a human readable string representation of this descriptor. */
    @Override public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb      .append(groupId())
                .append(':').append(artifactId())
                .append(':').append(extension());
        if (0 < classifier().length())
            sb.append(':').append(classifier());
        sb.append(':').append(version());
        return sb.toString();
    }
}
