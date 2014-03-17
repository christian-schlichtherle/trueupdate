/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.jsr88;

import edu.umd.cs.findbugs.annotations.CleanupObligation;
import edu.umd.cs.findbugs.annotations.DischargesObligation;
import net.java.trueupdate.installer.core.util.Uris;
import net.java.trueupdate.manager.spec.cmd.AbstractCommand;
import net.java.trueupdate.manager.spec.cmd.Command;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

    boolean redeploy() {
        return Boolean.valueOf(parameter("redeploy", "false"));
    }

    @CheckForNull File deploymentPlan() {
        final String dp = parameter("deploymentPlan");
        return dp.isEmpty() ? null : new File(dp);
    }

    private String parameter(String name) { return parameter(name, ""); }

    @SuppressWarnings("LoopStatementThatDoesntLoop")
    private @Nullable String parameter(
            final String name,
            final @CheckForNull String defaultValue) {
        for (String p : parameters(name)) return p;
        return defaultValue;
    }

    private List<String> parameters(String name) {
        return nonNullOr(parameters.get(name), Collections.<String>emptyList());
    }

    Command undeploymentTransaction() {
        return new UndeploymentCommand();
    }

    Command deploymentTransaction() {
        return new DeploymentCommand();
    }

    private void loanSessionTo(final Jsr88Script script) throws Jsr88Exception {
        final Jsr88Session session = new Jsr88Session(this);
        try {
            script.run(session);
        } finally {
            session.close();
        }
    }

    abstract private class RedeploymentCommand extends AbstractCommand {

        @Override final protected void onStart() throws Jsr88Exception {
            final File ma = moduleArchive();
            if (!ma.exists())
                throw new Jsr88Exception(String.format(
                        "The module archive %s does not exist.", ma));
        }
    } // RedeploymentCommand

    @CleanupObligation
    final private class UndeploymentCommand
    extends RedeploymentCommand {

        State state = State.STARTED;

        @Override protected void onPerform() throws Jsr88Exception {
            if (!redeploy()) {
                loanSessionTo(new Jsr88Script() {
                    @Override
                    public void run(final Jsr88Session session) throws Jsr88Exception {
                        session.checkDeclaredModuleID();
                        session.stop();
                        state = State.STOPPED;
                        session.undeploy();
                        state = State.UNDEPLOYED;
                    }
                });
            }
        }

        @DischargesObligation
        @Override protected void onRevert() throws Jsr88Exception {
            loanSessionTo(new Jsr88Script() {
                @Override
                public void run(final Jsr88Session session) throws Jsr88Exception {
                    if (redeploy()) {
                        session.redeploy();
                    } else {
                        switch (state) {
                            case UNDEPLOYED:
                                session.deploy();
                            case STOPPED:
                                session.start();
                        }
                    }
                }
            });
        }
    } // UndeploymentCommand

    @CleanupObligation
    final private class DeploymentCommand
    extends RedeploymentCommand {

        State state = State.UNDEPLOYED;

        @Override protected void onPerform() throws Jsr88Exception {
            loanSessionTo(new Jsr88Script() {
                @Override
                public void run(final Jsr88Session session) throws Jsr88Exception {
                    if (redeploy()) {
                        session.redeploy();
                    } else {
                        session.deploy();
                        state = State.DEPLOYED;
                        session.checkDeclaredModuleID();
                        session.start();
                        state = State.STARTED;
                    }
                }
            });
        }

        @DischargesObligation
        @Override protected void onRevert() throws Jsr88Exception {
            loanSessionTo(new Jsr88Script() {
                @Override
                public void run(final Jsr88Session session) throws Jsr88Exception {
                    if (!redeploy()) {
                        switch (state) {
                            case STARTED:
                                session.stop();
                            case DEPLOYED:
                                session.undeploy();
                        }
                    }
                }
            });
        }
    } // DeploymentCommand

    private enum State { STARTED, STOPPED, UNDEPLOYED, DEPLOYED }
}
