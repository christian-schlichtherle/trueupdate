/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.core;

import java.io.File;

/**
 * @author Christian Schlichtherle
 */
final class FileAccount {

    private File file = new File("");
    private int usages;

    boolean fileResolved() { return file().isFile(); }

    File file() { return file; }

    void file(File file) { this.file = file; }

    int usages() { return usages; }

    int incrementUsagesAndGet() { return ++usages; }

    int decrementUsagesAndGet() { return --usages; }

    void resetUsages() { usages = 0; }
}
