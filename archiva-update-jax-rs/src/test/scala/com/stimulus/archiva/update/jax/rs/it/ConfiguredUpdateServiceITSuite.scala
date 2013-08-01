/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.jax.rs.it

import com.stimulus.archiva.update.core.artifact._
import com.stimulus.archiva.update.core.io._
import com.stimulus.archiva.update.core.io.Loan._
import com.stimulus.archiva.update.core.it.ArtifactITContext
import com.stimulus.archiva.update.core.zip.model.Diffs
import com.stimulus.archiva.update.jax.rs._
import com.sun.jersey.api.client.ClientResponse
import com.sun.jersey.api.client.ClientResponse.Status
import com.sun.jersey.api.core._
import com.sun.jersey.core.util.MultivaluedMapImpl
import com.sun.jersey.test.framework._
import java.io.FilterInputStream
import java.util.zip.ZipInputStream
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MediaType._
import javax.ws.rs.ext.ContextResolver
import org.junit.Test
import org.scalatest.matchers.ShouldMatchers._
import org.slf4j.LoggerFactory
import ConfiguredUpdateServiceITSuite._

private object ConfiguredUpdateServiceITSuite {

  private val logger = LoggerFactory.getLogger(
    classOf[ConfiguredUpdateServiceITSuite])

  private def queryParams(descriptor: ArtifactDescriptor) = {
    val map = new MultivaluedMapImpl
    map.putSingle("groupId", descriptor.groupId)
    map.putSingle("artifactId", descriptor.artifactId)
    map.putSingle("version", descriptor.version)
    map.putSingle("classifier", descriptor.classifier)
    map.putSingle("extension", descriptor.extension)
    map
  }
}

/** @author Christian Schlichtherle */
class ConfiguredUpdateServiceITSuite
extends JerseyTest { this: ArtifactITContext =>

  @Test def testLifeCycle() {
    assertUpdateVersion()
    assertUpdatePatch()
  }

  private def assertUpdateVersion() {
    val updateVersion = updateVersionAs(TEXT_PLAIN_TYPE)
    logger info ("Resolved update for artifact {} to version {}.",
      artifactDescriptor, updateVersion)
    updateVersionAs(APPLICATION_JSON_TYPE) should
      be ('"' + updateVersion + '"')
    updateVersionAs(APPLICATION_XML_TYPE) should
      be ("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><version>" + updateVersion + "</version>")
  }

  private def updateVersionAs(mediaType: MediaType) =
    resource.path("update/version")
      .queryParams(queryParams(artifactDescriptor))
      .accept(mediaType)
      .get(classOf[String])

  private def assertUpdatePatch() {
    val updateVersion = updateVersionAs(TEXT_PLAIN_TYPE)
    val response = resource.path("update/patch")
      .queryParams(queryParams(artifactDescriptor))
      .queryParam("update-version", updateVersion)
      .get(classOf[ClientResponse])
    response.getClientResponseStatus should be (Status.OK)
    loan(new ZipInputStream(response getEntityInputStream ())) to { zipIn =>
      val entry = zipIn getNextEntry ()
      entry.getName should be (Diffs.DIFF_ENTRY_NAME)
      val source = new Source {
        def input() = new FilterInputStream(zipIn) {
          override def close() { zipIn closeEntry () }
        }
      }
      val store = new MemoryStore
      Copy copy (source, store)
      logger debug ("\n{}", new String(store.data, "UTF-8"))
    }
  }

  override protected def configure =
    new LowLevelAppDescriptor.Builder(resourceConfig).contextPath("").build

  private def resourceConfig = {
    val rc = new DefaultResourceConfig
    rc.getClasses.add(classOf[UpdateService])
    rc.getClasses.add(classOf[UpdateServiceExceptionMapper])
    rc.getSingletons.add(new ContextResolverForArtifactResolver)
    rc
  }

  private class ContextResolverForArtifactResolver
    extends ContextResolver[ArtifactResolver] {
    override def getContext(ignored: Class[_]) = artifactResolver
  }
}
