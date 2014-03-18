/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.spec;

import java.util.Locale;
import net.java.trueupdate.message.UpdateMessage;

/**
 * Identifiers for the commands during the update process.
 *
 * @author Christian Schlichtherle
 */
public enum CommandId {

    DOWNLOAD, ZIP, PATCH, UNZIP, UNDEPLOY,
    SWAP_OUT_FILE, SWAP_OUT_DIR, SWAP_IN_FILE, SWAP_IN_DIR,
    DEPLOY;

    /** Returns the resource bundle name. */
    public static String resourceBundleName() {
        return UpdateMessage.class.getName();
    }

    /** Returns the key for the begin message. */
    public String beginKey() { return prefix() + ".begin"; }

    /** Returns the key for the end message. */
    public String endKey() { return prefix() + ".end"; }

    private String prefix() {
        return "cmd." + name().toLowerCase(Locale.ENGLISH);
    }
}
