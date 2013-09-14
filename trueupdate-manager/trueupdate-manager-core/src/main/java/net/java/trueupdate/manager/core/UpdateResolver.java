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
import net.java.trueupdate.jaxrs.client.UpdateClient;
import net.java.trueupdate.manager.spec.UpdateDescriptor;

/**
 * Resolves diff ZIP files for artifact updates and manages their life cycle.
 *
 * @author Christian Schlichtherle
 */
abstract class UpdateResolver {

    private static final Logger
            logger = Logger.getLogger(UpdateResolver.class.getName());

    private final Map<UpdateDescriptor, FileAccount>
            accounts = new HashMap<UpdateDescriptor, FileAccount>();

    /** Returns the update client. */
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

    /**
     * Resolves the diff ZIP file for the given update descriptor.
     * Clients must not modify or delete the returned file.
     *
     * @param descriptor the update descriptor.
     */
    final File resolveDiffZip(final UpdateDescriptor descriptor)
    throws IOException {
        final FileAccount account = account(descriptor);
        if (account.fileResolved()) return account.file();
        final ArtifactDescriptor ad = descriptor.artifactDescriptor();
        final String uv = descriptor.updateVersion();
        final File diffZip = File.createTempFile("diff", ".zip");
        try {
            Copy.copy(updateClient().diff(ad, uv), new FileStore(diffZip));
        } catch (final IOException ex) {
            diffZip.delete();
            throw ex;
        }
        logger.log(Level.INFO,
                "Downloaded file {0} for artifact descriptor {1} and update version {2} .",
                new Object[] { diffZip, ad, uv });
        account.file(diffZip);
        return diffZip;
    }

    /**
     * Closes this update resolver.
     * This method is idempotent.
     */
    final void close() {
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
            logger.log(Level.INFO, "Deleted file {0} .", file);
        } else {
            logger.log(Level.WARNING, "Could not delete file {0} .",
                    file);
        }
    }
}
