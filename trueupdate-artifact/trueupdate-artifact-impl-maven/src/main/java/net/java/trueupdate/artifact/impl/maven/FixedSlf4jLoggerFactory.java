/*******************************************************************************
 * Copyright (c) 2010, 2013 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sonatype, Inc. - initial API and implementation
 *******************************************************************************/
package net.java.trueupdate.artifact.impl.maven;

import org.eclipse.aether.spi.locator.Service;
import org.eclipse.aether.spi.locator.ServiceLocator;
import org.eclipse.aether.spi.log.Logger;
import org.eclipse.aether.spi.log.LoggerFactory;
import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LocationAwareLogger;

/**
 * A logger factory that delegates to Slf4J logging.
 * This is based on the original
 * {@link org.eclipse.aether.internal.impl.Slf4jLoggerFactory} and has been
 * patched to work with SLF4J versions prior to 1.6.0, as required by Glassfish
 * Embedded.
 *
 * @author Sonatype, Inc (initial API and implementation)
 * @author Christian Schlichtherle (fix to work with SLF4J 1.6+)
 */
public final class FixedSlf4jLoggerFactory implements LoggerFactory, Service {

    private ILoggerFactory factory;

    public FixedSlf4jLoggerFactory() {
        // enables no-arg constructor
    }

    FixedSlf4jLoggerFactory(ILoggerFactory factory) {
        setLoggerFactory(factory);
    }

    @Override
    public void initService(ServiceLocator locator) {
        setLoggerFactory(locator.getService(ILoggerFactory.class));
    }

    public FixedSlf4jLoggerFactory setLoggerFactory(ILoggerFactory factory) {
        this.factory = factory;
        return this;
    }

    @Override
    public Logger getLogger(String name) {
        org.slf4j.Logger logger = getFactory().getLogger(name);
        /*if (logger instanceof LocationAwareLogger) {
            return new Slf4jLoggerEx((LocationAwareLogger) logger);
        }*/
        return new Slf4jLogger(logger);
    }

    private ILoggerFactory getFactory() {
        if (factory == null) {
            factory = org.slf4j.LoggerFactory.getILoggerFactory();
        }
        return factory;
    }

    private static final class Slf4jLogger
            implements Logger {

        private final org.slf4j.Logger logger;

        public Slf4jLogger(org.slf4j.Logger logger) {
            this.logger = logger;
        }

        @Override
        public boolean isDebugEnabled() {
            return logger.isDebugEnabled();
        }

        @Override
        public void debug(String msg) {
            logger.debug(msg);
        }

        @Override
        public void debug(String msg, Throwable error) {
            logger.debug(msg, error);
        }

        @Override
        public boolean isWarnEnabled() {
            return logger.isWarnEnabled();
        }

        @Override
        public void warn(String msg) {
            logger.warn(msg);
        }

        @Override
        public void warn(String msg, Throwable error) {
            logger.warn(msg, error);
        }
    }

    private static final class Slf4jLoggerEx implements Logger {

        private static final String FQCN = Slf4jLoggerEx.class.getName();
        private final LocationAwareLogger logger;

        public Slf4jLoggerEx(LocationAwareLogger logger) {
            this.logger = logger;
        }

        @Override
        public boolean isDebugEnabled() {
            return logger.isDebugEnabled();
        }

        @Override
        public void debug(String msg) {
            logger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, msg, null, null);
        }

        @Override
        public void debug(String msg, Throwable error) {
            logger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, msg, null, error);
        }

        @Override
        public boolean isWarnEnabled() {
            return logger.isWarnEnabled();
        }

        @Override
        public void warn(String msg) {
            logger.log(null, FQCN, LocationAwareLogger.WARN_INT, msg, null, null);
        }

        @Override
        public void warn(String msg, Throwable error) {
            logger.log(null, FQCN, LocationAwareLogger.WARN_INT, msg, null, error);
        }
    }
}
