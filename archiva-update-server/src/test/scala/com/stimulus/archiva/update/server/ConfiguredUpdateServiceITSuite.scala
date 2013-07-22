/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server

import com.sun.jersey.test.framework.{LowLevelAppDescriptor, JerseyTest}
import javax.ws.rs.core.{MultivaluedMap, MediaType}
import javax.ws.rs.core.MediaType._
import org.junit.Test
import org.scalatest.matchers.ShouldMatchers._
import com.sun.jersey.api.core.{DefaultResourceConfig, ResourceConfig}
import javax.ws.rs.ext.ContextResolver
import com.stimulus.archiva.update.server.resolver.{ArtifactDescriptor, ArtifactResolver}
import com.sun.jersey.core.util.MultivaluedMapImpl

private object ConfiguredUpdateServiceITSuite {

  private def queryParams(descriptor: ArtifactDescriptor) = {
    val map = new MultivaluedMapImpl
    map.putSingle("groupId", descriptor.groupId);
    map.putSingle("artifactId", descriptor.artifactId);
    map.putSingle("version", descriptor.version);
    map.putSingle("classifier", descriptor.classifier);
    map.putSingle("extension", descriptor.extension);
    map
  }
}

/** @author Christian Schlichtherle */
class ConfiguredUpdateServiceITSuite extends JerseyTest { this: TestContext =>

  import ConfiguredUpdateServiceITSuite._

  @Test def testLifeCycle() {
    assertUpdateVersionTo()
  }

  private def assertUpdateVersionTo() {
    val updateVersionTo = updateVersionToAs(TEXT_PLAIN_TYPE)
    updateVersionToAs(APPLICATION_JSON_TYPE) should be ('"' + updateVersionTo + '"')
    updateVersionToAs(APPLICATION_XML_TYPE) should
      be ("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><version>" + updateVersionTo + "</version>")
  }

  private def updateVersionToAs(mediaType: MediaType) =
    resource.path("update/version")
            .queryParams(queryParams(artifactDescriptor))
            .accept(mediaType)
            .get(classOf[String])

  override protected def configure =
    new LowLevelAppDescriptor.Builder(resourceConfig).contextPath("").build

  private def resourceConfig: ResourceConfig = {
    val rc = new DefaultResourceConfig
    rc.getClasses.add(classOf[UpdateService])
    rc.getClasses.add(classOf[UpdateServiceExceptionMapper])
    rc.getSingletons.add(new ContextResolverForArtifactResolver)
    rc
  }

  private final class ContextResolverForArtifactResolver
    extends ContextResolver[ArtifactResolver] {
    override def getContext(ignored: Class[_]) = artifactResolver
  }
}
