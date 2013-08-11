package net.java.trueupdate.agent.ejb;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import net.java.trueupdate.manager.spec.UpdateRuntimeException;
import net.java.trueupdate.artifact.spec.ArtifactDescriptor;
import net.java.trueupdate.manager.spec.UpdateException;
import net.java.trueupdate.manager.spec.UpdateManager;
import net.java.trueupdate.message.UpdateMessage;

/**
 * @author Christian Schlichtherle
 */
@Startup
@Singleton
public class TestBean {

    @PostConstruct
    public void send() {
        try {
            final UpdateManager updateManager = InitialContext.doLookup(
                    "java:global/net.java.trueupdate/trueupdate-manager-ejb/UpdateManagerBean");
            updateManager.subscribe(UpdateMessage.builder()
                    .type(UpdateMessage.Type.SUBSCRIPTION_REQUEST)
                    .artifactDescriptor(ArtifactDescriptor.builder().groupId("net.java.truevfs").artifactId("truevfs-kernel-spec").version("0.9").build())
                    .build());
        } catch (NamingException | UpdateException ex) {
            throw new UpdateRuntimeException(ex);
        }
    }
}
