/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.core;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.artifact.api.ArtifactDescriptor;
import net.java.trueupdate.core.io.*;
import net.java.trueupdate.jax.rs.client.UpdateClient;
import net.java.trueupdate.manager.api.UpdateDescriptor;

/**
 * Resolves ZIP patch files for artifact updates and manages their life cycle.
 *
 * @author Christian Schlichtherle
 */
abstract class BasicUpdateResolver implements UpdateResolver {

    private static final Logger
            logger = Logger.getLogger(BasicUpdateResolver.class.getName());

    private final Map<UpdateDescriptor, FileAndCount>
            subscriptions = new HashMap<>();

    /** Returns the artifact update client. */
    abstract UpdateClient updateClient();

    FileAndCount fileAndCount(final UpdateDescriptor updateDescriptor) {
        final FileAndCount fac = subscriptions.get(updateDescriptor);
        return null != fac ? fac : new FileAndCount();
    }

    void fileAndCount(final UpdateDescriptor updateDescriptor,
                      final FileAndCount fac) {
        if (0 < fac.count()) {
            subscriptions.put(updateDescriptor, fac);
        } else {
            assert 0 == fac.count();
            subscriptions.remove(updateDescriptor);
        }
    }

    void subscribe(UpdateDescriptor updateDescriptor) {
        new ConfiguredUpdateResolver(updateDescriptor).subscribe();
    }

    @Override public File resolve(UpdateDescriptor updateDescriptor)
    throws Exception {
        return new ConfiguredUpdateResolver(updateDescriptor).resolve();
    }

    void unsubscribe(UpdateDescriptor updateDescriptor) {
        new ConfiguredUpdateResolver(updateDescriptor).unsubscribe();
    }

    void shutdown() throws IOException {
        for (final Iterator<FileAndCount> it = subscriptions.values().iterator();
                it.hasNext(); ) {
            deleteResolvedFile(it.next());
            it.remove();
        }
    }

    static void deleteResolvedFile(final FileAndCount fac) {
        if (!fac.fileResolved()) return;
        assert 0 <= fac.count();
        final File file = fac.file();
        if (fac.deleteResolvedFile()) {
            logger.log(Level.INFO, "Deleted ZIP patch file {0}.", file);
        } else {
            logger.log(Level.WARNING, "Could not delete ZIP patch file {0}.",
                    file);
        }
    }

    private class ConfiguredUpdateResolver {

        final UpdateDescriptor updateDescriptor;

        ConfiguredUpdateResolver(final UpdateDescriptor updateDescriptor) {
            this.updateDescriptor = Objects.requireNonNull(updateDescriptor);
        }

        FileAndCount fileAndCount() {
            return BasicUpdateResolver.this.fileAndCount(updateDescriptor);
        }

        void fileAndCount(FileAndCount fac) {
            BasicUpdateResolver.this.fileAndCount(updateDescriptor, fac);
        }

        void subscribe() {
            final FileAndCount fac = fileAndCount();
            fileAndCount(fac.count(fac.count() + 1));
        }

        File resolve() throws Exception {
            final FileAndCount fac = fileAndCount();
            if (fac.fileResolved()) return fac.file();
            final ArtifactDescriptor ad = updateDescriptor.artifactDescriptor();
            final String uv = updateDescriptor.updateVersion();
            final File patch = File.createTempFile("patch", ".zip");
            try {
                Copy.copy(updateClient().diff(ad, uv), new FileStore(patch));
            } catch (final IOException ex) {
                patch.delete();
                throw ex;
            }
            logger.log(Level.INFO,
                    "Downloaded ZIP patch file {0} for artifact {1} and update version {2}.",
                    new Object[] { patch, ad, uv });
            fileAndCount(fac.file(patch));
            return patch;
        }

        void unsubscribe() {
            FileAndCount fac = fileAndCount();
            fileAndCount(fac = fac.count(fac.count() - 1));
            if (0 >= fac.count()) {
                assert 0 == fac.count();
                deleteResolvedFile(fac);
            }
        }
    } // ConfiguredUpdateResolver
}
@Immutable
final class FileAndCount {

    private final File file;
    private final int count;

    FileAndCount() { this(new File(""), 0); }

    FileAndCount(final File file, final int count) {
        assert null != file;
        this.file = file;
        assert 0 <= count;
        this.count = count;
    }

    boolean fileResolved() { return !file().getPath().isEmpty(); }

    boolean deleteResolvedFile() { return fileResolved() && file().delete(); }

    File file() { return file; }

    FileAndCount file(File file) { return new FileAndCount(file, count); }

    int count() { return count; }

    FileAndCount count(int count) { return new FileAndCount(file, count); }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof FileAndCount)) return false;
        final FileAndCount that = (FileAndCount) obj;
        return  this.file().equals(that.file()) &&
                this.count() == that.count();
    }

    @Override public int hashCode() {
        int hash = 17;
        hash = 31 * hash + file().hashCode();
        hash = 31 * hash + count();
        return hash;
    }
}
