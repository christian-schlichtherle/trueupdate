/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.cargo;

import java.io.File;
import java.net.*;
import java.util.*;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import net.java.trueupdate.installer.core.tx.*;
import static net.java.trueupdate.util.Objects.nonNullOr;
import org.codehaus.cargo.container.*;
import org.codehaus.cargo.container.configuration.*;
import org.codehaus.cargo.container.deployable.*;
import org.codehaus.cargo.container.deployer.*;
import org.codehaus.cargo.generic.DefaultContainerFactory;
import org.codehaus.cargo.generic.configuration.DefaultConfigurationFactory;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.generic.deployer.DefaultDeployerFactory;

/**
 * A context which decomposes a configuration URI to determine the parameters
 * of the various Cargo objects it creates.
 *
 * @author Christian Schlichtherle
 */
@Immutable
final class CargoContext {

    private final URI configuration;
    private final Map<String, List<String>> parameters;

    CargoContext(final URI configuration) {
        this.parameters = Uris.queryParameters(configuration);
        this.configuration = configuration;
    }

    public Transaction deploymentTransaction() {
        return new DeploymentTransaction();
    }

    void deploy() throws CargoException {
        final Deployable deployable = deployable();
        final String monitorUrl = monitorUrl();
        try {
            if (monitorUrl.isEmpty()) deployer().deploy(deployable);
            else deployer().deploy(deployable, deployableMonitor());
        } catch (RuntimeException ex) {
            throw new CargoException(
                    String.format("Cannot deploy %s .", deployable), ex);
        }
    }

    public Transaction undeploymentTransaction() {
        return new UndeploymentTransaction();
    }

    void undeploy() throws CargoException {
        final Deployable deployable = deployable();
        final String monitorUrl = monitorUrl();
        try {
            if (monitorUrl.isEmpty()) deployer().undeploy(deployable);
            else deployer().undeploy(deployable, deployableMonitor());
        } catch (RuntimeException ex) {
            throw new CargoException(
                    String.format("Cannot undeploy %s .", deployable), ex);
        }
    }

    public Deployer deployer() throws CargoContextException {
        try {
            return new DefaultDeployerFactory().createDeployer(container());
        } catch (RuntimeException ex) {
            throw newException("deployer", ex);
        }
    }

    public Container container() throws CargoContextException {
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

    public Configuration configuration() throws CargoContextException {
        try {
            final Configuration c = new DefaultConfigurationFactory()
                    .createConfiguration(
                            containerId(), containerType(), configurationType(),
                            nonEmptyOrNull(configurationHome()));
            for (final String name : parameters.keySet())
                if (!name.startsWith("context."))
                    c.setProperty(name, parameter(name));
            return c;
        } catch (RuntimeException ex) {
            throw newException("configuration", ex);
        }
    }

    public File deployablePath() throws CargoContextException {
        return new File(deployable().getFile());
    }

    public Deployable deployable() throws CargoContextException {
        try {
            return new DefaultDeployableFactory().createDeployable(
                    containerId(), deployableLocation(), deployableType());
        } catch (RuntimeException ex) {
            throw newException("deployable", ex);
        }
    }

    public DeployableMonitor deployableMonitor() throws CargoContextException {
        try {
            return new URLDeployableMonitor(new URL(monitorUrl()),
                    Long.parseLong(monitorTimeout()),
                    nonEmptyOrNull(monitorContains()));
        } catch (Exception ex) {
            throw newException("deployable monitor", ex);
        }
    }

    DeployableType deployableType() {
        return DeployableType.toType(
                nonNullOr(configuration.getScheme(), "file"));
    }

    String deployableLocation() {
        return nonNullOr(configuration.getPath(), "");
    }

    String containerId() { return parameter("context.container.id"); }

    ContainerType containerType() {
        return ContainerType.toType(
                parameter("context.container.type", "remote"));
    }

    String containerHome() { return parameter("context.container.home"); }

    ConfigurationType configurationType() {
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

    String configurationHome() {
        return parameter("context.configuration.home",
                defaultConfigurationHome());
    }

    private String defaultConfigurationHome() {
        return ConfigurationType.EXISTING.equals(configurationType())
                ? containerHome() : "";
    }

    String monitorUrl() {
        return parameter("context.monitor.url");
    }

    String monitorTimeout() {
        return parameter("context.monitor.timeout", "20000");
    }

    String monitorContains() {
        return parameter("context.monitor.contains");
    }

    private String parameter(String name) { return parameter(name, ""); }

    private String parameter(final String name, final String defaultValue) {
        for (String p : parameters(name)) return p;
        return defaultValue;
    }

    private List<String> parameters(String name) {
        return nonNullOr(parameters.get(name), Collections.<String>emptyList());
    }

    private CargoContextException newException(String componentName,
                                               Throwable cause) {
        return new CargoContextException(configuration, componentName, cause);
    }

    private static @Nullable String nonEmptyOrNull(String string) {
        return string.isEmpty() ? null : string;
    }

    private class DeploymentTransaction extends AtomicMethodsTransaction {
        @Override public void performAtomic() throws Exception { deploy(); }
        @Override public void rollbackAtomic() throws Exception { undeploy(); }
    } // DeploymentTransaction

    private class UndeploymentTransaction extends AtomicMethodsTransaction {
        @Override public void performAtomic() throws Exception { undeploy(); }
        @Override public void rollbackAtomic() throws Exception { deploy(); }
    } // UndeploymentTransaction
}
