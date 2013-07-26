/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.core.artifact;

import java.io.Serializable;
import static java.util.Objects.requireNonNull;

/**
 * Describes an artifact by its Maven coordinates.
 * Implementations should be immutable.
 *
 * @see    <a href="http://maven.apache.org/pom.html#Maven_Coordinates">Maven - POM Reference - Maven Coordinates</a>
 * @author Christian Schlichtherle
 */
public interface ArtifactDescriptor extends Serializable {

    /** Returns the group id, e.g. {@code com.stimulus.archiva}. */
    String groupId();

    /** Returns the artifact id, e.g. {@code mailarchiva}. */
    String artifactId();

    /** Returns the version, e.g. {@code 3.2.1}. */
    String version();

    /** Returns the classifier, which may be empty. */
    String classifier();

    /**
     * Returns the extension, e.g. {@code war}.
     * In the Maven realm, this may also be referred to as the artifact
     * <i>type</i> or <i>extension</i>.
     */
    String extension();

    /**
     * Returns {@code true} if and only if the given object is an
     * {@link ArtifactDescriptor} with equal properties.
     */
    @Override boolean equals(Object obj);

    /**
     * Returns a hash code for this artifact descriptor which is consistent
     * with {@link #equals(Object)}.
     */
    @Override int hashCode();

    /** Returns a human readable string representation of this descriptor. */
    @Override String toString();

    /**
     * A builder for an artifact descriptor.
     * The default value for the property {@code extension} is {@code "jar"}
     * and the default value for the property {@code classifier} is {@code ""}.
     */
    class Builder {

        private String groupId, artifactId, version,
                extension = "jar", classifier = "";

        public Builder groupId(final String groupId) {
            this.groupId = requireNonNull(groupId);
            return this;
        }

        public Builder artifactId(final String artifactId) {
            this.artifactId = requireNonNull(artifactId);
            return this;
        }

        public Builder version(final String version) {
            this.version = requireNonNull(version);
            return this;
        }

        public Builder classifier(final String classifier) {
            this.classifier = requireNonNull(classifier);
            return this;
        }

        public Builder extension(final String packaging) {
            this.extension = requireNonNull(packaging);
            return this;
        }

        public ArtifactDescriptor build() {
            return build(groupId, artifactId, version, extension, classifier);
        }

        private static ArtifactDescriptor build(
                final String groupId,
                final String artifactId,
                final String version,
                final String extension,
                final String classifier) {
            requireNonNull(groupId);
            requireNonNull(artifactId);
            requireNonNull(version);
            requireNonNull(classifier);
            requireNonNull(extension);
            return new BasicArtifactDescriptor() {
                private static final long serialVersionUID = 0L;

                @Override public String groupId() { return groupId; }
                @Override public String artifactId() { return artifactId; }
                @Override public String version() { return version; }
                @Override public String classifier() { return classifier; }
                @Override public String extension() { return extension; }
            };
        }
    }
}
