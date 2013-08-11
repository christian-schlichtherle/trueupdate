/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.ejb;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import net.java.trueupdate.manager.spec.UpdateException;
import net.java.trueupdate.manager.spec.UpdateManager;
import net.java.trueupdate.message.UpdateMessage;

/**
 * @author Christian Schlichtherle
 */
@Stateless
@Remote
@EJB(name = "java:global/net.java.trueupdate/trueupdate-manager-ejb/UpdateManagerBean", beanInterface = UpdateManager.class)
public class UpdateManagerBean implements UpdateManager {

    private static Logger
            logger = Logger.getLogger(UpdateManagerBean.class.getName());

    @Override
    public void subscribe(UpdateMessage message) throws UpdateException {
        logger.log(Level.INFO, "Subscribe:\n{0}", message.toString());
    }

    @Override
    public void install(UpdateMessage message) throws UpdateException {
        logger.log(Level.INFO, "Install:\n{0}", message.toString());
    }

    @Override
    public void unsubscribe(UpdateMessage message) throws UpdateException {
        logger.log(Level.INFO, "Unsubscribe:\n{0}", message.toString());
    }
}
