/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.maven;

import java.util.Locale;
import javax.annotation.concurrent.Immutable;
import org.eclipse.aether.spi.log.Logger;
import org.eclipse.aether.spi.log.LoggerFactory;
import org.eclipse.aether.transfer.AbstractTransferListener;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transfer.TransferResource;

/**
 * A transfer listener which logs succeeded and failed uploads and downloads.
 *
 * @author Christian Schlichtherle
 */
@Immutable
final class LogTransferListener extends AbstractTransferListener {

    private final Logger logger;

    LogTransferListener(LoggerFactory factory) {
        this.logger = factory.getLogger(LogTransferListener.class.getName());
    }

    @Override public void transferInitiated(final TransferEvent event) {
        if (!logger.isDebugEnabled()) return;
        logger.debug(new Message(event).transferInitiated());
    }

    @Override public void transferSucceeded(final TransferEvent event) {
        if (!logger.isDebugEnabled()) return;
        logger.debug(new Message(event).transferSucceeded());
    }

    @Override public void transferFailed(final TransferEvent event) {
        if (!logger.isWarnEnabled()) return;
        logger.warn(new Message(event).transferFailed(), event.getException());
    }

    @Immutable
    private static final class Message {

        private final TransferEvent event;

        Message(final TransferEvent event) {
            assert null != event;
            this.event = event;
        }

        String transferInitiated() { return format(initiatedPattern()); }

        String transferSucceeded() {
            final long bytes = event.getTransferredBytes();
            final TransferSize size = new TransferSize(bytes);
            final TransferRate rate = new TransferRate(bytes, durationMillis());
            return format(succeededPattern(), resourceUrlString(),
                    size.toString(Locale.ENGLISH),
                    rate.toString(Locale.ENGLISH));
        }

        String transferFailed() { return format(failedPattern()); }

        long durationMillis() {
            return System.currentTimeMillis() - resource().getTransferStartTime();
        }

        private String format(String pattern) {
            return format(pattern, resourceUrlString());
        }

        private String format(String pattern, Object... args) {
            return String.format(Locale.ENGLISH, pattern, args);
        }

        private String resourceUrlString() {
            final TransferResource resource = resource();
            return resource.getRepositoryUrl() + resource.getResourceName();
        }

        private TransferResource resource() { return event.getResource(); }

        private String initiatedPattern() {
            return isUpload()
                    ? "Starting to upload %s."
                    : "Starting to download %s.";
        }

        private String succeededPattern() {
            return isUpload()
                    ? "Succeeded to upload %s: %s at %s."
                    : "Succeeded to download %s: %s at %s.";
        }

        private String failedPattern() {
            return isUpload()
                    ? "Failed to upload %s."
                    : "Failed to download %s.";
        }

        private boolean isUpload() {
            return TransferEvent.RequestType.PUT.equals(event.getRequestType());
        }
    }
}
