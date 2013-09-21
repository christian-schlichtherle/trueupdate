/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.artifact.spec;

import java.io.Serializable;
import javax.annotation.*;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.artifact.spec.dto.ArtifactDescriptorDto;
import static net.java.trueupdate.util.Objects.*;
import static net.java.trueupdate.util.Strings.*;
import static net.java.trueupdate.util.SystemProperties.resolve;

/**
 * An artifact descriptor comprises of a group ID, an artifact ID, a version,
 * an optional classifier and an optional extension.
 * The model is the same as used in Maven in order to enable a Maven based
 * implementation of an {@linkplain ArtifactResolver artifact resolver} without
 * actually depending on Maven in this specification.
 * This class implements an immutable value object, so you can easily share it
 * with anyone.
 *
 * @see    <a href="http://maven.apache.org/pom.html#Maven_Coordinates">Maven - POM Reference - Maven Coordinates</a>
 * @author Christian Schlichtherle
 */
@Immutable
public final class ArtifactDescriptor implements Serializable {

    private static final long serialVersionUID = 0L;

    private final String groupId, artifactId, version, classifier, extension;

    ArtifactDescriptor(final Builder<?> b) {
        this.groupId = requireNonEmpty(b.groupId);
        this.artifactId = requireNonEmpty(b.artifactId);
        this.version = requireNonEmpty(b.version);
        this.classifier = nonNullOr(b.classifier, "");
        this.extension = nonNullOr(b.extension, "jar");
    }

    /** Returns a new builder with all properties set from this instance. */
    public Builder<Void> update() {
        return builder()
                .groupId(groupId())
                .artifactId(artifactId())
                .version(version())
                .classifier(classifier())
                .extension(extension());
    }

    /** Parses the given configuration item. */
    public static ArtifactDescriptor parse(ArtifactDescriptorDto ci) {
        return builder().parse(ci).build();
    }

    /** Returns a new builder for an artifact descriptor. */
    public static Builder<Void> builder() { return new Builder<Void>(); }

    /** Returns the group id, e.g. {@code net.java.trueupdate}. */
    public String groupId() { return groupId; }

    /** Returns an artifact descriptor with the given group id. */
    public ArtifactDescriptor groupId(String groupId) {
        return groupId().equals(groupId)
                ? this
                : update().groupId(groupId).build();
    }

    /** Returns the artifact id, e.g. {@code trueupdate-core}. */
    public String artifactId() { return artifactId; }

    /** Returns an artifact descriptor with the given artifact id. */
    public ArtifactDescriptor artifactId(String artifactId) {
        return artifactId().equals(artifactId)
                ? this
                : update().artifactId(artifactId).build();
    }

    /** Returns the version, e.g. {@code 3.2.1}. */
    public String version() { return version; }

    /** Returns an artifact descriptor with the given version. */
    public ArtifactDescriptor version(String version) {
        return version().equals(version)
                ? this
                : update().version(version).build();
    }

    /**
     * Returns the classifier.
     * The default value is {@code ""}.
     */
    public String classifier() { return classifier; }

    /** Returns an artifact descriptor with the given classifier. */
    public ArtifactDescriptor classifier(String classifier) {
        return classifier().equals(classifier)
                ? this
                : update().classifier(classifier).build();
    }

    /**
     * Returns the extension, e.g. {@code war}.
     * In the Maven realm, this may also be referred to as the artifact
     * <i>type</i> or <i>extension</i>.
     * The default value is {@code "jar"}.
     */
    public String extension() { return extension; }

    /** Returns an artifact descriptor with the given extension. */
    public ArtifactDescriptor extension(String extension) {
        return extension().equals(extension)
                ? this
                : update().extension(extension).build();
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

    /**
     * A builder for an artifact descriptor.
     *
     * @param <P> The type of the parent builder.
     */
    @SuppressWarnings("PackageVisibleField")
    public static class Builder<P> {

        @CheckForNull String groupId, artifactId, version, classifier,
                             extension;

        protected Builder() { }

        /** Selectively parses the given configuration item. */
        public Builder<P> parse(final ArtifactDescriptorDto ci) {
            groupId = resolve(ci.groupId, groupId);
            artifactId = resolve(ci.artifactId, artifactId);
            version = resolve(ci.version, version);
            classifier = resolve(ci.classifier, classifier);
            extension = resolve(ci.extension, extension);
            return this;
        }

        public Builder<P> groupId(final @Nullable String groupId) {
            this.groupId = groupId;
            return this;
        }

        public Builder<P> artifactId(final @Nullable String artifactId) {
            this.artifactId = artifactId;
            return this;
        }

        public Builder<P> version(final @Nullable String version) {
            this.version = version;
            return this;
        }

        public Builder<P> classifier(final @Nullable String classifier) {
            this.classifier = classifier;
            return this;
        }

        public Builder<P> extension(final @Nullable String extension) {
            this.extension = extension;
            return this;
        }

        public ArtifactDescriptor build() {
            return new ArtifactDescriptor(this);
        }

        /**
         * Injects the product of this builder into the parent builder, if
         * defined.
         *
         * @throws IllegalStateException if there is no parent builder defined.
         */
        public P inject() {
            throw new IllegalStateException("No parent builder defined.");
        }
    } // Builder
}
