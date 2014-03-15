/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.core;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import net.java.trueupdate.artifact.spec.ArtifactDescriptor;
import net.java.trueupdate.core.io.*;
import net.java.trueupdate.jaxrs.client.UpdateClient;
import net.java.trueupdate.manager.spec.*;
import net.java.trueupdate.manager.spec.tx.*;
import net.java.trueupdate.message.UpdateMessage;

/**
 * Resolves delta ZIP files for artifact updates and manages their life cycle.
 *
 * @author Christian Schlichtherle
 */
abstract class CoreUpdateResolver {

    private static final Logger logger = Logger.getLogger(
            CoreUpdateResolver.class.getName(),
            UpdateMessage.class.getName());

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
     * Resolves the delta ZIP file for the given update context.
     * Clients must not modify or delete the returned file.
     *
     * @param context the update context.
     */
    final File resolve(final UpdateDescriptor descriptor,
                       final UpdateContext context)
    throws Exception {
        final FileAccount account = account(descriptor);
        final File deltaZip;
        if (account.fileResolved()) deltaZip = account.file();
        else account.file(deltaZip = download(descriptor, context));
        logger.log(Level.INFO, "resolver.resolved", new Object[] {
            deltaZip, descriptor.artifactDescriptor(),
            descriptor.updateVersion()
        });
        return deltaZip;
    }

    private File download(final UpdateDescriptor descriptor,
                          final UpdateContext context) throws Exception {

        class DownloadTransaction extends Transaction {

            File deltaZip;

            @Override public void prepare() throws Exception {
                deltaZip = File.createTempFile("delta", ".zip");
            }

            @Override public void perform() throws Exception {
                final ArtifactDescriptor ad = descriptor.artifactDescriptor();
                final String uv = descriptor.updateVersion();
                Copy.copy(updateClient().diff(ad, uv), new FileStore(deltaZip));
            }

            @Override public void rollback() throws Exception {
                if (!deltaZip.delete() && deltaZip.exists())
                    throw new IOException(deltaZip + " (could not delete)");
            }
        } // DownloadTransaction

        final DownloadTransaction tx = new DownloadTransaction();
        Transactions.execute(context.decorate(Action.DOWNLOAD, tx));
        return tx.deltaZip;
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
        if (file.delete())
            logger.log(Level.INFO, "resolver.delete.success", file);
        else
            logger.log(Level.WARNING, "resolver.delete.failure", file);
    }
}
