/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.zip;

import java.util.jar.*;
import java.util.zip.ZipEntry;
import javax.annotation.WillCloseWhenClosed;

/**
 * Adapts a {@link JarOutputStream} to a {@link ZipOutput}.
 *
 * @author Christian Schlichtherle
 */
public class JarOutputStreamAdapter extends ZipOutputStreamAdapter {

    public JarOutputStreamAdapter(@WillCloseWhenClosed JarOutputStream jar) {
        super(jar);
    }

    @Override public ZipEntry entry(String name) { return new JarEntry(name); }
}
