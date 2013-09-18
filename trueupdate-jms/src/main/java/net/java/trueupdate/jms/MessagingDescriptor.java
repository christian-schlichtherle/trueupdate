/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms;

import javax.annotation.*;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.*;
import net.java.trueupdate.util.Objects;
import static net.java.trueupdate.util.Strings.*;


/**
 * Describes some JMS administered objects.
 * This class implements an immutable value object, so you can easily share it
 * with anyone.
 *
 * @author Christian Schlichtherle
 */
@Immutable
@XmlJavaTypeAdapter(MessagingDescriptorAdapter.class)
public final class MessagingDescriptor {

    private final String connectionFactory;
    private final @CheckForNull String agent, manager;

    MessagingDescriptor(final Builder<?> b) {
        this.connectionFactory = requireNonEmpty(b.connectionFactory);
        this.agent = requireNullOrNonEmpty(b.agent);
        this.manager = requireNullOrNonEmpty(b.manager);
    }

    private static String requireNullOrNonEmpty(final @CheckForNull String s) {
        if (null != s && s.isEmpty()) throw new IllegalArgumentException();
        return s;
    }

    /** Returns a new builder with all properties set from this instance. */
    public Builder<Void> update() {
        return builder()
                .connectionFactory(connectionFactory())
                .agent(agent())
                .manager(manager());
    }

    /** Returns a new builder for a messaging descriptor. */
    public static Builder<Void> builder() { return new Builder<Void>(); }

    /** Returns the JNDI name of the connection factory. */
    public String connectionFactory() { return connectionFactory; }

    /** Returns the nullable JNDI name of the destination for the update agent. */
    public @Nullable String agent() { return agent; }

    /** Returns the nullable JNDI name of the destination for the update manager. */
    public @Nullable String manager() { return manager; }

    /**
     * Returns {@code true} if and only if the given object is a
     * {@code MessagingDescriptor} with equal properties.
     */
    @Override public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MessagingDescriptor)) return false;
        final MessagingDescriptor that = (MessagingDescriptor) obj;
        return  this.connectionFactory().equals(that.connectionFactory()) &&
                Objects.equals(this.agent(), that.agent()) &&
                Objects.equals(this.manager(), that.manager());
    }

    /** Returns a hash code which is consistent with {@link #equals(Object)}. */
    @Override public int hashCode() {
        int hash = 17;
        hash = 31 * hash + connectionFactory().hashCode();
        hash = 31 * hash + Objects.hashCode(agent());
        hash = 31 * hash + Objects.hashCode(manager());
        return hash;
    }

    /**
     * A builder for a messaging descriptor.
     *
     * @param <P> The type of the parent builder.
     */
    @SuppressWarnings("PackageVisibleField")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Builder<P> {

        @CheckForNull String connectionFactory, agent, manager;

        protected Builder() { }

        public Builder<P> connectionFactory(final @Nullable String connectionFactory) {
            this.connectionFactory = connectionFactory;
            return this;
        }

        public Builder<P> agent(final @Nullable String agent) {
            this.agent = agent;
            return this;
        }

        public Builder<P> manager(final @Nullable String manager) {
            this.manager = manager;
            return this;
        }

        public MessagingDescriptor build() {
            return new MessagingDescriptor(this);
        }

        /**
         * Injects the product of this builder into the parent builder, if
         * defined.
         *
         * @throws IllegalStateException if there is no parent builder defined.
         */
        public P inject() {
            throw new java.lang.IllegalStateException("No parent builder defined.");
        }
    } // Builder
}

@Immutable
final class MessagingDescriptorAdapter
extends XmlAdapter<MessagingDescriptor.Builder<?>, MessagingDescriptor> {

    @Override
    public MessagingDescriptor unmarshal(MessagingDescriptor.Builder<?> v)
    throws Exception {
        return v.build();
    }

    @Override
    public MessagingDescriptor.Builder<?> marshal(MessagingDescriptor v)
    throws Exception {
        return v.update();
    }
}
