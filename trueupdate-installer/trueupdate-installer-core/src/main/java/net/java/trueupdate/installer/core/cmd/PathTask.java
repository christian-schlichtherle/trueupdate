/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.core.cmd;

import java.io.File;
import net.java.trueupdate.core.io.Task;

/**
 * A task which is executed on a file system {@linkplain File path}.
 *
 * @author Christian Schlichtherle
 */
public interface PathTask<V, X extends Exception>
extends Task<V, File, X> { }
