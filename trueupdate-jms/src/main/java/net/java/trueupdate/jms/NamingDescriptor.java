/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms;

import javax.annotation.*;
import javax.annotation.concurrent.Immutable;
import javax.naming.InitialContext;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.*;
import static net.java.trueupdate.util.Strings.*;


/**
 * Describes a JDNI context.
 * This class implements an immutable value object, so you can easily share it
 * with anyone.
 *
 * @author Christian Schlichtherle
 */
@Immutable
@XmlJavaTypeAdapter(NamingDescriptorAdapter.class)
public final class NamingDescriptor {

    private static final String
            INITIAL_CONTEXT_CLASS = InitialContext.class.getName(),
            LOOKUP = "java:comp/env";

    private final String initialContextClass, lookup;

    NamingDescriptor(final Builder<?> b) {
        this.initialContextClass =
                nonEmptyOr(b.initialContextClass, INITIAL_CONTEXT_CLASS);
        this.lookup = nonEmptyOr(b.lookup, LOOKUP);
    }

    /** Returns a new builder with all properties set from this instance. */
    public Builder<Void> update() {
        return builder()
                .initialContextClass(initialContextClass())
                .lookup(lookup());
    }

    /** Returns a new builder for a naming descriptor. */
    public static Builder<Void> builder() { return new Builder<Void>(); }

    /**
     * Returns the class name of the initial context.
     * The default value is {@code "javax.naming.InitialContext"}.
     */
    public String initialContextClass() { return initialContextClass; }

    /**
     * Returns the context to look up from the initial context.
     * The default value is {@code "java:comp/env"}.
     */
    public String lookup() { return lookup; }

    /**
     * Returns {@code true} if and only if the given object is a
     * {@code NamingDescriptor} with equal properties.
     */
    @Override public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NamingDescriptor)) return false;
        final NamingDescriptor that = (NamingDescriptor) obj;
        return  this.initialContextClass().equals(that.initialContextClass()) &&
                this.lookup().equals(that.lookup());
    }

    /** Returns a hash code which is consistent with {@link #equals(Object)}. */
    @Override public int hashCode() {
        int hash = 17;
        hash = 31 * hash + initialContextClass().hashCode();
        hash = 31 * hash + lookup().hashCode();
        return hash;
    }

    /**
     * A builder for a naming descriptor.
     *
     * @param <P> The type of the parent builder.
     */
    @SuppressWarnings("PackageVisibleField")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Builder<P> {

        @CheckForNull String initialContextClass, lookup;

        protected Builder() { }

        public Builder<P> initialContextClass(final @Nullable String initialContextClass) {
            this.initialContextClass = initialContextClass;
            return this;
        }

        public Builder<P> lookup(final @Nullable String lookup) {
            this.lookup = lookup;
            return this;
        }

        public NamingDescriptor build() {
            return new NamingDescriptor(this);
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
final class NamingDescriptorAdapter
extends XmlAdapter<NamingDescriptor.Builder<?>, NamingDescriptor> {

    @Override
    public NamingDescriptor unmarshal(NamingDescriptor.Builder<?> v)
    throws Exception {
        return v.build();
    }

    @Override
    public NamingDescriptor.Builder<?> marshal(NamingDescriptor v)
    throws Exception {
        return v.update();
    }
}
