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
import net.java.trueupdate.manager.spec.tx.Transaction;
import static net.java.trueupdate.manager.spec.tx.Transactions.*;
import net.java.trueupdate.manager.spec.tx.Transactions.LoggerConfig;
import net.java.trueupdate.message.UpdateDescriptor;
import net.java.trueupdate.message.UpdateMessage;

/**
 * Resolves diff ZIP files for artifact updates and manages their life cycle.
 *
 * @author Christian Schlichtherle
 */
abstract class CoreUpdateResolver {

    private static final Logger logger = Logger.getLogger(
            CoreUpdateResolver.class.getName(),
            UpdateMessage.class.getName());

    private static final LoggerConfig loggerConfig = new LoggerConfig() {
        @Override public Logger logger() { return logger; }
    };

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
    throws Exception {
        final FileAccount account = account(descriptor);
        final File diffZip;
        if (account.fileResolved()) diffZip = account.file();
        else account.file(diffZip = download(descriptor));
        if (logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, "cur.resolved", new Object[] {
                diffZip, descriptor.artifactDescriptor(),
                descriptor.updateVersion()
            });
        }
        return diffZip;
    }

    private File download(final UpdateDescriptor descriptor) throws Exception {

        class DownloadTransaction extends Transaction {

            File diffZip;

            @Override public void prepare() throws Exception {
                diffZip = File.createTempFile("diff", ".zip");
            }

            @Override public void perform() throws Exception {
                final ArtifactDescriptor ad = descriptor.artifactDescriptor();
                final String uv = descriptor.updateVersion();
                Copy.copy(updateClient().diff(ad, uv), new FileStore(diffZip));
            }

            @Override public void rollback() throws Exception {
                diffZip.delete();
            }
        } // DownloadTransaction

        final DownloadTransaction tx = new DownloadTransaction();
        execute(timed("cur.download", tx, loggerConfig));
        return tx.diffZip;
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
            logger.log(Level.INFO, "cur.delete.success", file);
        else
            logger.log(Level.WARNING, "cur.delete.failure", file);
    }
}
