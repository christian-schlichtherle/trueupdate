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
public enum ActionId {

    DOWNLOAD, ZIP, PATCH, UNZIP, UNDEPLOY,
    SWAP_OUT_FILE, SWAP_OUT_DIR, SWAP_IN_FILE, SWAP_IN_DIR,
    DEPLOY;

    public String beginKey() { return prefix() + ".begin"; }

    public String endKey() { return prefix() + ".end"; }

    /**
     * Returns the prefix for the key for the message catalog in the resource
     * bundle for the class {@link UpdateMessage}.
     */
    public String prefix() {
        return "cmd." + name().toLowerCase(Locale.ENGLISH);
    }
}
