/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.maven;

import java.util.Locale;
import javax.annotation.concurrent.Immutable;
import org.eclipse.aether.spi.log.*;
import org.eclipse.aether.transfer.*;

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
        if (isDebugEnabled()) debug(new Message(event).transferInitiated());
    }

    @Override public void transferSucceeded(final TransferEvent event) {
        if (isDebugEnabled()) debug(new Message(event).transferSucceeded());
    }

    @Override public void transferFailed(final TransferEvent event) {
        // Don't log the exception: It gets thrown anyway, so that logging it
        // just duplicates the information.
        if (isWarnEnabled()) warn(new Message(event).transferFailed());
    }

    private boolean isWarnEnabled() { return logger.isWarnEnabled(); }
    private boolean isDebugEnabled() { return logger.isDebugEnabled(); }

    private void warn(String message) { logger.warn(message); }
    private void debug(String message) { logger.debug(message); }

    @Immutable
    private static final class Message {

        private final TransferEvent event;

        Message(final TransferEvent event) {
            assert null != event;
            this.event = event;
        }

        String transferInitiated() { return format(initiatedFormat()); }

        String transferSucceeded() {
            final long bytes = event.getTransferredBytes();
            final TransferSize size = new TransferSize(bytes);
            final TransferRate rate = new TransferRate(bytes, durationMillis());
            return format(succeededFormat(), resourceUrlString(),
                    size.toString(Locale.ENGLISH),
                    rate.toString(Locale.ENGLISH));
        }

        String transferFailed() { return format(failedFormat()); }

        private long durationMillis() {
            return System.currentTimeMillis() - resource().getTransferStartTime();
        }

        private String format(String format) {
            return format(format, resourceUrlString());
        }

        private String format(String format, Object... args) {
            return String.format(Locale.ENGLISH, format, args);
        }

        private String resourceUrlString() {
            final TransferResource resource = resource();
            return resource.getRepositoryUrl() + resource.getResourceName();
        }

        private TransferResource resource() { return event.getResource(); }

        private String initiatedFormat() {
            return isUpload()
                    ? "Starting to upload %s."
                    : "Starting to download %s.";
        }

        private String succeededFormat() {
            return isUpload()
                    ? "Succeeded to upload %s: %s at %s."
                    : "Succeeded to download %s: %s at %s.";
        }

        private String failedFormat() {
            return isUpload()
                    ? "Failed to upload %s."
                    : "Failed to download %s.";
        }

        private boolean isUpload() {
            return TransferEvent.RequestType.PUT.equals(event.getRequestType());
        }
    }
}
