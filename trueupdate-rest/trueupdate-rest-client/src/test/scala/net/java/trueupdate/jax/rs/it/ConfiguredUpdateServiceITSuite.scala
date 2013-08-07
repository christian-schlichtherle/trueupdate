/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jax.rs.it

import com.sun.jersey.api.core._
import com.sun.jersey.test.framework._
import java.io.FilterInputStream
import java.util.zip.ZipInputStream
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MediaType._
import javax.ws.rs.ext.ContextResolver
import net.java.trueupdate.core.TestContext
import net.java.trueupdate.core.io._
import net.java.trueupdate.core.util.Loan._
import net.java.trueupdate.core.zip.model.Diff
import net.java.trueupdate.jax.rs.server._
import net.java.trueupdate.jax.rs.client.ConfiguredUpdateClient
import net.java.trueupdate.repository.spec._
import org.junit.Test
import org.scalatest.matchers.ShouldMatchers._
import org.slf4j.LoggerFactory
import ConfiguredUpdateServiceITSuite._

private object ConfiguredUpdateServiceITSuite {

  val logger = LoggerFactory.getLogger(classOf[ConfiguredUpdateServiceITSuite])
}

/** @author Christian Schlichtherle */
class ConfiguredUpdateServiceITSuite extends JerseyTest {
  this: TestContext with ArtifactRepositoryTestContext =>

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
    updateClient.version(artifactDescriptor, mediaType)

  private def assertUpdatePatch() {
    val updateVersion = updateVersionAs(TEXT_PLAIN_TYPE)
    val source = updateClient.patch(artifactDescriptor, updateVersion)
    loan(new ZipInputStream(source input ())) to { zipIn =>
      val entry = zipIn getNextEntry ()
      entry.getName should be (Diff.ENTRY_NAME)
      val source = new Source {
        def input() = new FilterInputStream(zipIn) {
          override def close() { zipIn closeEntry () }
        }
      }
      val store = memoryStore
      Copy copy (source, store)
      logger debug ("\n{}", utf8String(store))
    }
  }

  private def updateClient =
    new ConfiguredUpdateClient(getBaseURI, client)

  override protected def configure =
    new LowLevelAppDescriptor.Builder(resourceConfig).contextPath("").build

  private def resourceConfig = {
    val rc = new DefaultResourceConfig
    rc.getClasses.add(classOf[UpdateServer])
    rc.getClasses.add(classOf[UpdateServiceExceptionMapper])
    rc.getSingletons.add(new ContextResolverForArtifactResolver)
    rc
  }

  private class ContextResolverForArtifactResolver
    extends ContextResolver[ArtifactRepository] {
    override def getContext(ignored: Class[_]) = artifactRepository
  }
}
