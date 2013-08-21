/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.core;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.annotation.concurrent.Immutable;
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

    private final Map<UpdateDescriptor, FileResource>
            resources = new HashMap<>();

    /** Returns the artifact update client. */
    abstract UpdateClient updateClient();

    FileResource resource(final UpdateDescriptor updateDescriptor) {
        final FileResource resource = resources.get(updateDescriptor);
        return null != resource ? resource : new FileResource();
    }

    void resource(final UpdateDescriptor updateDescriptor,
                  final FileResource resource) {
        if (0 < resource.usages()) {
            resources.put(updateDescriptor, resource);
        } else {
            assert 0 == resource.usages();
            resources.remove(updateDescriptor);
        }
    }

    void subscribe(UpdateDescriptor updateDescriptor) {
        new ConfiguredUpdateResolver(updateDescriptor).subscribe();
    }

    @Override public File resolveZipPatchFile(UpdateDescriptor updateDescriptor)
    throws Exception {
        return new ConfiguredUpdateResolver(updateDescriptor)
                .resolveZipPatchFile();
    }

    void unsubscribe(UpdateDescriptor updateDescriptor) {
        new ConfiguredUpdateResolver(updateDescriptor).unsubscribe();
    }

    void shutdown() throws IOException {
        for (final Iterator<FileResource> it = resources.values().iterator();
                it.hasNext(); ) {
            deleteResolvedFile(it.next());
            it.remove();
        }
    }

    static void deleteResolvedFile(final FileResource resource) {
        if (!resource.fileResolved()) return;
        assert 0 <= resource.usages();
        final File file = resource.file();
        if (resource.deleteResolvedFile()) {
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

        FileResource resource() {
            return BasicUpdateResolver.this.resource(updateDescriptor);
        }

        void resource(FileResource resource) {
            BasicUpdateResolver.this.resource(updateDescriptor, resource);
        }

        void subscribe() {
            resource(resource().allocate());
        }

        File resolveZipPatchFile() throws Exception {
            final FileResource resource = resource();
            if (resource.fileResolved()) return resource.file();
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
            resource(resource.file(patch));
            return patch;
        }

        void unsubscribe() {
            final FileResource resource = resource().release();
            resource(resource);
            if (0 >= resource.usages()) {
                assert 0 == resource.usages();
                deleteResolvedFile(resource);
            }
        }
    } // ConfiguredUpdateResolver
}

@Immutable
final class FileResource {

    private final File file;
    private final int usages;

    FileResource() { this(new File(""), 0); }

    FileResource(final File file, final int usages) {
        assert null != file;
        this.file = file;
        assert 0 <= usages;
        this.usages = usages;
    }

    boolean fileResolved() { return !file().getPath().isEmpty(); }

    boolean deleteResolvedFile() { return fileResolved() && file().delete(); }

    File file() { return file; }

    FileResource file(File file) { return new FileResource(file, usages); }

    FileResource allocate() { return usages(usages() + 1); }

    FileResource release() { return usages(usages() - 1); }

    int usages() { return usages; }

    FileResource usages(int usages) { return new FileResource(file, usages); }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof FileResource)) return false;
        final FileResource that = (FileResource) obj;
        return  this.file().equals(that.file()) &&
                this.usages() == that.usages();
    }

    @Override public int hashCode() {
        int hash = 17;
        hash = 31 * hash + file().hashCode();
        hash = 31 * hash + usages();
        return hash;
    }
}
