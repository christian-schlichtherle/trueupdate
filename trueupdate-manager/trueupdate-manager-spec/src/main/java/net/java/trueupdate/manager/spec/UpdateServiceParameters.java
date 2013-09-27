/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.spec;

import java.net.URI;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.manager.spec.dto.UpdateServiceParametersDto;
import net.java.trueupdate.util.Objects;
import static net.java.trueupdate.util.SystemProperties.resolve;
import net.java.trueupdate.util.builder.AbstractBuilder;

/**
 * Update service parameters.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class UpdateServiceParameters {

    private final URI uri;

    UpdateServiceParameters(final Builder<?> b) {
        this.uri = Objects.requireNonNull(b.uri);
    }

    /** Parses the given configuration item. */
    public static UpdateServiceParameters parse(UpdateServiceParametersDto ci) {
        return builder().parse(ci).build();
    }

    /** Returns a new builder for update service parameters. */
    public static Builder<Void> builder() { return new Builder<Void>(); }

    /** Returns the base URI of the update service. */
    public URI uri() { return uri; }

    /**
     * A builder for update service parameters.
     *
     * @param <P> The type of the parent builder, if defined.
     */
    @SuppressWarnings("PackageVisibleField")
    public static class Builder<P> extends AbstractBuilder<P> {

        URI uri;

        protected Builder() { }

        /** Selectively parses the given configuration item. */
        public final Builder<P> parse(final UpdateServiceParametersDto ci) {
            if (null != ci.uri)
                this.uri = URI.create(ensureEndsWithSlash(resolve(ci.uri)));
            return this;
        }

        private static String ensureEndsWithSlash(String string) {
            return string.endsWith("/") ? string : string + "/";
        }

        public final Builder<P> uri(final @Nullable URI uri) {
            this.uri = uri;
            return this;
        }

        @Override public UpdateServiceParameters build() {
            return new UpdateServiceParameters(this);
        }
    } // Builder
}
