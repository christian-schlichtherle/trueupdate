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
extends XmlAdapter<CompactLogRecordDto[], List<LogRecord>> {

    @Override
    public List<LogRecord> unmarshal(
            final @CheckForNull CompactLogRecordDto[] clrs)
    throws Exception {
        final int l = null == clrs ? 0 : clrs.length;
        final List<LogRecord> lrs = new ArrayList<LogRecord>(l);
        for (int i = 0; i < l; i++) {
            final CompactLogRecordDto clr = clrs[i];
            final LogRecord lr = new LogRecord(Level.parse(clr.level),
                                               clr.message);
            lr.setLoggerName(clr.loggerName);
            final String rbn = clr.resourceBundleName;
            lr.setResourceBundleName(rbn);
            if (null != rbn) {
                try { lr.setResourceBundle(ResourceBundle.getBundle(rbn)); }
                catch (MissingResourceException ignored) { }
            }
            lr.setSequenceNumber(clr.sequenceNumber);
            lr.setSourceClassName(clr.sourceClassName);
            lr.setSourceMethodName(clr.sourceMethodName);
            lr.setParameters(clr.parameters);
            lr.setThreadID(clr.threadId);
            lr.setMillis(clr.millis);
            if (null != clr.thrown)
                lr.setThrown(new Throwable(clr.thrown));
            lrs.add(lr);
        }
        return lrs;
    }

    @Override
    public CompactLogRecordDto[] marshal(
            final @CheckForNull List<LogRecord> lrs)
    throws Exception {
        final int lrss = null == lrs ? 0 : lrs.size();
        final CompactLogRecordDto[] clrs = new CompactLogRecordDto[lrss];
        if (0 != lrss) {
            int i = 0;
            for (final LogRecord lr : lrs) {
                final CompactLogRecordDto clr = new CompactLogRecordDto();
                clr.loggerName = lr.getLoggerName();
                clr.resourceBundleName = lr.getResourceBundleName();
                clr.level = lr.getLevel().getName();
                clr.sequenceNumber = lr.getSequenceNumber();
                clr.sourceClassName = lr.getSourceClassName();
                clr.sourceMethodName = lr.getSourceMethodName();
                clr.message = lr.getMessage();
                clr.parameters = marshalAsXsiTypesForUseWithMessageFormat(
                        lr.getParameters());
                clr.threadId = lr.getThreadID();
                clr.millis = lr.getMillis();
                final Throwable t = lr.getThrown();
                if (null != t) {
                    final StringWriter sw = new StringWriter(1024);
                    final PrintWriter pw = new PrintWriter(sw);
                    t.printStackTrace(pw);
                    pw.close();
                    clr.thrown = sw.toString();
                }
                clrs[i++] = clr;
            }
        }
        return clrs;
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
