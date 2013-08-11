/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.artifact.spec;

import java.io.Serializable;
import static java.util.Objects.requireNonNull;
import javax.annotation.concurrent.Immutable;

/**
 * A Value Object which describes an artifact by its Maven coordinates.
 *
 * @see    <a href="http://maven.apache.org/pom.html#Maven_Coordinates">Maven - POM Reference - Maven Coordinates</a>
 * @author Christian Schlichtherle
 */
@Immutable
public final class ArtifactDescriptor implements Serializable {

    private static final long serialVersionUID = 0L;

    private final String groupId, artifactId, version, classifier, extension;

    ArtifactDescriptor(final Builder b) {
        this.groupId = requireNonEmpty(b.groupId);
        this.artifactId = requireNonEmpty(b.artifactId);
        this.version = requireNonEmpty(b.version);
        this.classifier = requireNonNull(b.classifier);
        this.extension = requireNonEmpty(b.extension);
    }

    static String requireNonEmpty(final String string) {
        if (string.isEmpty()) throw new IllegalArgumentException();
        return string;
    }

    /**
     * Returns the group id, e.g. {@code net.java.trueupdate}.
     * The default value of this property is {@code ""}.
     */
    public String groupId() { return groupId; }

    /** Returns an artifact descriptor with the given group id. */
    public ArtifactDescriptor groupId(String groupId) {
        return this.groupId.equals(groupId)
                ? this
                : new Builder()
                    .groupId(groupId)
                    .artifactId(artifactId)
                    .version(version)
                    .classifier(classifier)
                    .extension(extension)
                    .build();
    }

    /**
     * Returns the artifact id, e.g. {@code trueupdate-core}.
     * The default value of this property is {@code ""}.
     */
    public String artifactId() { return artifactId; }

    /** Returns an artifact descriptor with the given artifact id. */
    public ArtifactDescriptor artifactId(String artifactId) {
        return this.artifactId.equals(artifactId)
                ? this
                : new Builder()
                    .groupId(groupId)
                    .artifactId(artifactId)
                    .version(version)
                    .classifier(classifier)
                    .extension(extension)
                    .build();
    }

    /**
     * Returns the version, e.g. {@code 3.2.1}.
     * The default value of this property is {@code ""}.
     */
    public String version() { return version; }

    /** Returns an artifact descriptor with the given version. */
    public ArtifactDescriptor version(String version) {
        return this.version.equals(version)
                ? this
                : new Builder()
                    .groupId(groupId)
                    .artifactId(artifactId)
                    .version(version)
                    .classifier(classifier)
                    .extension(extension)
                    .build();
    }

    /**
     * Returns the classifier.
     * The default value of this property is {@code ""}.
     */
    public String classifier() { return classifier; }

    /** Returns an artifact descriptor with the given classifier. */
    public ArtifactDescriptor classifier(String classifier) {
        return this.classifier.equals(classifier)
                ? this
                : new Builder()
                    .groupId(groupId)
                    .artifactId(artifactId)
                    .version(version)
                    .classifier(classifier)
                    .extension(extension)
                    .build();
    }

    /**
     * Returns the extension, e.g. {@code war}.
     * In the Maven realm, this may also be referred to as the artifact
     * <i>type</i> or <i>extension</i>.
     * The default value of this property is {@code "jar"}.
     */
    public String extension() { return extension; }

    /** Returns an artifact descriptor with the given extension. */
    public ArtifactDescriptor extension(String extension) {
        return this.extension.equals(extension)
                ? this
                : new Builder()
                    .groupId(groupId)
                    .artifactId(artifactId)
                    .version(version)
                    .classifier(classifier)
                    .extension(extension)
                    .build();
    }

    /**
     * Returns {@code true} if and only if the given object is an
     * {@code ArtifactDescriptor} with equal properties.
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

    /** Returns a hash code which is consistent with {@link #equals(Object)}. */
    @Override public int hashCode() {
        int hash = 17;
        hash = 31 * hash + groupId().hashCode();
        hash = 31 * hash + artifactId().hashCode();
        hash = 31 * hash + version().hashCode();
        hash = 31 * hash + classifier().hashCode();
        hash = 31 * hash + extension().hashCode();
        return hash;
    }

    /** Returns a human readable string representation of this object. */
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

    /** Returns a new builder for an artifact descriptor. */
    public static Builder builder() { return new Builder(); }

    /**
     * A builder for an artifact descriptor.
     * The default value for the property {@code classifier} is an empty string
     * and the default value for the property {@code extension} is
     * {@code "jar"}.
     */
    @SuppressWarnings("PackageVisibleField")
    public static final class Builder {

        String groupId, artifactId, version, classifier = "", extension = "jar";

        Builder() { }

        public Builder groupId(final String groupId) {
            this.groupId = requireNonEmpty(groupId);
            return this;
        }

        public Builder artifactId(final String artifactId) {
            this.artifactId = requireNonEmpty(artifactId);
            return this;
        }

        public Builder version(final String version) {
            this.version = requireNonEmpty(version);
            return this;
        }

        public Builder classifier(final String classifier) {
            this.classifier = requireNonNull(classifier);
            return this;
        }

        public Builder extension(final String extension) {
            this.extension = requireNonEmpty(extension);
            return this;
        }

        public ArtifactDescriptor build() {
            return new ArtifactDescriptor(this);
        }
    } // Builder
}
