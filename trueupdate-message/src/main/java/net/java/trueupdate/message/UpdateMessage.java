/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.message;

import java.io.Serializable;
import java.net.URI;
import static java.util.Objects.requireNonNull;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.artifact.spec.ArtifactDescriptor;

/**
 * A Value Object which gets exchanged between update agents and update
 * managers in order to establish a protocol for the automated installation of
 * application updates.
 *
 * @see Type
 * @author Christian Schlichtherle
 */
@Immutable
public final class UpdateMessage implements Serializable {

    private static final long serialVersionUID = 0L;

    static final URI EMPTY = URI.create("");

    private final Type type;
    private final ArtifactDescriptor artifactDescriptor;
    private final String status, updateVersion;
    private final URI oldLocation, newLocation;

    UpdateMessage(
            final Type type,
            final String status,
            final ArtifactDescriptor artifactDescriptor,
            final String updateVersion,
            final URI oldLocation,
            final URI newLocation) {
        this.type = requireNonNull(type);
        this.status = requireNonNull(status);
        this.artifactDescriptor = requireNonNull(artifactDescriptor);
        this.updateVersion = requireNonNull(updateVersion);
        this.oldLocation = requireNonNull(oldLocation);
        this.newLocation = requireNonNull(newLocation);
    }

    /** Returns the update message type. */
    public Type type() { return type; }

    /** Returns an update message with the given update message type. */
    public UpdateMessage type(Type type) {
        return this.type.equals(type)
                ? this
                : new UpdateMessage(type, status, artifactDescriptor,
                    updateVersion, oldLocation, newLocation);
    }

    /** Returns the update message status. */
    public String status() { return status; }

    /** Returns an update message with the given update message status. */
    public UpdateMessage status(final String status) {
        return this.status.equals(status)
                ? this
                : new UpdateMessage(type, status, artifactDescriptor,
                    updateVersion, oldLocation, newLocation);
    }

    /** Returns the artifact descriptor. */
    public ArtifactDescriptor artifactDescriptor() {
        return artifactDescriptor;
    }

    /** Returns an update message with the given artifact descriptor. */
    public UpdateMessage artifactDescriptor(ArtifactDescriptor artifactDescriptor) {
        return this.artifactDescriptor.equals(artifactDescriptor)
                ? this
                : new UpdateMessage(type, status, artifactDescriptor,
                    updateVersion, oldLocation, newLocation);
    }

    /** Returns the update version. */
    public String updateVersion() { return updateVersion; }

    /** Returns an update message with the given update version. */
    public UpdateMessage updateVersion(String updateVersion) {
        return this.updateVersion.equals(updateVersion)
                ? this
                : new UpdateMessage(type, status, artifactDescriptor,
                    updateVersion, oldLocation, newLocation);
    }

    /** Returns the old application location. */
    public URI oldLocation() { return oldLocation; }

    /** Returns an update message with the given old application location. */
    public UpdateMessage oldLocation(URI oldLocation) {
        return this.oldLocation.equals(oldLocation)
                ? this
                : new UpdateMessage(type, status, artifactDescriptor,
                    updateVersion, oldLocation, newLocation);
    }

    /**
     * Returns the new application location.
     * If this equals {@link #oldLocation()}, then the update should happen
     * in-place.
     */
    public URI newLocation() { return newLocation; }

    /** Returns an update message with the given new application location. */
    public UpdateMessage newLocation(URI newLocation) {
        return this.newLocation.equals(newLocation)
                ? this
                : new UpdateMessage(type, status, artifactDescriptor,
                    updateVersion, oldLocation, newLocation);
    }

    /**
     * Returns a success response for this update message with an empty status.
     */
    public UpdateMessage successResponse() {
        return type(type().successResponse()).status("");
    }

    /**
     * Returns a failure response for this update message with the string
     * representation of the given exception as the status.
     */
    public UpdateMessage failureResponse(Exception ex) {
        return type(type().failureResponse()).status(ex.toString());
    }

    /**
     * Returns {@code true} if and only if the given object is an
     * {@code UpdateMessage} with equal properties.
     */
    @Override public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof UpdateMessage)) return false;
        final UpdateMessage that = (UpdateMessage) obj;
        return  this.type().equals(that.type()) &&
                this.artifactDescriptor().equals(that.artifactDescriptor()) &&
                this.updateVersion().equals(that.updateVersion()) &&
                this.oldLocation().equals(that.oldLocation()) &&
                this.newLocation().equals(that.newLocation());
    }

    /** Returns a hash code which is consistent with {@link #equals(Object)}. */
    @Override public int hashCode() {
        int hash = 17;
        hash = 31 * hash + type().hashCode();
        hash = 31 * hash + artifactDescriptor().hashCode();
        hash = 31 * hash + updateVersion().hashCode();
        hash = 31 * hash + oldLocation().hashCode();
        hash = 31 * hash + newLocation().hashCode();
        return hash;
    }

    /** Returns a human readable string representation of this object. */
    @Override public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb      .append("Type: ").append(type()).append('\n')
                .append("Artifact Descriptor: ").append(artifactDescriptor()).append('\n');
        if (!updateVersion().isEmpty())
            sb.append("Update Version: ").append(updateVersion()).append('\n');
        if (!oldLocation().equals(EMPTY))
            sb.append("Old Location: ").append(oldLocation()).append('\n');
        if (!newLocation().equals(EMPTY))
            sb.append("New Location: ").append(newLocation()).append('\n');
        return sb.toString();
    }

    /**
     * The update message type.
     * The communication protocol works as follows:
     * <ol>
     * <li>
     * The update agent needs to send a {@link #SUBSCRIPTION_REQUEST} in
     * order to subscribe to the list of recipients for update announcements
     * for the application.
     * <li>
     * The update manager needs to send a
     * {@link #SUBSCRIPTION_SUCCESS_RESPONSE} or a
     * {@link #SUBSCRIPTION_FAILURE_RESPONSE}.
     * <li>
     * Upon a successful subscription, the update manager needs to send an
     * {@link #UPDATE_ANNOUNCEMENT} for every update.
     * <li>
     * The update agent may send an {@link #INSTALLATION_REQUEST}.
     * <li>
     * The update manager needs to install the application update and send an
     * {@link #INSTALLATION_SUCCESS_RESPONSE} or an
     * {@link #INSTALLATION_FAILURE_RESPONSE}.
     * <li>
     * The update agent may send an {@link #UNSUBSCRIPTION_REQUEST} in order to
     * unsubscribe from the list of recipients for update announcements for the
     * application.
     * <li>
     * The update manager needs to send an
     * {@link #UNSUBSCRIPTION_SUCCESS_RESPONSE} or an
     * {@link #UNSUBSCRIPTION_FAILURE_RESPONSE}.
     * </ol>
     * <p>
     * Note that messages may get lost or duplicated and no timeout is defined.
     */
    public enum Type {
        SUBSCRIPTION_REQUEST {

            @Override public Type successResponse() {
                return SUBSCRIPTION_SUCCESS_RESPONSE;
            }

            @Override public Type failureResponse() {
                return SUBSCRIPTION_FAILURE_RESPONSE;
            }
        },

        SUBSCRIPTION_SUCCESS_RESPONSE,
        SUBSCRIPTION_FAILURE_RESPONSE,
        UPDATE_ANNOUNCEMENT,

        INSTALLATION_REQUEST {

            @Override public Type successResponse() {
                return INSTALLATION_SUCCESS_RESPONSE;
            }

            @Override public Type failureResponse() {
                return INSTALLATION_FAILURE_RESPONSE;
            }
        },

        INSTALLATION_SUCCESS_RESPONSE,
        INSTALLATION_FAILURE_RESPONSE,

        UNSUBSCRIPTION_REQUEST {

            @Override public Type successResponse() {
                return UNSUBSCRIPTION_SUCCESS_RESPONSE;
            }

            @Override public Type failureResponse() {
                return UNSUBSCRIPTION_FAILURE_RESPONSE;
            }
        },

        UNSUBSCRIPTION_SUCCESS_RESPONSE,
        UNSUBSCRIPTION_FAILURE_RESPONSE;

        /**
         * Returns the corresponding {@code *_SUCCESS_RESPONSE} if and only if
         * this is a {@code *_REQUEST} type.
         * Otherwise returns {@code this}.
         */
        public Type successResponse() { return this; }

        /**
         * Returns the corresponding {@code *_FAILURE_RESPONSE} if and only if
         * this is a {@code *_REQUEST} type.
         * Otherwise returns {@code this}.
         */
        public Type failureResponse() { return this; }
    } // Type

    /**
     * A builder for an update message.
     * The default value for the properties {@code status} and
     * {@code updateVersion} is an empty string and the default value for the
     * properties {@code oldLocation} and {@code newLocation} is an empty URI.
     */
    public static final class Builder {

        private Type type;
        private ArtifactDescriptor artifactDescriptor;
        private String status = "", updateVersion = "";
        private URI oldLocation = EMPTY, newLocation = EMPTY;

        public Builder type(final Type type) {
            this.type = requireNonNull(type);
            return this;
        }

        public Builder artifactDescriptor(
                final ArtifactDescriptor artifactDescriptor) {
            this.artifactDescriptor = requireNonNull(artifactDescriptor);
            return this;
        }

        public Builder updateVersion(final String updateVersion) {
            this.updateVersion = requireNonNull(updateVersion);
            return this;
        }

        public Builder oldLocation(final URI oldLocation) {
            this.oldLocation = requireNonNull(oldLocation);
            return this;
        }

        public Builder newLocation(final URI newLocation) {
            this.newLocation = requireNonNull(newLocation);
            return this;
        }

        public UpdateMessage build() {
            return new UpdateMessage(type, status, artifactDescriptor,
                    updateVersion, oldLocation, newLocation);
        }
    } // Builder
}
