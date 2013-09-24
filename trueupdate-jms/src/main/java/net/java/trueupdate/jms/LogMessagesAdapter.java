/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jms;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import net.java.trueupdate.message.LogMessage;

/**
 * Marshals a list of log messages to its DTO and vice versa.
 *
 * @author Christian Schlichtherle
 */
@Immutable
final class LogMessagesAdapter
extends XmlAdapter<CompactLogMessageDto[], List<LogMessage>> {

    @Override
    public @Nullable List<LogMessage> unmarshal(
            final @CheckForNull CompactLogMessageDto[] clms)
    throws Exception {
        final int l = null == clms ? 0 : clms.length;
        final List<LogMessage> lms = new ArrayList<LogMessage>(l);
        for (int i = 0; i < l; i++) {
            final CompactLogMessageDto clm = clms[i];
            lms.add(LogMessage
                    .builder()
                    .level(Level.parse(clm.level))
                    .message(clm.message)
                    .parameters(clm.parameters)
                    .build());
        }
        return lms;
    }

    @Override
    public @Nullable CompactLogMessageDto[] marshal(
            final @CheckForNull List<LogMessage> lms)
    throws Exception {
        final int l = null == lms ? 0 : lms.size();
        final CompactLogMessageDto[] clms = new CompactLogMessageDto[l];
        if (0 != l) {
            int i = 0;
            for (final LogMessage lm : lms) {
                final CompactLogMessageDto clm = new CompactLogMessageDto();
                clm.level = lm.level().getName();
                clm.message = lm.message();
                final Object[] p = lm.parameters();
                clm.parameters = 0 == p.length ? null : p;
                clms[i++] = clm;
            }
        }
        return clms;
    }
}
