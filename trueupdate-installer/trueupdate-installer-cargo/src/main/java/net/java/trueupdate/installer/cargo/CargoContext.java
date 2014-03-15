/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.cargo;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.installer.core.util.Uris;
import net.java.trueupdate.manager.spec.tx.AtomicMethodsTransaction;
import net.java.trueupdate.manager.spec.tx.Transaction;
import static net.java.trueupdate.util.Objects.nonNullOr;
import static net.java.trueupdate.util.Strings.nonEmptyOr;
import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployer.DeployableMonitor;
import org.codehaus.cargo.container.deployer.Deployer;
import org.codehaus.cargo.container.deployer.URLDeployableMonitor;
import org.codehaus.cargo.generic.DefaultContainerFactory;
import org.codehaus.cargo.generic.configuration.DefaultConfigurationFactory;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.generic.deployer.DefaultDeployerFactory;

/**
 * A context which decomposes a location URI to configure various parameters
 * and perform a redeployment using the generic Cargo API.
 *
 * @author Christian Schlichtherle
 */
@Immutable
final class CargoContext {

    private final URI location;
    private final Map<String, List<String>> parameters;

    CargoContext(final URI location) {
        this.parameters = Uris.queryParameters(location);
        this.location = location;
    }

    Transaction deploymentTransaction() {
        return new DeploymentTransaction();
    }

    private void deploy() throws CargoException {
        final Deployable deployable = deployable();
        final String monitorUrl = monitorUrl();
        try {
            if (monitorUrl.isEmpty()) deployer().deploy(deployable);
            else deployer().deploy(deployable, deployableMonitor());
        } catch (RuntimeException ex) {
            throw new CargoException(
                    String.format("Could not deploy %s .", deployable), ex);
        }
    }

    Transaction undeploymentTransaction() {
        return new UndeploymentTransaction();
    }

    private void undeploy() throws CargoException {
        final Deployable deployable = deployable();
        final String monitorUrl = monitorUrl();
        try {
            if (monitorUrl.isEmpty()) deployer().undeploy(deployable);
            else deployer().undeploy(deployable, deployableMonitor());
        } catch (RuntimeException ex) {
            throw new CargoException(
                    String.format("Could not undeploy %s .", deployable), ex);
        }
    }

    private Deployer deployer() throws CargoConfigurationException {
        try {
            return new DefaultDeployerFactory().createDeployer(container());
        } catch (RuntimeException ex) {
            throw newException("deployer", ex);
        }
    }

    private Container container() throws CargoConfigurationException {
        try {
            final Container c = new DefaultContainerFactory().createContainer(
                    containerId(), containerType(), configuration());
            if (c instanceof InstalledLocalContainer)
                ((InstalledLocalContainer) c).setHome(containerHome());
            return c;
        } catch (RuntimeException ex) {
            throw newException("container", ex);
        }
    }

    private Configuration configuration() throws CargoConfigurationException {
        try {
            final Configuration c = new DefaultConfigurationFactory()
                    .createConfiguration(
                            containerId(), containerType(), configurationType(),
                            nonEmptyOr(configurationHome(), null));
            for (final String name : parameters.keySet())
                if (!name.startsWith("context."))
                    c.setProperty(name, parameter(name));
            return c;
        } catch (RuntimeException ex) {
            throw newException("configuration", ex);
        }
    }

    File deployablePath() throws CargoConfigurationException {
        return new File(deployable().getFile());
    }

    private Deployable deployable() throws CargoConfigurationException {
        try {
            return new DefaultDeployableFactory().createDeployable(
                    containerId(), deployableLocation(), deployableType());
        } catch (RuntimeException ex) {
            throw newException("deployable", ex);
        }
    }

    private DeployableMonitor deployableMonitor() throws CargoConfigurationException {
        try {
            return new URLDeployableMonitor(new URL(monitorUrl()),
                    Long.parseLong(monitorTimeout()),
                    nonEmptyOr(monitorContains(), null));
        } catch (Exception ex) {
            throw newException("deployable monitor", ex);
        }
    }

    private DeployableType deployableType() {
        return DeployableType.toType(
                nonNullOr(location.getScheme(), "file"));
    }

    private String deployableLocation() {
        return nonNullOr(location.getPath(), "");
    }

    private String containerId() { return parameter("context.container.id"); }

    private ContainerType containerType() {
        return ContainerType.toType(
                parameter("context.container.type", "remote"));
    }

    private String containerHome() { return parameter("context.container.home"); }

    private ConfigurationType configurationType() {
        return ConfigurationType.toType(parameter("context.configuration.type",
                defaultConfigurationType()));
    }

    private String defaultConfigurationType() {
        final ContainerType type = containerType();
        if (ContainerType.INSTALLED.equals(type))
            return "existing";
        else if (ContainerType.EMBEDDED.equals(type))
            return "standalone";
        else if (ContainerType.REMOTE.equals(type))
            return "runtime";
        else
            return "";
    }

    private String configurationHome() {
        return parameter("context.configuration.home",
                defaultConfigurationHome());
    }

    private String defaultConfigurationHome() {
        return ConfigurationType.EXISTING.equals(configurationType())
                ? containerHome() : "";
    }

    private String monitorUrl() {
        return parameter("context.monitor.url");
    }

    private String monitorTimeout() {
        return parameter("context.monitor.timeout", "20000");
    }

    private String monitorContains() {
        return parameter("context.monitor.contains");
    }

    private String parameter(String name) { return parameter(name, ""); }

    @SuppressWarnings("LoopStatementThatDoesntLoop")
    private String parameter(final String name, final String defaultValue) {
        for (String p : parameters(name)) return p;
        return defaultValue;
    }

    private List<String> parameters(String name) {
        return nonNullOr(parameters.get(name), Collections.<String>emptyList());
    }

    private CargoConfigurationException newException(String componentName,
                                                     Throwable cause) {
        return new CargoConfigurationException(location, componentName, cause);
    }

    private final class DeploymentTransaction
    extends AtomicMethodsTransaction {
        @Override public void performAtomic() throws Exception { deploy(); }
        @Override public void rollbackAtomic() throws Exception { undeploy(); }
    } // DeploymentTransaction

    private final class UndeploymentTransaction
    extends AtomicMethodsTransaction {
        @Override public void performAtomic() throws Exception { undeploy(); }
        @Override public void rollbackAtomic() throws Exception { deploy(); }
    } // UndeploymentTransaction
}
