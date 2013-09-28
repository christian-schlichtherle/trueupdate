/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.message;

import java.util.*;
import java.util.logging.LogRecord;
import javax.annotation.*;
import net.java.trueupdate.artifact.spec.*;
import static net.java.trueupdate.util.Objects.*;
import static net.java.trueupdate.util.Strings.requireNonEmpty;
import net.java.trueupdate.util.builder.*;

/**
 * An update message encapsulates the data which gets exchanged between update
 * agents and update managers in order to establish a protocol for the
 * automated installation of artifact updates.
 * This class implements an immutable value object, so you can easily share its
 * instances with anyone or use them as map keys.
 * There is one exception though: Each update message has a mutable list of
 * attached, mutable {@link LogRecord}s - see {@link #attachedLogs()}.
 * This list is not considered to be a property and thus it does not
 * participate in {@link #equals} nor {@link #hashCode}.
 * Furthermore, any transformer method will return a new update message with a
 * new, empty list of attached log records.
 *
 * @see Type
 * @author Christian Schlichtherle
 */
public final class UpdateMessage {

    private final long timestamp;
    private final String from, to;
    private final Type type;
    private final ArtifactDescriptor artifactDescriptor;
    private final String updateVersion;
    private final String currentLocation, updateLocation;

    private final List<LogRecord> attachedLogs = new LinkedList<LogRecord>();

    @SuppressWarnings("unchecked")
    UpdateMessage(final Builder<?> b) {
        this.timestamp = nonNullOrNow(b.timestamp);
        this.from = requireNonEmpty(b.from);
        this.to = requireNonEmpty(b.to);
        this.type = requireNonNull(b.type);
        this.artifactDescriptor = requireNonNull(b.artifactDescriptor);
        this.updateVersion = nonNullOr(b.updateVersion, "");
        this.currentLocation = requireNonEmpty(b.currentLocation);
        this.updateLocation = nonNullOr(b.updateLocation, currentLocation);
    }

    private static long nonNullOrNow(Long timestamp) {
        return null != timestamp ? timestamp : System.currentTimeMillis();
    }

    /** Returns a new builder with all properties set from this instance. */
    public Builder<Void> update() {
        return builder()
                .timestamp(timestamp)
                .from(from)
                .to(to)
                .type(type)
                .artifactDescriptor(artifactDescriptor)
                .updateVersion(updateVersion)
                .currentLocation(currentLocation)
                .updateLocation(updateLocation);
    }

    /**
     * Returns a new builder for an update message.
     * The default value for the property {@code timestamp} is the creation
     * time of the update message in milliseconds since the epoch.
     * The default value for the property {@code updateVersion} is an empty
     * string.
     * The default value for the property {@code updateLocation} is the
     * effective value of the property {@code currentLocation}.
     */
    public static Builder<Void> builder() { return new Builder<Void>(); }

    /** Returns the update message timestamp. */
    public long timestamp() { return timestamp; }

    /** Returns an update message with the given update message timestamp. */
    public UpdateMessage timestamp(long timestamp) {
        return timestamp == this.timestamp
                ? this
                : update().timestamp(timestamp).build();
    }

    /** Returns the update message sender. */
    public String from() { return from; }

    /** Returns an update message with the given update message sender. */
    public UpdateMessage from(String from) {
        return this.from.equals(from)
                ? this
                : update().from(from).build();
    }

    /** Returns the update message recipient. */
    public String to() { return to; }

    /** Returns an update message with the given update message recipient. */
    public UpdateMessage to(String to) {
        return to.equals(this.to)
                ? this
                : update().to(to).build();
    }

    /** Returns the update message type. */
    public Type type() { return type; }

    /** Returns an update message with the given update message type. */
    public UpdateMessage type(Type type) {
        return type.equals(this.type)
                ? this
                : update().type(type).build();
    }

    /** Returns the artifact descriptor. */
    public ArtifactDescriptor artifactDescriptor() {
        return artifactDescriptor;
    }

    /** Returns an update message with the given artifact descriptor. */
    public UpdateMessage artifactDescriptor(ArtifactDescriptor artifactDescriptor) {
        return artifactDescriptor.equals(this.artifactDescriptor)
                ? this
                : update().artifactDescriptor(artifactDescriptor).build();
    }

    /** Returns the update version. */
    public String updateVersion() { return updateVersion; }

    /** Returns an update message with the given update version. */
    public UpdateMessage updateVersion(String updateVersion) {
        return updateVersion.equals(this.updateVersion)
                ? this
                : update().updateVersion(updateVersion).build();
    }

    /** Returns the current location. */
    public String currentLocation() { return currentLocation; }

    /** Returns an update message with the given current location. */
    public UpdateMessage currentLocation(String currentLocation) {
        return currentLocation.equals(this.currentLocation)
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
    public UpdateMessage updateLocation(String updateLocation) {
        return updateLocation.equals(this.updateLocation)
                ? this
                : update().updateLocation(updateLocation).build();
    }

    /**
     * Returns the mutable list of the attached, mutable log records.
     * Note that the returned list is shared with this instance.
     * Furthermore, each transformer method in this class returns a new update
     * message with a new, empty list.
     */
    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public List<LogRecord> attachedLogs() { return attachedLogs; }

    /**
     * Returns {@code true} if and only if the given object is an
     * {@code UpdateMessage} with equal properties.
     */
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    @Override public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof UpdateMessage)) return false;
        final UpdateMessage that = (UpdateMessage) obj;
        return  this.timestamp == that.timestamp &&
                this.from.equals(that.from) &&
                this.to.equals(that.to) &&
                this.type.equals(that.type) &&
                this.artifactDescriptor.equals(that.artifactDescriptor) &&
                this.updateVersion.equals(that.updateVersion) &&
                this.currentLocation.equals(that.currentLocation) &&
                this.updateLocation.equals(that.updateLocation);
    }

    /** Returns a hash code which is consistent with {@link #equals(Object)}. */
    @Override public int hashCode() {
        int hash = 17;
        hash = 31 * hash + hashCode(timestamp);
        hash = 31 * hash + from.hashCode();
        hash = 31 * hash + to.hashCode();
        hash = 31 * hash + type.hashCode();
        hash = 31 * hash + artifactDescriptor.hashCode();
        hash = 31 * hash + updateVersion.hashCode();
        hash = 31 * hash + currentLocation.hashCode();
        hash = 31 * hash + updateLocation.hashCode();
        return hash;
    }

    private static int hashCode(long value) {
        return (int) ((value >> 32) ^ value);
    }

    /** Returns a human readable string representation of this object. */
    @Override public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb      .append("Timestamp: ").append(new Date(timestamp)).append('\n')
                .append("From: ").append(from).append('\n')
                .append("To: ").append(to).append('\n')
                .append("Type: ").append(type).append('\n')
                .append("Artifact-descriptor: ").append(artifactDescriptor).append('\n');
        if (!updateVersion.isEmpty())
            sb.append("Update-version: ").append(updateVersion).append('\n');
        if (!currentLocation.isEmpty())
            sb.append("Current-location: ").append(currentLocation).append('\n');
        if (!updateLocation.isEmpty())
            sb.append("Update-location: ").append(updateLocation).append('\n');
        sb.append("Attached-log-records: ").append(attachedLogs.size());
        return sb.toString();
    }

    /**
     * The update message type.
     * <p>
     * Note that messages may get lost or duplicated and in general, no timeout
     * is defined.
     * <p>
     * Note that new types must be <em>strictly</em> added to the <em>end<em>
     * because the serialization may depend on the enum ordinal!
     */
    public enum Type {

        SUBSCRIPTION_NOTICE(true) {
            @Override void dispatchMessageTo(UpdateMessage message,
                                             UpdateMessageListener listener)
            throws Exception {
                listener.onSubscriptionNotice(message);
            }
        },

        SUBSCRIPTION_REQUEST(true) {
            @Override void dispatchMessageTo(UpdateMessage message,
                                             UpdateMessageListener listener)
            throws Exception {
                listener.onSubscriptionRequest(message);
            }
        },

        SUBSCRIPTION_RESPONSE(false) {
            @Override void dispatchMessageTo(UpdateMessage message,
                                             UpdateMessageListener listener)
            throws Exception {
                listener.onSubscriptionResponse(message);
            }
        },

        UPDATE_NOTICE(false) {
            @Override void dispatchMessageTo(UpdateMessage message,
                                             UpdateMessageListener listener)
            throws Exception {
                listener.onUpdateNotice(message);
            }
        },

        INSTALLATION_REQUEST(true) {
            @Override void dispatchMessageTo(UpdateMessage message,
                                             UpdateMessageListener listener)
            throws Exception {
                listener.onInstallationRequest(message);
            }
        },

        PROGRESS_NOTICE(false) {
            @Override void dispatchMessageTo(UpdateMessage message,
                                             UpdateMessageListener listener)
            throws Exception {
                listener.onProgressNotice(message);
            }
        },

        REDEPLOYMENT_REQUEST(false) {
            @Override void dispatchMessageTo(UpdateMessage message,
                                             UpdateMessageListener listener)
            throws Exception {
                listener.onRedeploymentRequest(message);
            }
        },

        PROCEED_REDEPLOYMENT_RESPONSE(true) {
            @Override void dispatchMessageTo(UpdateMessage message,
                                             UpdateMessageListener listener)
            throws Exception {
                listener.onProceedRedeploymentResponse(message);
            }
        },

        CANCEL_REDEPLOYMENT_RESPONSE(true) {
            @Override void dispatchMessageTo(UpdateMessage message,
                                             UpdateMessageListener listener)
            throws Exception {
                listener.onCancelRedeploymentResponse(message);
            }
        },

        INSTALLATION_SUCCESS_RESPONSE(false) {
            @Override void dispatchMessageTo(UpdateMessage message,
                                             UpdateMessageListener listener)
            throws Exception {
                listener.onInstallationSuccessResponse(message);
            }
        },

        INSTALLATION_FAILURE_RESPONSE(false) {
            @Override void dispatchMessageTo(UpdateMessage message,
                                             UpdateMessageListener listener)
            throws Exception {
                listener.onInstallationFailureResponse(message);
            }
        },

        UNSUBSCRIPTION_NOTICE(true) {
            @Override void dispatchMessageTo(UpdateMessage message,
                                             UpdateMessageListener listener)
            throws Exception {
                listener.onUnsubscriptionNotice(message);
            }
        };

        private final boolean forManager;

        Type(final boolean forManager) { this.forManager = forManager; }

        /**
         * Returns {@code true} if and only if messages of this type should be
         * processed by an update manager.
         */
        public final boolean forManager() { return forManager; }

        abstract void dispatchMessageTo(UpdateMessage message,
                                        UpdateMessageListener listener)
        throws Exception;
    } // Type

    /**
     * A builder for an update message.
     *
     * @param <P> The type of the parent builder, if defined.
     */
    @SuppressWarnings("PackageVisibleField")
    public static class Builder<P> extends AbstractBuilder<P> {

        @CheckForNull Long timestamp;
        @CheckForNull String from, to;
        @CheckForNull Type type;
        @CheckForNull ArtifactDescriptor artifactDescriptor;
        @CheckForNull String updateVersion;
        @CheckForNull String currentLocation, updateLocation;

        protected Builder() { }

        public final Builder<P> timestamp(final @Nullable Long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public final Builder<P> from(final @Nullable String from) {
            this.from = from;
            return this;
        }

        public final Builder<P> to(final @Nullable String to) {
            this.to = to;
            return this;
        }

        public final Builder<P> type(final @Nullable Type type) {
            this.type = type;
            return this;
        }

        public final ArtifactDescriptor.Builder<Builder<P> > artifactDescriptor() {
            return new ArtifactDescriptor.Builder<Builder<P>>() {
                @Override public Builder<P> inject() {
                    return artifactDescriptor(build());
                }
            };
        }

        public final Builder<P> artifactDescriptor(
                final @Nullable ArtifactDescriptor artifactDescriptor) {
            this.artifactDescriptor = artifactDescriptor;
            return this;
        }

        public final Builder<P> updateVersion(
                final @Nullable String updateVersion) {
            this.updateVersion = updateVersion;
            return this;
        }

        public final Builder<P> currentLocation(
                final @Nullable String currentLocation) {
            this.currentLocation = currentLocation;
            return this;
        }

        public final Builder<P> updateLocation(
                final @Nullable String updateLocation) {
            this.updateLocation = updateLocation;
            return this;
        }

        @Override public final UpdateMessage build() {
            return new UpdateMessage(this);
        }
    } // Builder
}
