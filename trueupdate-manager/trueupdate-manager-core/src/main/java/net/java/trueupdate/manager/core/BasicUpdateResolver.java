/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.core;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import net.java.trueupdate.artifact.spec.ArtifactDescriptor;
import net.java.trueupdate.core.io.*;
import net.java.trueupdate.jax.rs.client.UpdateClient;
import net.java.trueupdate.manager.spec.UpdateDescriptor;

/**
 * Resolves ZIP patch files for artifact updates and manages their life cycle.
 *
 * @author Christian Schlichtherle
 */
abstract class BasicUpdateResolver implements UpdateResolver {

    private static final Logger
            logger = Logger.getLogger(BasicUpdateResolver.class.getName());

    private final Map<UpdateDescriptor, FileAccount>
            accounts = new HashMap<UpdateDescriptor, FileAccount>();

    /** Returns the artifact update client. */
    abstract UpdateClient updateClient();

    final void restart() {
        for (FileAccount account : accounts.values()) account.resetUsages();
    }

    final void allocate(UpdateDescriptor descriptor) {
        account(descriptor).incrementUsagesAndGet();
    }

    private FileAccount account(final UpdateDescriptor descriptor) {
        FileAccount account = accounts.get(descriptor);
        if (null == account) {
            account = new FileAccount();
            accounts.put(descriptor, account);
        }
        return account;
    }

    final void release(UpdateDescriptor descriptor) {
        final FileAccount account = account(descriptor);
        final int usages = account.decrementUsagesAndGet();
        if (0 >= usages) {
            assert 0 == usages;
            accounts.remove(descriptor);
            deleteResolvedFile(account);
        }
    }

    @Override public final File resolveZipPatchFile(UpdateDescriptor descriptor)
    throws Exception {
        final FileAccount account = account(descriptor);
        if (account.fileResolved()) return account.file();
        final ArtifactDescriptor ad = descriptor.artifactDescriptor();
        final String uv = descriptor.updateVersion();
        final File patch = File.createTempFile("patch", ".zip");
        try {
            Copy.copy(updateClient().diff(ad, uv), new FileStore(patch));
        } catch (final IOException ex) {
            patch.delete();
            throw ex;
        }
        logger.log(Level.INFO,
                "Downloaded ZIP patch file {0} for artifact descriptor {1} and update version {2} .",
                new Object[] { patch, ad, uv });
        account.file(patch);
        return patch;
    }

    final void shutdown() {
        for (final Iterator<FileAccount> it = accounts.values().iterator();
                it.hasNext(); ) {
            deleteResolvedFile(it.next());
            it.remove();
        }
    }

    private static void deleteResolvedFile(final FileAccount account) {
        if (!account.fileResolved()) return;
        assert 0 <= account.usages();
        final File file = account.file();
        if (file.delete()) {
            logger.log(Level.INFO, "Deleted ZIP patch file {0}.", file);
        } else {
            logger.log(Level.WARNING, "Could not delete ZIP patch file {0}.",
                    file);
        }
    }
}

final class FileAccount {

    private File file = new File("");
    private int usages;

    boolean fileResolved() { return !file().getPath().isEmpty(); }

    File file() { return file; }

    void file(File file) { this.file = file; }

    int usages() { return usages; }

    int incrementUsagesAndGet() { return ++usages; }

    int decrementUsagesAndGet() { return --usages; }

    void resetUsages() { usages = 0; }
}
