/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.spec;

import java.io.Serializable;
import java.net.URI;
import java.util.Date;
import static java.util.Objects.requireNonNull;
import javax.annotation.*;
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

    private static final URI EMPTY = URI.create("");

    private final long timestamp;
    private final URI from, to;
    private final Type type;
    private final ArtifactDescriptor artifactDescriptor;
    private final URI currentLocation, updateLocation;
    private final String updateVersion, status;

    UpdateMessage(final Builder b) {
        this.timestamp = nonNullOrNow(b.timestamp);
        this.from = requireNonNull(b.from);
        this.to = requireNonNull(b.to);
        this.type = requireNonNull(b.type);
        this.artifactDescriptor = requireNonNull(b.artifactDescriptor);
        this.currentLocation = nonNullOr(b.currentLocation, EMPTY);
        this.updateLocation = nonNullOr(b.updateLocation, currentLocation);
        this.updateVersion = nonNullOr(b.updateVersion, "");
        this.status = nonNullOr(b.status, "");
    }

    private static long nonNullOrNow(Long timestamp) {
        return null != timestamp ? timestamp : System.currentTimeMillis();
    }

    private static <T> T nonNullOr(T value, T eagerDefault) {
        return null != value ? value : eagerDefault;
    }

    /** Returns a new builder with all properties set from this instance. */
    public Builder update() {
        return builder()
                .timestamp(timestamp())
                .from(from())
                .to(to())
                .type(type())
                .artifactDescriptor(artifactDescriptor())
                .currentLocation(currentLocation())
                .updateLocation(updateLocation())
                .updateVersion(updateVersion())
                .status(status());
    }

    /**
     * Returns a new builder for an update message.
     * The default value for the property {@code timestamp} is the creation
     * time of the update message in milliseconds since the epoch.
     * The default value for the properties {@code status} and
     * {@code updateVersion} is an empty string.
     * The default value for the property {@code location} is an empty URI.
     * The default value for the property {@code updateLocation} is the
     * effective value of the property {@code location}.
     */
    public static Builder builder() { return new Builder(); }

    /** Returns the update message timestamp. */
    public long timestamp() { return timestamp; }

    /** Returns an update message with the given update message timestamp. */
    public UpdateMessage timestamp(long timestamp) {
        return this.timestamp == timestamp
                ? this
                : update().timestamp(timestamp).build();
    }

    /** Returns the update message sender. */
    public URI from() { return from; }

    /** Returns an update message with the given update message sender. */
    public UpdateMessage from(URI from) {
        return this.from.equals(from)
                ? this
                : update().from(from).build();
    }

    /** Returns the update message recipient. */
    public URI to() { return to; }

    /** Returns an update message with the given update message recipient. */
    public UpdateMessage to(URI to) {
        return this.to.equals(to)
                ? this
                : update().to(to).build();
    }

    /** Returns the update message type. */
    public Type type() { return type; }

    /** Returns an update message with the given update message type. */
    public UpdateMessage type(Type type) {
        return this.type.equals(type)
                ? this
                : update().type(type).build();
    }

    /** Returns the artifact descriptor. */
    public ArtifactDescriptor artifactDescriptor() {
        return artifactDescriptor;
    }

    /** Returns an update message with the given artifact descriptor. */
    public UpdateMessage artifactDescriptor(ArtifactDescriptor artifactDescriptor) {
        return this.artifactDescriptor.equals(artifactDescriptor)
                ? this
                : update().artifactDescriptor(artifactDescriptor).build();
    }

    /** Returns the current location. */
    public URI currentLocation() { return currentLocation; }

    /** Returns an update message with the given current location. */
    public UpdateMessage currentLocation(URI currentLocation) {
        return this.currentLocation.equals(currentLocation)
                ? this
                : update().currentLocation(currentLocation).build();
    }

    /**
     * Returns the update location.
     * If this equals {@link #currentLocation()}, then the update should happen
     * in-place.
     */
    public URI updateLocation() { return updateLocation; }

    /** Returns an update message with the given update location. */
    public UpdateMessage updateLocation(URI newLocation) {
        return this.updateLocation.equals(newLocation)
                ? this
                : update().updateLocation(newLocation).build();
    }

    /** Returns the update version. */
    public String updateVersion() { return updateVersion; }

    /** Returns an update message with the given update version. */
    public UpdateMessage updateVersion(String updateVersion) {
        return this.updateVersion.equals(updateVersion)
                ? this
                : update().updateVersion(updateVersion).build();
    }

    /** Returns the status text. */
    public String status() { return status; }

    /** Returns an update message with the given status text. */
    public UpdateMessage status(final String status) {
        return this.status.equals(status)
                ? this
                : update().status(status).build();
    }

    /**
     * Returns a success response for this update message with an empty status,
     * swapped from/to URIs and an updated time stamp.
     * First, checks if the type of this update message is a {@code *_REQUEST}.
     * If no, then an {@link UnsupportedOperationException} gets thrown.
     * If yes, then a new update message of the corresponding type
     * {@code *_SUCCESS_RESPONSE} gets returned with an empty status and an
     * updated time stamp.
     */
    public UpdateMessage successResponse() {
        return update()
                .timestamp(null)
                .type(type().successResponse())
                .from(to())
                .to(from())
                .status(null)
                .build();
    }

    /**
     * Returns a failure response for this update message with the string
     * representation of the given exception as the status, swapped from/to
     * URIs and an updated time stamp.
     * First, checks if the type of this update message is a {@code *_REQUEST}.
     * If no, then an {@link UnsupportedOperationException} gets thrown.
     * If yes, then a new update message of the corresponding type
     * {@code *_FAILURE_RESPONSE} gets returned with the string representation
     * of the given exception as the status and an updated time stamp.
     */
    public UpdateMessage failureResponse(Exception ex) {
        return update()
                .timestamp(null)
                .type(type().failureResponse())
                .from(to())
                .to(from())
                .status(ex.toString())
                .build();
    }

    /**
     * Extracts an application descriptor from the information in this update
     * message.
     */
    public ApplicationDescriptor applicationDescriptor() {
        return ApplicationDescriptor
                .builder()
                .artifactDescriptor(artifactDescriptor())
                .currentLocation(currentLocation())
                .build();
    }

    /**
     * Returns {@code true} if and only if the given object is an
     * {@code UpdateMessage} with equal properties.
     */
    @Override public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof UpdateMessage)) return false;
        final UpdateMessage that = (UpdateMessage) obj;
        return  this.timestamp() == that.timestamp() &&
                this.from().equals(that.from()) &&
                this.to().equals(that.to()) &&
                this.type().equals(that.type()) &&
                this.artifactDescriptor().equals(that.artifactDescriptor()) &&
                this.currentLocation().equals(that.currentLocation()) &&
                this.updateLocation().equals(that.updateLocation()) &&
                this.updateVersion().equals(that.updateVersion()) &&
                this.status().equals(that.status());
    }

    /** Returns a hash code which is consistent with {@link #equals(Object)}. */
    @Override public int hashCode() {
        int hash = 17;
        hash = 31 * hash + hashCode(timestamp);
        hash = 31 * hash + from().hashCode();
        hash = 31 * hash + to().hashCode();
        hash = 31 * hash + type().hashCode();
        hash = 31 * hash + artifactDescriptor().hashCode();
        hash = 31 * hash + currentLocation().hashCode();
        hash = 31 * hash + updateLocation().hashCode();
        hash = 31 * hash + updateVersion().hashCode();
        hash = 31 * hash + status().hashCode();
        return hash;
    }

    private static int hashCode(long value) {
        return (int) ((value >> 32) ^ value);
    }

    /** Returns a human readable string representation of this object. */
    @Override public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb      .append("Timestamp: ").append(new Date(timestamp())).append('\n')
                .append("From: ").append(from()).append('\n')
                .append("To: ").append(to()).append('\n')
                .append("Type: ").append(type()).append('\n')
                .append("Artifact-Descriptor: ").append(artifactDescriptor()).append('\n');
        if (!currentLocation().equals(EMPTY))
            sb.append("Current-Location: ").append(currentLocation()).append('\n');
        if (!updateLocation().equals(EMPTY))
            sb.append("Update-Location: ").append(updateLocation()).append('\n');
        if (!updateVersion().isEmpty())
            sb.append("Update-Version: ").append(updateVersion()).append('\n');
        if (!status().isEmpty())
            sb.append("Status: ").append(status()).append('\n');
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

        SUBSCRIPTION_NOTICE {

            @Override public boolean forManager() { return true; }

            @Override public Type successResponse() {
                return SUBSCRIPTION_SUCCESS_RESPONSE;
            }

            @Override public Type failureResponse() {
                return SUBSCRIPTION_FAILURE_RESPONSE;
            }

            @Override void dispatchMessageTo(UpdateMessage message,
                                             UpdateMessageDispatcher dispatcher)
            throws Exception {
                dispatcher.onSubscriptionNotice(message);
            }
        },

        SUBSCRIPTION_REQUEST {

            @Override public boolean forManager() { return true; }

            @Override public Type successResponse() {
                return SUBSCRIPTION_SUCCESS_RESPONSE;
            }

            @Override public Type failureResponse() {
                return SUBSCRIPTION_FAILURE_RESPONSE;
            }

            @Override void dispatchMessageTo(UpdateMessage message,
                                             UpdateMessageDispatcher dispatcher)
            throws Exception {
                dispatcher.onSubscriptionRequest(message);
            }
        },

        SUBSCRIPTION_SUCCESS_RESPONSE {

            @Override public boolean forManager() { return false; }

            @Override void dispatchMessageTo(UpdateMessage message,
                                             UpdateMessageDispatcher dispatcher)
            throws Exception {
                dispatcher.onSubscriptionSuccessResponse(message);
            }
        },

        SUBSCRIPTION_FAILURE_RESPONSE {

            @Override public boolean forManager() { return false; }

            @Override void dispatchMessageTo(UpdateMessage message,
                                             UpdateMessageDispatcher dispatcher)
            throws Exception {
                dispatcher.onSubscriptionFailureResponse(message);
            }
        },

        UPDATE_NOTICE {

            @Override public boolean forManager() { return false; }

            @Override void dispatchMessageTo(UpdateMessage message,
                                             UpdateMessageDispatcher dispatcher)
            throws Exception {
                dispatcher.onUpdateNotice(message);
            }
        },

        INSTALLATION_REQUEST {

            @Override public boolean forManager() { return true; }

            @Override public Type successResponse() {
                return INSTALLATION_SUCCESS_RESPONSE;
            }

            @Override public Type failureResponse() {
                return INSTALLATION_FAILURE_RESPONSE;
            }

            @Override void dispatchMessageTo(UpdateMessage message,
                                             UpdateMessageDispatcher dispatcher)
            throws Exception {
                dispatcher.onInstallationRequest(message);
            }
        },

        INSTALLATION_SUCCESS_RESPONSE {

            @Override public boolean forManager() { return false; }

            @Override void dispatchMessageTo(UpdateMessage message,
                                             UpdateMessageDispatcher dispatcher)
            throws Exception {
                dispatcher.onInstallationSuccessResponse(message);
            }
        },

        INSTALLATION_FAILURE_RESPONSE {

            @Override public boolean forManager() { return false; }

            @Override void dispatchMessageTo(UpdateMessage message,
                                             UpdateMessageDispatcher dispatcher)
            throws Exception {
                dispatcher.onInstallationFailureResponse(message);
            }
        },

        UNSUBSCRIPTION_NOTICE {

            @Override public boolean forManager() { return true; }

            @Override public Type successResponse() {
                return UNSUBSCRIPTION_SUCCESS_RESPONSE;
            }

            @Override public Type failureResponse() {
                return UNSUBSCRIPTION_FAILURE_RESPONSE;
            }

            @Override void dispatchMessageTo(UpdateMessage message,
                                             UpdateMessageDispatcher dispatcher)
            throws Exception {
                dispatcher.onUnsubscriptionNotice(message);
            }
        },

        UNSUBSCRIPTION_REQUEST {

            @Override public boolean forManager() { return true; }

            @Override public Type successResponse() {
                return UNSUBSCRIPTION_SUCCESS_RESPONSE;
            }

            @Override public Type failureResponse() {
                return UNSUBSCRIPTION_FAILURE_RESPONSE;
            }

            @Override void dispatchMessageTo(UpdateMessage message,
                                             UpdateMessageDispatcher dispatcher)
            throws Exception {
                dispatcher.onUnsubscriptionRequest(message);
            }
        },

        UNSUBSCRIPTION_SUCCESS_RESPONSE {

            @Override public boolean forManager() { return false; }

            @Override void dispatchMessageTo(UpdateMessage message,
                                             UpdateMessageDispatcher dispatcher)
            throws Exception {
                dispatcher.onUnsubscriptionSuccessResponse(message);
            }
        },

        UNSUBSCRIPTION_FAILURE_RESPONSE {

            @Override public boolean forManager() { return false; }

            @Override void dispatchMessageTo(UpdateMessage message,
                                             UpdateMessageDispatcher dispatcher)
            throws Exception {
                dispatcher.onUnsubscriptionFailureResponse(message);
            }
        };

        /**
         * Returns {@code true} if and only if messages of this type should be
         * processed by an update manager.
         */
        public abstract boolean forManager();

        /**
         * Returns the corresponding {@code *_SUCCESS_RESPONSE} if and only if
         * this is a {@code *_REQUEST} type.
         * Otherwise throws an {@link UnsupportedOperationException}.
         */
        public Type successResponse() {
            throw new UnsupportedOperationException();
        }

        /**
         * Returns the corresponding {@code *_FAILURE_RESPONSE} if and only if
         * this is a {@code *_REQUEST} type.
         * Otherwise throws an {@link UnsupportedOperationException}.
         */
        public Type failureResponse() {
            throw new UnsupportedOperationException();
        }

        abstract void dispatchMessageTo(UpdateMessage message,
                                        UpdateMessageDispatcher dispatcher)
        throws Exception;
    } // Type

    /** A builder for an update message. */
    @SuppressWarnings("PackageVisibleField")
    public static final class Builder {

        @CheckForNull Long timestamp;
        @CheckForNull URI from, to;
        @CheckForNull Type type;
        @CheckForNull ArtifactDescriptor artifactDescriptor;
        @CheckForNull URI currentLocation, updateLocation;
        @CheckForNull String updateVersion, status;

        Builder() { }

        public Builder timestamp(final @Nullable Long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder from(final @Nullable URI from) {
            this.from = from;
            return this;
        }

        public Builder to(final @Nullable URI to) {
            this.to = to;
            return this;
        }

        public Builder type(final @Nullable Type type) {
            this.type = type;
            return this;
        }

        public Builder artifactDescriptor(
                final @Nullable ArtifactDescriptor artifactDescriptor) {
            this.artifactDescriptor = artifactDescriptor;
            return this;
        }

        public Builder currentLocation(final @Nullable URI currentLocation) {
            this.currentLocation = currentLocation;
            return this;
        }

        public Builder updateLocation(final @Nullable URI updateLocation) {
            this.updateLocation = updateLocation;
            return this;
        }

        public Builder updateVersion(final @Nullable String updateVersion) {
            this.updateVersion = updateVersion;
            return this;
        }

        public Builder status(final @Nullable String status) {
            this.status = status;
            return this;
        }

        public UpdateMessage build() { return new UpdateMessage(this); }
    } // Builder
}
