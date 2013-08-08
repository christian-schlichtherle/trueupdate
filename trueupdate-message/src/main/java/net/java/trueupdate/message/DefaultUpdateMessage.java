/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.message;

import java.io.Serializable;
import static java.util.Objects.requireNonNull;
import javax.annotation.concurrent.Immutable;

/**
 * The default implementation of an update message.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public final class DefaultUpdateMessage<B extends Serializable>
extends UpdateMessage<B> implements Serializable {

    private static final long serialVersionUID = 0L;

    private final Type<B> type;
    private final B body;

    public DefaultUpdateMessage(final Type<B> type, final B body) {
        this.type = requireNonNull(type);
        this.body = requireNonNull(body);
    }

    @Override public final Type<B> type() { return type; }

    @Override public final B body() { return body; }
}
