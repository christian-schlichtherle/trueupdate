/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.artifact.spec;

import javax.annotation.*;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.artifact.spec.ci.ArtifactDescriptorCi;
import static net.java.trueupdate.util.Objects.*;
import static net.java.trueupdate.util.Strings.*;
import static net.java.trueupdate.util.SystemProperties.resolve;
import net.java.trueupdate.util.builder.AbstractBuilder;

/**
 * An artifact descriptor comprises of a group ID, an artifact ID, a version,
 * an optional classifier and an optional packaging.
 * This is effectively the same model as with Maven coordinates in order to
 * enable a Maven based implementation of an
 * {@linkplain ArtifactResolver artifact resolver} without actually depending
 * on Maven in this specification.
 * This class implements an immutable value object, so you can easily share its
 * instances with anyone or use them as map keys.
 *
 * @see    <a href="http://maven.apache.org/pom.html#Maven_Coordinates">Maven - POM Reference - Maven Coordinates</a>
 * @author Christian Schlichtherle
 */
@Immutable
public final class ArtifactDescriptor {

    private final String groupId, artifactId, version, classifier, packaging;

    ArtifactDescriptor(final Builder<?> b) {
        this.groupId = requireNonEmpty(b.groupId);
        this.artifactId = requireNonEmpty(b.artifactId);
        this.version = requireNonEmpty(b.version);
        this.classifier = nonNullOr(b.classifier, "");
        this.packaging = nonNullOr(b.packaging, "jar");
    }

    /** Returns a new builder with all properties set from this instance. */
    public Builder<Void> update() {
        return builder()
                .groupId(groupId)
                .artifactId(artifactId)
                .version(version)
                .classifier(classifier)
                .packaging(packaging);
    }

    /** Parses the given configuration item. */
    public static ArtifactDescriptor parse(ArtifactDescriptorCi ci) {
        return builder().parse(ci).build();
    }

    /** Returns a new builder for an artifact descriptor. */
    public static Builder<Void> builder() { return new Builder<Void>(); }

    /** Returns the group id, e.g. {@code net.java.trueupdate}. */
    public String groupId() { return groupId; }

    /** Returns an artifact descriptor with the given group id. */
    public ArtifactDescriptor groupId(String groupId) {
        return groupId.equals(this.groupId)
                ? this
                : update().groupId(groupId).build();
    }

    /** Returns the artifact id, e.g. {@code trueupdate-core}. */
    public String artifactId() { return artifactId; }

    /** Returns an artifact descriptor with the given artifact id. */
    public ArtifactDescriptor artifactId(String artifactId) {
        return artifactId.equals(this.artifactId)
                ? this
                : update().artifactId(artifactId).build();
    }

    /** Returns the version, e.g. {@code 3.2.1}. */
    public String version() { return version; }

    /** Returns an artifact descriptor with the given version. */
    public ArtifactDescriptor version(String version) {
        return version.equals(this.version)
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
        return classifier.equals(this.classifier)
                ? this
                : update().classifier(classifier).build();
    }

    /**
     * Returns the packaging, e.g. {@code war}.
     * In the Maven realm, this may also be referred to as the artifact
     * <i>type</i> or <i>extension</i>.
     * The default value is {@code "jar"}.
     */
    public String packaging() { return packaging; }

    /** Returns an artifact descriptor with the given packaging. */
    public ArtifactDescriptor packaging(String packaging) {
        return packaging.equals(this.packaging)
                ? this
                : update().packaging(packaging).build();
    }

    /**
     * Returns {@code true} if and only if the given object is an
     * {@code ArtifactDescriptor} with equal properties.
     */
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    @Override public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ArtifactDescriptor)) return false;
        final ArtifactDescriptor that = (ArtifactDescriptor) obj;
        return  this.groupId.equals(that.groupId) &&
                this.artifactId.equals(that.artifactId) &&
                this.version.equals(that.version) &&
                this.classifier.equals(that.classifier) &&
                this.packaging.equals(that.packaging);
    }

    /** Returns a hash code which is consistent with {@link #equals(Object)}. */
    @Override public int hashCode() {
        int hash = 17;
        hash = 31 * hash + groupId.hashCode();
        hash = 31 * hash + artifactId.hashCode();
        hash = 31 * hash + version.hashCode();
        hash = 31 * hash + classifier.hashCode();
        hash = 31 * hash + packaging.hashCode();
        return hash;
    }

    /** Returns a human readable string representation of this object. */
    @Override public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb      .append(groupId())
                .append(':').append(artifactId)
                .append(':').append(packaging);
        if (0 != classifier.length())
            sb.append(':').append(classifier);
        sb.append(':').append(version());
        return sb.toString();
    }

    /**
     * A builder for an artifact descriptor.
     *
     * @param <P> The type of the parent builder, if defined.
     */
    @SuppressWarnings("PackageVisibleField")
    public static class Builder<P> extends AbstractBuilder<P> {

        @CheckForNull String groupId, artifactId, version, classifier,
                             packaging;

        protected Builder() { }

        /** Selectively parses the given configuration item. */
        public final Builder<P> parse(final ArtifactDescriptorCi ci) {
            groupId = resolve(ci.groupId, groupId);
            artifactId = resolve(ci.artifactId, artifactId);
            version = resolve(ci.version, version);
            classifier = resolve(ci.classifier, classifier);
            packaging = resolve(ci.packaging, packaging);
            return this;
        }

        public final Builder<P> groupId(final @Nullable String groupId) {
            this.groupId = groupId;
            return this;
        }

        public final Builder<P> artifactId(final @Nullable String artifactId) {
            this.artifactId = artifactId;
            return this;
        }

        public final Builder<P> version(final @Nullable String version) {
            this.version = version;
            return this;
        }

        public final Builder<P> classifier(final @Nullable String classifier) {
            this.classifier = classifier;
            return this;
        }

        public final Builder<P> packaging(final @Nullable String packaging) {
            this.packaging = packaging;
            return this;
        }

        @Override public final ArtifactDescriptor build() {
            return new ArtifactDescriptor(this);
        }
    } // Builder
}
