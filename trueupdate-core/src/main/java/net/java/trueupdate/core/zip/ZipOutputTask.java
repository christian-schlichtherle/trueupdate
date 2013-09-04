/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip;

import java.util.zip.ZipOutputStream;
import net.java.trueupdate.core.io.Task;

/**
 * Executes a task on a {@link ZipOutputStream}.
 *
 * @see ZipSinks#execute
 * @see ZipSinks#bind
 * @author Christian Schlichtherle
 */
public interface ZipOutputTask<V, X extends Exception>
extends Task<V, ZipOutput, X> { }
