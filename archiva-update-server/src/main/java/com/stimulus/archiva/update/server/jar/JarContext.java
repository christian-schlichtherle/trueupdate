/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jar;

import com.stimulus.archiva.update.server.jar.diff.JarDiff;

/**
 * @author Christian Schlichtherle
 */
public class JarContext {
    public JarDiff.Builder diff() { return new JarDiff.Builder(); }
}
