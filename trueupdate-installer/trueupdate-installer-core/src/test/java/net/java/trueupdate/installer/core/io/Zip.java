/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.core.io;

import java.io.File;
import java.util.Locale;

/**
 * @author Christian Schlichtherle
 */
public class Zip {

    public static void main(final String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: Zip <zipFile> <fileOrDirectory>");
            System.exit(1);
        }
        final File zipFile = new File(args[0]);
        final File fileOrDirectory = new File(args[1]);
        final long started = System.currentTimeMillis();
        Files.zip(zipFile, fileOrDirectory, fileOrDirectory.getName());
        final long duration = System.currentTimeMillis() - started;
        System.out.printf(Locale.ENGLISH, "Written ZIP file in %.3f seconds.\n", duration / 1000.0);
    }
}
