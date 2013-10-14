/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jaxrs.server;

import java.util.concurrent.Callable;
import javax.annotation.concurrent.Immutable;
import javax.ws.rs.*;
import net.java.trueupdate.artifact.spec.*;
import net.java.trueupdate.jaxrs.util.UpdateServiceException;

/**
 * The (un-configured) server-side implementation of a RESTful service for
 * artifact updates.
 *
 * @author Christian Schlichtherle
 */
@Immutable
public abstract class AbstractUpdateServer {

    /** Returns the artifact resolver. */
    protected abstract ArtifactResolver artifactResolver();

    /** Returns a configured update server. */
    @Path("artifact")
    public ConfiguredUpdateServer artifact(
            final @QueryParam("groupId") String groupId,
            final @QueryParam("artifactId") String artifactId,
            final @QueryParam("version") String version,
            final @QueryParam("classifier") @DefaultValue("") String classifier,
            // Note the use of "extension" as the parameter name for backwards
            // compatibility with TrueUpdate 0.6 and earlier versions.
            final @QueryParam("extension") @DefaultValue("jar") String packaging)
    throws UpdateServiceException {
        return UpdateServers.wrap(new Callable<ConfiguredUpdateServer>() {
            @Override
            public ConfiguredUpdateServer call() {
                return new ConfiguredUpdateServer(artifactResolver(),
                        ArtifactDescriptor.builder()
                                .groupId(groupId)
                                .artifactId(artifactId)
                                .version(version)
                                .classifier(classifier)
                                .packaging(packaging)
                                .build());
            }
        });
    }
}
