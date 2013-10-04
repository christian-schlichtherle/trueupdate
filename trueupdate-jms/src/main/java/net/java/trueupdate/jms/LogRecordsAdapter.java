/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.annotation.*;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Marshals a list of log records to an array of their DTOs and vice versa.
 *
 * @author Christian Schlichtherle
 */
@Immutable
final class LogRecordsAdapter
extends XmlAdapter<LogRecordDto[], List<LogRecord>> {

    @Override
    public List<LogRecord> unmarshal(
            final @CheckForNull LogRecordDto[] dtos)
    throws Exception {
        final int l = null == dtos ? 0 : dtos.length;
        final List<LogRecord> lrs = new ArrayList<LogRecord>(l);
        for (int i = 0; i < l; i++) {
            final LogRecordDto dto = dtos[i];
            final LogRecord lr = new LogRecord(Level.parse(dto.level),
                                               dto.message);
            lr.setLoggerName(dto.loggerName);
            final String rbn = dto.resourceBundleName;
            lr.setResourceBundleName(rbn);
            if (null != rbn) {
                try { lr.setResourceBundle(ResourceBundle.getBundle(rbn)); }
                catch (MissingResourceException ignored) { }
            }
            lr.setSequenceNumber(dto.sequenceNumber);
            lr.setSourceClassName(dto.sourceClassName);
            lr.setSourceMethodName(dto.sourceMethodName);
            lr.setParameters(dto.parameters);
            lr.setThreadID(dto.threadId);
            lr.setMillis(dto.millis);
            if (null != dto.thrown)
                lr.setThrown(new Throwable(dto.thrown));
            lrs.add(lr);
        }
        return lrs;
    }

    @Override
    public LogRecordDto[] marshal(
            final @CheckForNull List<LogRecord> lrs)
    throws Exception {
        final int lrss = null == lrs ? 0 : lrs.size();
        final LogRecordDto[] dtos = new LogRecordDto[lrss];
        if (0 != lrss) {
            int i = 0;
            for (final LogRecord lr : lrs) {
                final LogRecordDto dto = new LogRecordDto();
                dto.loggerName = lr.getLoggerName();
                dto.resourceBundleName = lr.getResourceBundleName();
                dto.level = lr.getLevel().getName();
                dto.sequenceNumber = lr.getSequenceNumber();
                dto.sourceClassName = lr.getSourceClassName();
                dto.sourceMethodName = lr.getSourceMethodName();
                dto.message = lr.getMessage();
                dto.parameters = marshalAsXsiTypesForUseWithMessageFormat(
                        lr.getParameters());
                dto.threadId = lr.getThreadID();
                dto.millis = lr.getMillis();
                final Throwable t = lr.getThrown();
                if (null != t) {
                    final StringWriter sw = new StringWriter(1024);
                    final PrintWriter pw = new PrintWriter(sw);
                    t.printStackTrace(pw);
                    pw.close();
                    dto.thrown = sw.toString();
                }
                dtos[i++] = dto;
            }
        }
        return dtos;
    }

    private static @Nullable Object[] marshalAsXsiTypesForUseWithMessageFormat(
            final @CheckForNull Object[] in) {
        if (null == in) return null;
        final int l = in.length;
        final Object[] out = new Object[l];
        for (int i = 0; i < l; i++) {
            final Object obj = in[i];
            out[i] = (null == obj || obj instanceof Number
                        || obj instanceof Boolean || obj instanceof Date)
                    ? obj : obj.toString();
        }
        return out;
    }
}
