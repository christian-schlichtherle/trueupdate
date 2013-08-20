/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.artifact.maven;

import java.io.File;
import java.util.Locale;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import org.eclipse.aether.*;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.metadata.Metadata;
import org.eclipse.aether.repository.ArtifactRepository;
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
        logFailure("Invalid artifact descriptor: %s.", event.getArtifact());
    }

    @Override public void artifactDescriptorMissing(RepositoryEvent event) {
        assert null == event.getArtifact(); // FIXME?
        logFailure("Missing artifact descriptor for %s.", event.getArtifact());
    }

    @Override public void metadataInvalid(RepositoryEvent event) {
        logFailure("Invalid metadata %s.", event.getMetadata());
    }

    @Override public void artifactResolving(RepositoryEvent event) {
        logStartingWorkOnSource(event, "resolve", event.getArtifact(), event.getRepository());
    }

    @Override public void artifactResolved(RepositoryEvent event) {
        logFinishedWorkOnSource(event, "resolve", event.getArtifact(), event.getRepository());
    }

    @Override public void metadataResolving(RepositoryEvent event) {
        logStartingWorkOnSource(event, "resolve", event.getMetadata(), event.getRepository());
    }

    @Override public void metadataResolved(RepositoryEvent event) {
        logFinishedWorkOnSource(event, "resolve", event.getMetadata(), event.getRepository());
    }

    @Override public void artifactDownloading(RepositoryEvent event) {
        logStartingWorkOnSource(event, "download", event.getArtifact(), event.getRepository());
    }

    @Override public void artifactDownloaded(RepositoryEvent event) {
        logFinishedWorkOnSource(event, "download", event.getArtifact(), event.getRepository());
    }

    @Override public void metadataDownloading(RepositoryEvent event) {
        logStartingWorkOnSource(event, "download", event.getMetadata(), event.getRepository());
    }

    @Override public void metadataDownloaded(RepositoryEvent event) {
        logFinishedWorkOnSource(event, "download", event.getMetadata(), event.getRepository());
    }

    @Override public void artifactInstalling(RepositoryEvent event) {
        logStartingWorkOnDestination(event, "install", event.getArtifact(), event.getFile());
    }

    @Override public void artifactInstalled(RepositoryEvent event) {
        logFinishedWorkOnDestination(event, "install", event.getArtifact(), event.getFile());
    }

    @Override public void metadataInstalling(RepositoryEvent event) {
        logStartingWorkOnDestination(event, "install", event.getMetadata(), event.getFile());
    }

    @Override public void metadataInstalled(RepositoryEvent event) {
        logFinishedWorkOnDestination(event, "install", event.getMetadata(), event.getFile());
    }

    @Override public void artifactDeploying(RepositoryEvent event) {
        logStartingWorkOnDestination(event, "deploy", event.getArtifact(), event.getRepository());
    }

    @Override public void artifactDeployed(RepositoryEvent event) {
        logFinishedWorkOnDestination(event, "deploy", event.getArtifact(), event.getRepository());
    }

    @Override public void metadataDeploying(RepositoryEvent event) {
        logStartingWorkOnDestination(event, "deploy", event.getMetadata(), event.getRepository());
    }

    @Override public void metadataDeployed(RepositoryEvent event) {
        logFinishedWorkOnDestination(event, "deploy", event.getMetadata(), event.getRepository());
    }

    private void logFailure(String messageFormat, Object item) {
        if (isWarnEnabled())
            warn(format(messageFormat, describe(item)));
    }

    private void logStartingWorkOnSource(RepositoryEvent event, String verb, Object item, Object source) {
        if (isDebugEnabled())
            debug(startingWorkOnSourceMessage(event, verb, item, source));
    }

    private static String startingWorkOnSourceMessage(RepositoryEvent event, String verb, Object item, Object source) {
        return formatWorkOnSourceMessage(startingWorkMessageFormat(event, verb), describe(item), describe(source));
    }

    private void logFinishedWorkOnSource(RepositoryEvent event, String verb, Object item, Object source) {
        if (isFailure(event))
            warnFinishedWorkOnSource(event, verb, item, source);
        else
            debugFinishedWorkOnSource(event, verb, item, source);
    }

    private void warnFinishedWorkOnSource(RepositoryEvent event, String verb, Object item, Object source) {
        if (isWarnEnabled())
            warn(finishedWorkOnSourceMessage(event, verb, item, source));
    }

    private void debugFinishedWorkOnSource(RepositoryEvent event, String verb, Object item, Object source) {
        if (isDebugEnabled())
            debug(finishedWorkOnSourceMessage(event, verb, item, source));
    }

    private static String finishedWorkOnSourceMessage(RepositoryEvent event, String verb, Object item, Object source) {
        return formatWorkOnSourceMessage(finishedWorkMessageFormat(event, verb), describe(item), describe(source));
    }

    private void logStartingWorkOnDestination(RepositoryEvent event, String verb, Object item, Object destination) {
        if (isDebugEnabled())
            debug(startingWorkOnDestinationMessage(event, verb, item, destination));
    }

    private static String startingWorkOnDestinationMessage(RepositoryEvent event, String verb, Object item, Object destination) {
        return formatWorkOnDestinationMessage(startingWorkMessageFormat(event, verb), describe(item), describe(destination));
    }

    private void logFinishedWorkOnDestination(RepositoryEvent event, String verb, Object item, Object destination) {
        if (isFailure(event))
            warnFinishedWorkOnDestination(event, verb, item, destination);
        else
            debugFinishedWorkOnDestination(event, verb, item, destination);
    }

    private void warnFinishedWorkOnDestination(RepositoryEvent event, String verb, Object item, Object destination) {
        if (isWarnEnabled())
            warn(finishedWorkOnDestinationMessage(event, verb, item, destination));
    }

    private void debugFinishedWorkOnDestination(RepositoryEvent event, String verb, Object item, Object destination) {
        if (isDebugEnabled())
            debug(finishedWorkOnDestinationMessage(event, verb, item, destination));
    }

    private static String finishedWorkOnDestinationMessage(RepositoryEvent event, String verb, Object item, Object source) {
        return formatWorkOnDestinationMessage(finishedWorkMessageFormat(event, verb), describe(item), describe(source));
    }

    private static String startingWorkMessageFormat(RepositoryEvent event, String verb) {
        return "Starting to " + verb + " %s.";
    }

    private static String finishedWorkMessageFormat(RepositoryEvent event, String verb) {
        return (isFailure(event) ? "Failed" : "Succeeded")
                + " to " + verb + " %s.";
    }

    private static boolean isFailure(RepositoryEvent event) {
        return !event.getExceptions().isEmpty();
    }

    private static String formatWorkOnSourceMessage(String messageFormat, String item, String source) {
        return expandAndFormat("%s from %s", messageFormat, item, source);
    }

    private static String formatWorkOnDestinationMessage(String messageFormat, String item, String destination) {
        return expandAndFormat("%s to %s", messageFormat, item, destination);
    }

    private static String expandAndFormat(String expansionFormat, String messageFormat, Object... args) {
        return format(expand(expansionFormat, messageFormat, args), args);
    }

    private static String expand(String expansionFormat, String messageFormat, Object... args) {
        return hasSecondArg(args) ? format(messageFormat, expansionFormat) : messageFormat;
    }

    private static boolean hasSecondArg(Object... args) {
        return 2 <= args.length && nonEmpty(args[1]);
    }

    private static boolean nonEmpty(Object object) {
        return null != object && !object.toString().isEmpty();
    }

    private static @Nullable String describe(@CheckForNull Object object) {
        if (null == object) return null;
        if (object instanceof Artifact) return "artifact " + object;
        if (object instanceof Metadata) return "metadata " + object;
        if (object instanceof ArtifactRepository) return "repository " + object;
        if (object instanceof File) return "file " + object;
        return object.toString();
    }

    private static String format(final String format, final Object... args) {
        return String.format(Locale.ENGLISH, format, args);
    }

    private boolean isWarnEnabled() { return logger.isWarnEnabled(); }
    private boolean isDebugEnabled() { return logger.isDebugEnabled(); }

    private void warn(String message) { logger.warn(message); }
    private void debug(String message) { logger.debug(message); }
}
