/*
 * Copyright (C) 2012-2013 Schlichtherle IT Services.
 * Copyright (C) 2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.annotation.CheckForNull;
import javax.annotation.concurrent.Immutable;

/**
 * Thrown to indicate that an error happened on the input side rather than the
 * output side when copying an {@link InputStream} to an {@link OutputStream}.
 *
 * @see Copy#copy(Source, Sink)
 * @author Christian Schlichtherle (copied and edited from TrueCommons I/O 2.3.2)
 */
@Immutable
public class InputException extends IOException {

    private static final long serialVersionUID = 0L;

    public InputException(@CheckForNull Throwable cause) { super(cause); }
}
