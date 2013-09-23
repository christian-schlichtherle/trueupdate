/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.message;

import java.util.Date;
import javax.annotation.*;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.artifact.spec.*;
import static net.java.trueupdate.util.Objects.*;

/**
 * An immutable Value Object which gets exchanged between update agents and
 * update managers in order to establish a protocol for the automated
 * installation of application updates.
 *
 * @see Type
 * @author Christian Schlichtherle
 */
@Immutable
public final class UpdateMessage {

    private final long timestamp;
    private final String from, to;
    private final Type type;
    private final ArtifactDescriptor artifactDescriptor;
    private final String updateVersion, status;
    private final String currentLocation, updateLocation;

    UpdateMessage(final Builder<?> b) {
        this.timestamp = nonNullOrNow(b.timestamp);
        this.from = requireNonNull(b.from);
        this.to = requireNonNull(b.to);
        this.type = requireNonNull(b.type);
        this.artifactDescriptor = requireNonNull(b.artifactDescriptor);
        this.updateVersion = nonNullOr(b.updateVersion, "");
        this.currentLocation = nonNullOr(b.currentLocation, "");
        this.updateLocation = nonNullOr(b.updateLocation, currentLocation);
        this.status = nonNullOr(b.status, "");
    }

    private static long nonNullOrNow(Long timestamp) {
        return null != timestamp ? timestamp : System.currentTimeMillis();
    }

    /** Returns a new builder with all properties set from this instance. */
    public Builder<Void> update() {
        return builder()
                .timestamp(timestamp())
                .from(from())
                .to(to())
                .type(type())
                .artifactDescriptor(artifactDescriptor())
                .updateVersion(updateVersion())
                .currentLocation(currentLocation())
                .updateLocation(updateLocation())
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
    public static Builder<Void> builder() { return new Builder<Void>(); }

    /** Returns the update message timestamp. */
    public long timestamp() { return timestamp; }

    /** Returns an update message with the given update message timestamp. */
    public UpdateMessage timestamp(long timestamp) {
        return timestamp() == timestamp
                ? this
                : update().timestamp(timestamp).build();
    }

    /** Returns the update message sender. */
    public String from() { return from; }

    /** Returns an update message with the given update message sender. */
    public UpdateMessage from(String from) {
        return from().equals(from)
                ? this
                : update().from(from).build();
    }

    /** Returns the update message recipient. */
    public String to() { return to; }

    /** Returns an update message with the given update message recipient. */
    public UpdateMessage to(String to) {
        return to().equals(to)
                ? this
                : update().to(to).build();
    }

    /** Returns the update message type. */
    public Type type() { return type; }

    /** Returns an update message with the given update message type. */
    public UpdateMessage type(Type type) {
        return type().equals(type)
                ? this
                : update().type(type).build();
    }

    /** Returns the artifact descriptor. */
    public ArtifactDescriptor artifactDescriptor() {
        return artifactDescriptor;
    }

    /** Returns an update message with the given artifact descriptor. */
    public UpdateMessage artifactDescriptor(ArtifactDescriptor artifactDescriptor) {
        return artifactDescriptor().equals(artifactDescriptor)
                ? this
                : update().artifactDescriptor(artifactDescriptor).build();
    }

    /** Returns the update version. */
    public String updateVersion() { return updateVersion; }

    /** Returns an update message with the given update version. */
    public UpdateMessage updateVersion(String updateVersion) {
        return updateVersion().equals(updateVersion)
                ? this
                : update().updateVersion(updateVersion).build();
    }

    /** Returns the current location. */
    public String currentLocation() { return currentLocation; }

    /** Returns an update message with the given current location. */
    public UpdateMessage currentLocation(String currentLocation) {
        return currentLocation().equals(currentLocation)
                ? this
                : update().currentLocation(currentLocation).build();
    }

    /**
     * Returns the update location.
     * If this equals {@link #currentLocation()}, then the update should happen
     * in-place.
     */
    public String updateLocation() { return updateLocation; }

    /** Returns an update message with the given update location. */
    public UpdateMessage updateLocation(String newLocation) {
        return updateLocation().equals(newLocation)
                ? this
                : update().updateLocation(newLocation).build();
    }

    /** Returns the status text. */
    public String status() { return status; }

    /** Returns an update message with the given status text. */
    public UpdateMessage status(final String status) {
        return status().equals(status)
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

    /** Vends an application descriptor from this update message. */
    public ApplicationDescriptor applicationDescriptor() {
        try {
            return ApplicationDescriptor
                    .builder()
                    .artifactDescriptor(artifactDescriptor())
                    .currentLocation(currentLocation())
                    .build();
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /** Vends an update descriptor from this update message. */
    public UpdateDescriptor updateDescriptor() {
        try {
            return UpdateDescriptor
                    .builder()
                    .artifactDescriptor(artifactDescriptor())
                    .updateVersion(updateVersion())
                    .build();
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException(ex);
        }
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
                this.updateVersion().equals(that.updateVersion()) &&
                this.currentLocation().equals(that.currentLocation()) &&
                this.updateLocation().equals(that.updateLocation()) &&
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
        hash = 31 * hash + updateVersion().hashCode();
        hash = 31 * hash + currentLocation().hashCode();
        hash = 31 * hash + updateLocation().hashCode();
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
        if (!updateVersion().isEmpty())
            sb.append("Update-Version: ").append(updateVersion()).append('\n');
        if (!currentLocation().isEmpty())
            sb.append("Current-Location: ").append(currentLocation()).append('\n');
        if (!updateLocation().isEmpty())
            sb.append("Update-Location: ").append(updateLocation()).append('\n');
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
     * The update manager then needs to send a
     * {@link #SUBSCRIPTION_SUCCESS_RESPONSE} or a
     * {@link #SUBSCRIPTION_FAILURE_RESPONSE}.
     * <li>
     * Upon a successful subscription, the update manager needs to send an
     * {@link #UPDATE_NOTICE} for every update.
     * <li>
     * The update agent may then send an {@link #INSTALLATION_REQUEST}.
     * <li>
     * The update manager then needs to install the application update and send
     * an {@link #INSTALLATION_SUCCESS_RESPONSE} or an
     * {@link #INSTALLATION_FAILURE_RESPONSE}.
     * <li>
     * The update agent may send an {@link #UNSUBSCRIPTION_REQUEST} or
     * {@link #UNSUBSCRIPTION_NOTICE} in order to unsubscribe from the list of
     * recipients for update announcements for the application.
     * <li>
     * If and only if the message is an {@link #UNSUBSCRIPTION_REQUEST}, then
     * the update manager needs to send an
     * {@link #UNSUBSCRIPTION_SUCCESS_RESPONSE} or an
     * {@link #UNSUBSCRIPTION_FAILURE_RESPONSE}.
     * <li>
     * Finally, the update manager may send itself a
     * {@link #SUBSCRIPTION_NOTICE} for every subscription before shutting down
     * in order to persist them.
     * Upon startup, the manager then needs to process the messages without
     * responding to the update agents.
     * </ol>
     * <p>
     * Note that messages may get lost or duplicated and no timeout is defined.
     */
    public enum Type {

        SUBSCRIPTION_NOTICE {

            @Override public boolean forManager() { return true; }

            @Override void dispatchMessageTo(UpdateMessage message,
                                             UpdateMessageListener listener)
            throws Exception {
                listener.onSubscriptionNotice(message);
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
                                             UpdateMessageListener listener)
            throws Exception {
                listener.onSubscriptionRequest(message);
            }
        },

        SUBSCRIPTION_SUCCESS_RESPONSE {

            @Override public boolean forManager() { return false; }

            @Override void dispatchMessageTo(UpdateMessage message,
                                             UpdateMessageListener listener)
            throws Exception {
                listener.onSubscriptionSuccessResponse(message);
            }
        },

        SUBSCRIPTION_FAILURE_RESPONSE {

            @Override public boolean forManager() { return false; }

            @Override void dispatchMessageTo(UpdateMessage message,
                                             UpdateMessageListener listener)
            throws Exception {
                listener.onSubscriptionFailureResponse(message);
            }
        },

        UPDATE_NOTICE {

            @Override public boolean forManager() { return false; }

            @Override void dispatchMessageTo(UpdateMessage message,
                                             UpdateMessageListener listener)
            throws Exception {
                listener.onUpdateNotice(message);
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
                                             UpdateMessageListener listener)
            throws Exception {
                listener.onInstallationRequest(message);
            }
        },

        INSTALLATION_SUCCESS_RESPONSE {

            @Override public boolean forManager() { return false; }

            @Override void dispatchMessageTo(UpdateMessage message,
                                             UpdateMessageListener listener)
            throws Exception {
                listener.onInstallationSuccessResponse(message);
            }
        },

        INSTALLATION_FAILURE_RESPONSE {

            @Override public boolean forManager() { return false; }

            @Override void dispatchMessageTo(UpdateMessage message,
                                             UpdateMessageListener listener)
            throws Exception {
                listener.onInstallationFailureResponse(message);
            }
        },

        UNSUBSCRIPTION_NOTICE {

            @Override public boolean forManager() { return true; }

            @Override void dispatchMessageTo(UpdateMessage message,
                                             UpdateMessageListener listener)
            throws Exception {
                listener.onUnsubscriptionNotice(message);
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
                                             UpdateMessageListener listener)
            throws Exception {
                listener.onUnsubscriptionRequest(message);
            }
        },

        UNSUBSCRIPTION_SUCCESS_RESPONSE {

            @Override public boolean forManager() { return false; }

            @Override void dispatchMessageTo(UpdateMessage message,
                                             UpdateMessageListener listener)
            throws Exception {
                listener.onUnsubscriptionSuccessResponse(message);
            }
        },

        UNSUBSCRIPTION_FAILURE_RESPONSE {

            @Override public boolean forManager() { return false; }

            @Override void dispatchMessageTo(UpdateMessage message,
                                             UpdateMessageListener listener)
            throws Exception {
                listener.onUnsubscriptionFailureResponse(message);
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
                                        UpdateMessageListener listener)
        throws Exception;
    } // Type

    /**
     * A builder for an update message.
     *
     * @param <P> The type of the parent builder.
     */
    @SuppressWarnings("PackageVisibleField")
    public static final class Builder<P> {

        @CheckForNull Long timestamp;
        @CheckForNull String from, to;
        @CheckForNull Type type;
        @CheckForNull ArtifactDescriptor artifactDescriptor;
        @CheckForNull String updateVersion, status;
        @CheckForNull String currentLocation, updateLocation;

        protected Builder() { }

        public Builder<P> timestamp(final @Nullable Long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder<P> from(final @Nullable String from) {
            this.from = from;
            return this;
        }

        public Builder<P> to(final @Nullable String to) {
            this.to = to;
            return this;
        }

        public Builder<P> type(final @Nullable Type type) {
            this.type = type;
            return this;
        }

        public ArtifactDescriptor.Builder<Builder<P> > artifactDescriptor() {
            return new ArtifactDescriptor.Builder<Builder<P> >() {
                @Override public Builder<P> inject() {
                    return artifactDescriptor(build());
                }
            };
        }

        public Builder<P> artifactDescriptor(
                final @Nullable ArtifactDescriptor artifactDescriptor) {
            this.artifactDescriptor = artifactDescriptor;
            return this;
        }

        public Builder<P> updateVersion(final @Nullable String updateVersion) {
            this.updateVersion = updateVersion;
            return this;
        }

        public Builder<P> currentLocation(final @Nullable String currentLocation) {
            this.currentLocation = currentLocation;
            return this;
        }

        public Builder<P> updateLocation(final @Nullable String updateLocation) {
            this.updateLocation = updateLocation;
            return this;
        }

        public Builder<P> status(final @Nullable String status) {
            this.status = status;
            return this;
        }

        public UpdateMessage build() { return new UpdateMessage(this); }

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
