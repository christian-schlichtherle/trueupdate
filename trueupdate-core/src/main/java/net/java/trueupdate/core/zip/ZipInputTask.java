/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip;

import java.util.zip.ZipFile;
import javax.annotation.WillNotClose;
import net.java.trueupdate.core.io.Task;

/**
 * Executes a task on a {@link ZipFile}.
 *
 * @see ZipSources#execute
 * @see ZipSources#bind
 * @author Christian Schlichtherle
 */
public interface ZipInputTask<V, X extends Exception>
extends Task<V, ZipInput, X> {

    @Override V execute(@WillNotClose ZipInput resource) throws X;
}
