/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.message;

import java.io.Serializable;
import java.net.URI;
import java.util.Date;
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

    private final long timestamp;
    private final URI from, to;
    private final Type type;
    private final ArtifactDescriptor artifactDescriptor;
    private final String status, updateVersion;
    private final URI oldLocation, newLocation;

    UpdateMessage(final Builder b) {
        this.timestamp = null != b.timestamp
                ? b.timestamp
                : System.currentTimeMillis();
        this.from = requireNonNull(b.from);
        this.to = requireNonNull(b.to);
        this.type = requireNonNull(b.type);
        this.status = requireNonNull(b.status);
        this.artifactDescriptor = requireNonNull(b.artifactDescriptor);
        this.updateVersion = requireNonNull(b.updateVersion);
        this.oldLocation = requireNonNull(b.oldLocation);
        this.newLocation = requireNonNull(b.newLocation);
    }

    /** Returns a new builder with all properties set from this instance. */
    public Builder update() {
        return create()
                .timestamp(timestamp())
                .from(from())
                .to(to())
                .type(type())
                .artifactDescriptor(artifactDescriptor())
                .status(status())
                .updateVersion(updateVersion())
                .oldLocation(oldLocation())
                .newLocation(newLocation());
    }

    /** Returns a new builder for an update message. */
    public static Builder create() { return new Builder(); }

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

    /** Returns the update message status. */
    public String status() { return status; }

    /** Returns an update message with the given update message status. */
    public UpdateMessage status(final String status) {
        return this.status.equals(status)
                ? this
                : update().status(status).build();
    }

    /** Returns the update version. */
    public String updateVersion() { return updateVersion; }

    /** Returns an update message with the given update version. */
    public UpdateMessage updateVersion(String updateVersion) {
        return this.updateVersion.equals(updateVersion)
                ? this
                : update().updateVersion(updateVersion).build();
    }

    /** Returns the old application location. */
    public URI oldLocation() { return oldLocation; }

    /** Returns an update message with the given old application location. */
    public UpdateMessage oldLocation(URI oldLocation) {
        return this.oldLocation.equals(oldLocation)
                ? this
                : update().oldLocation(oldLocation).build();
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
                : update().newLocation(newLocation).build();
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
                .type(type().successResponse())
                .from(to())
                .to(from())
                .status("")
                .timestamp(System.currentTimeMillis())
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
                .type(type().failureResponse())
                .from(to())
                .to(from())
                .status(ex.toString())
                .timestamp(System.currentTimeMillis())
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
                this.status().equals(that.status()) &&
                this.updateVersion().equals(that.updateVersion()) &&
                this.oldLocation().equals(that.oldLocation()) &&
                this.newLocation().equals(that.newLocation());
    }

    /** Returns a hash code which is consistent with {@link #equals(Object)}. */
    @Override public int hashCode() {
        int hash = 17;
        hash = 31 * hash + hashCode(timestamp);
        hash = 31 * hash + from().hashCode();
        hash = 31 * hash + to().hashCode();
        hash = 31 * hash + type().hashCode();
        hash = 31 * hash + artifactDescriptor().hashCode();
        hash = 31 * hash + status().hashCode();
        hash = 31 * hash + updateVersion().hashCode();
        hash = 31 * hash + oldLocation().hashCode();
        hash = 31 * hash + newLocation().hashCode();
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
                .append("Status: ").append(status()).append('\n')
                .append("Artifact-Descriptor: ").append(artifactDescriptor()).append('\n');
        if (!updateVersion().isEmpty())
            sb.append("Update-Version: ").append(updateVersion()).append('\n');
        if (!oldLocation().equals(EMPTY))
            sb.append("Old-Location: ").append(oldLocation()).append('\n');
        if (!newLocation().equals(EMPTY))
            sb.append("New-Location: ").append(newLocation()).append('\n');
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

            @Override void dispatchMessageTo(UpdateMessage message,
                                             UpdateMessageListener listener)
            throws UpdateMessageException {
                listener.onSubscriptionRequest(message);
            }
        },

        SUBSCRIPTION_SUCCESS_RESPONSE {

            @Override void dispatchMessageTo(UpdateMessage message,
                                             UpdateMessageListener listener)
            throws UpdateMessageException {
                listener.onSubscriptionSuccessResponse(message);
            }
        },

        SUBSCRIPTION_FAILURE_RESPONSE {

            @Override void dispatchMessageTo(UpdateMessage message,
                                             UpdateMessageListener listener)
            throws UpdateMessageException {
                listener.onSubscriptionFailureResponse(message);
            }
        },

        UPDATE_ANNOUNCEMENT {

            @Override void dispatchMessageTo(UpdateMessage message,
                                             UpdateMessageListener listener)
            throws UpdateMessageException {
                listener.onUpdateAnnouncement(message);
            }
        },

        INSTALLATION_REQUEST {

            @Override public Type successResponse() {
                return INSTALLATION_SUCCESS_RESPONSE;
            }

            @Override public Type failureResponse() {
                return INSTALLATION_FAILURE_RESPONSE;
            }

            @Override void dispatchMessageTo(UpdateMessage message,
                                             UpdateMessageListener listener)
            throws UpdateMessageException {
                listener.onInstallationRequest(message);
            }
        },

        INSTALLATION_SUCCESS_RESPONSE {

            @Override void dispatchMessageTo(UpdateMessage message,
                                             UpdateMessageListener listener)
            throws UpdateMessageException {
                listener.onInstallationSuccessResponse(message);
            }
        },

        INSTALLATION_FAILURE_RESPONSE {

            @Override void dispatchMessageTo(UpdateMessage message,
                                             UpdateMessageListener listener)
            throws UpdateMessageException {
                listener.onInstallationFailureResponse(message);
            }
        },

        UNSUBSCRIPTION_REQUEST {

            @Override public Type successResponse() {
                return UNSUBSCRIPTION_SUCCESS_RESPONSE;
            }

            @Override public Type failureResponse() {
                return UNSUBSCRIPTION_FAILURE_RESPONSE;
            }

            @Override void dispatchMessageTo(UpdateMessage message,
                                             UpdateMessageListener listener)
            throws UpdateMessageException {
                listener.onUnsubscriptionRequest(message);
            }
        },

        UNSUBSCRIPTION_SUCCESS_RESPONSE {

            @Override void dispatchMessageTo(UpdateMessage message,
                                             UpdateMessageListener listener)
            throws UpdateMessageException {
                listener.onUnsubscriptionSuccessResponse(message);
            }
        },

        UNSUBSCRIPTION_FAILURE_RESPONSE {

            @Override void dispatchMessageTo(UpdateMessage message,
                                             UpdateMessageListener listener)
            throws UpdateMessageException {
                listener.onUnsubscriptionFailureResponse(message);
            }
        };

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
                                        UpdateMessageListener listener)
        throws UpdateMessageException;
    } // Type

    /**
     * A builder for an update message.
     * The default value for the timestamp is the time the {@link #build()}
     * method gets called in milliseconds since the epoch.
     * The default value for the properties {@code status} and
     * {@code updateVersion} is an empty string.
     * The default value for the properties {@code oldLocation} and
     * {@code newLocation} is an empty URI.
     */
    @SuppressWarnings("PackageVisibleField")
    public static final class Builder {

        Long timestamp;
        URI from, to;
        Type type;
        ArtifactDescriptor artifactDescriptor;
        String status = "", updateVersion = "";
        URI oldLocation = EMPTY, newLocation = EMPTY;

        Builder() { }

        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder from(URI from) {
            this.from = requireNonNull(from);
            return this;
        }

        public Builder to(URI to) {
            this.to = requireNonNull(to);
            return this;
        }

        public Builder type(final Type type) {
            this.type = requireNonNull(type);
            return this;
        }

        public Builder status(final String status) {
            this.status = requireNonNull(status);
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

        public UpdateMessage build() { return new UpdateMessage(this); }
    } // Builder
}
