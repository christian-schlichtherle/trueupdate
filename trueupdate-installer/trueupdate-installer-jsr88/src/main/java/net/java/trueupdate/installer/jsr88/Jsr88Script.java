/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.jsr88;

/**
 * @author Christian Schlichtherle
 */
interface Jsr88Script {
    void run(Jsr88Session session) throws Jsr88Exception;
}
