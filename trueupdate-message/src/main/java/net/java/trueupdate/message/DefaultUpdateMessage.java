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
public final class DefaultUpdateMessage
extends UpdateMessage implements Serializable {

    private static final long serialVersionUID = 0L;

    private final Type type;
    private final String text;

    public DefaultUpdateMessage(final Type type, final String text) {
        this.type = requireNonNull(type);
        this.text = requireNonNull(text);
    }

    @Override public final Type type() { return type; }

    @Override public final String text() { return text; }
}
