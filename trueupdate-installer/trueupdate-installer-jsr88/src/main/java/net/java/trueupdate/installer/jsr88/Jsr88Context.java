/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.jsr88;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import net.java.trueupdate.installer.core.util.Uris;
import net.java.trueupdate.manager.spec.tx.Transaction;
import static net.java.trueupdate.util.Objects.nonNullOr;

/**
 * A context which decomposes a location URI to configure various parameters
 * and perform a redeployment using the JSR 88 API.
 *
 * @author Christian Schlichtherle
 */
@Immutable
final class Jsr88Context {

    private final URI location;
    private final Map<String, List<String>> parameters;
    private final DeploymentFactory df;

    Jsr88Context(final URI location, final DeploymentFactory df) {
        this.location = location;
        this.parameters = Uris.queryParameters(location);
        assert null != df;
        this.df = df;
    }

    DeploymentFactory deploymentFactory() { return df; }

    ModuleType moduleType() throws Jsr88Exception {
        final String scheme = location.getScheme().toLowerCase(Locale.ENGLISH);

             if ("car".equals(scheme)) return ModuleType.CAR;
        else if ("ear".equals(scheme)) return ModuleType.EAR;
        else if ("ejb".equals(scheme)) return ModuleType.EJB;
        else if ("rar".equals(scheme)) return ModuleType.RAR;
        else if ("war".equals(scheme)) return ModuleType.WAR;

        throw new Jsr88Exception(String.format(
                "Unknown module type %s.", scheme));
    }

    File moduleArchive() { return new File(location.getPath()); }

    String moduleID() { return parameter("moduleID"); }
    String uri() { return parameter("uri"); }
    String username() { return parameter("username"); }
    String password() { return parameter("password"); }

    @Nullable File deploymentPlan() {
        final String dp = parameter("deploymentPlan", null);
        return null == dp ? null : new File(dp);
    }

    private String parameter(String name) { return parameter(name, ""); }

    private String parameter(final String name, final String defaultValue) {
        for (String p : parameters(name)) return p;
        return defaultValue;
    }

    private List<String> parameters(String name) {
        return nonNullOr(parameters.get(name), Collections.<String>emptyList());
    }

    Transaction undeploymentTransaction() {
        return new UndeploymentTransaction();
    }

    Transaction deploymentTransaction() {
        return new DeploymentTransaction();
    }

    @NotThreadSafe
    private abstract class RedeploymentTransaction extends Transaction {

        Jsr88Session session;

        @Override public final void prepare() throws Jsr88Exception {
            final File ma = moduleArchive();
            if (!ma.exists())
                throw new Jsr88Exception(String.format(
                        "The module archive %s does not exist.", ma));
            session = new Jsr88Session(Jsr88Context.this);
        }

        @Override public void rollback() { close(); }

        @Override public final void commit() { close(); }

        final void close() { session.close(); }
    } // RedeploymentTransaction

    private final class UndeploymentTransaction
    extends RedeploymentTransaction {

        State state = State.STARTED;

        @Override public void perform() throws Jsr88Exception {
            session.checkDeclaredModuleID();
            session.stop();
            state = State.STOPPED;
            session.undeploy();
            state = State.UNDEPLOYED;
        }

        @Override public void rollback() {
            try {
                switch (state) {
                    case UNDEPLOYED:
                        session.deploy();
                    case STOPPED:
                        session.start();
                }
            } catch (Jsr88Exception ex) {
                throw new IllegalStateException(ex);
            } finally {
                super.rollback();
            }
        }
    } // UndeploymentTransaction

    private final class DeploymentTransaction
    extends RedeploymentTransaction {

        State state = State.UNDEPLOYED;

        @Override public void perform() throws Jsr88Exception {
            session.deploy();
            state = State.DEPLOYED;
            session.checkDeclaredModuleID();
            session.start();
            state = State.STARTED;
        }

        @Override public void rollback() {
            try {
                switch (state) {
                    case STARTED:
                        session.stop();
                    case DEPLOYED:
                        session.undeploy();
                }
            } catch (Jsr88Exception ex) {
                throw new IllegalStateException(ex);
            } finally {
                super.rollback();
            }
        }
    } // DeploymentTransaction

    private enum State { STARTED, STOPPED, UNDEPLOYED, DEPLOYED }
}
