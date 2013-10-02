/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.spec;

import java.util.Locale;
import net.java.trueupdate.message.UpdateMessage;

/**
 * Identifiers for the transactions during the update process.
 *
 * @author Christian Schlichtherle
 */
public enum Action {

    DOWNLOAD, ZIP, PATCH, UNZIP, UNDEPLOY,
    SWAP_OUT_FILE, SWAP_OUT_DIR, SWAP_IN_FILE, SWAP_IN_DIR,
    DEPLOY;

    /**
     * Returns the key for the message catalog in the resource bundle for the
     * class {@link UpdateMessage}.
     */
    public String key() {
        return "tx." + name().toLowerCase(Locale.ENGLISH);
    }
}
