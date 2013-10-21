/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.java.trueupdate.installer.jsr88;

import javax.annotation.WillNotClose;
import net.java.trueupdate.core.io.Task;

/**
 * A JSR 88 task is given a JSR 88 session when executing and either returns
 * nothing or throws an exception.
 *
 * @author Christian Schlichtherle
 */
interface Jsr88Task extends Task<Void, Jsr88Session, Exception> {

    @Override Void execute(@WillNotClose Jsr88Session session) throws Exception;
}
