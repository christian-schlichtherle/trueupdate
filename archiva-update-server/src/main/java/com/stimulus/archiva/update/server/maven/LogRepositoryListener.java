/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.maven;

import java.util.Locale;
import javax.annotation.concurrent.Immutable;
import org.eclipse.aether.*;
import org.eclipse.aether.spi.log.*;

/**
 * A repository listener which logs events.
 *
 * @author Christian Schlichtherle
 */
@Immutable
final class LogRepositoryListener extends AbstractRepositoryListener {

    private final Logger logger;

    LogRepositoryListener(LoggerFactory factory) {
        this.logger = factory.getLogger(LogRepositoryListener.class.getName());
    }

    @Override public void artifactDescriptorInvalid(RepositoryEvent event) {
        warn(event.getException(), "Invalid artifact descriptor: %s.", event.getArtifact());
    }

    @Override public void artifactDescriptorMissing(RepositoryEvent event) {
        assert null == event.getArtifact(); // FIXME!
        warn("Missing artifact descriptor for %s.", event.getArtifact());
    }

    @Override public void metadataInvalid(RepositoryEvent event) {
        warn("Invalid metadata %s.", event.getMetadata());
    }

    @Override public void artifactResolving(RepositoryEvent event) {
        debug("Resolving artifact %s.", event.getArtifact());
    }

    @Override public void artifactResolved(RepositoryEvent event) {
        debug("Resolved artifact %s from %s.", event.getArtifact(), event.getRepository());
    }

    @Override public void metadataResolving(RepositoryEvent event) {
        debug("Resolving metadata %s from %s.", event.getMetadata(), event.getRepository());
    }

    @Override public void metadataResolved(RepositoryEvent event) {
        debug("Resolved metadata %s from %s.", event.getMetadata(), event.getRepository());
    }

    @Override public void artifactDownloading(RepositoryEvent event) {
        debug("Downloading artifact %s from %s.", event.getArtifact(), event.getRepository());
    }

    @Override public void artifactDownloaded(RepositoryEvent event) {
        debug("Downloaded artifact %s from %s.", event.getArtifact(), event.getRepository());
    }

    @Override public void metadataDownloading(RepositoryEvent event) {
        debug("Downloading metadata %s from %s.", event.getMetadata(), event.getRepository());
    }

    @Override public void metadataDownloaded(RepositoryEvent event) {
        debug("Downloaded metadata %s from %s.", event.getMetadata(), event.getRepository());
    }

    @Override public void artifactInstalling(RepositoryEvent event) {
        debug("Installing %s to %s.", event.getArtifact(), event.getFile());
    }

    @Override public void artifactInstalled(RepositoryEvent event) {
        debug("Installed %s to %s.", event.getArtifact(), event.getFile());
    }

    @Override public void metadataInstalling(RepositoryEvent event) {
        debug("Installing %s to %s.", event.getMetadata(), event.getFile());
    }

    @Override public void metadataInstalled(RepositoryEvent event) {
        debug("Installed %s to %s.", event.getMetadata(), event.getFile());
    }

    @Override public void artifactDeploying(RepositoryEvent event) {
        debug("Deploying %s to %s.", event.getArtifact(), event.getRepository());
    }

    @Override public void artifactDeployed(RepositoryEvent event) {
        debug("Deployed %s to %s.", event.getArtifact(), event.getRepository());
    }

    @Override public void metadataDeploying(RepositoryEvent event) {
        debug("Deploying %s to %s.", event.getMetadata(), event.getRepository());
    }

    @Override public void metadataDeployed(RepositoryEvent event) {
        debug("Deployed %s to %s.", event.getMetadata(), event.getRepository());
    }

    private void debug(final String format, final Object... args) {
        if (!logger.isDebugEnabled()) return;
        logger.debug(format(format, args));
    }

    private void warn(final String format, final Object... args) {
        if (!logger.isWarnEnabled()) return;
        logger.warn(format(format, args));
    }

    private void warn(
            final Throwable error,
            final String format,
            final Object... args) {
        if (!logger.isWarnEnabled()) return;
        logger.warn(format(format, args), error);
    }

    private String format(final String format, final Object... args) {
        return String.format(Locale.ENGLISH, format, args);
    }
}
