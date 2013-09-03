/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.core.io;

import java.io.File;
import net.java.trueupdate.core.io.Task;

/**
 * A task which is executed on a given file.
 *
 * @author Christian Schlichtherle
 */
public interface FileTask<V, X extends Exception>
extends Task<V, File, X> { }
