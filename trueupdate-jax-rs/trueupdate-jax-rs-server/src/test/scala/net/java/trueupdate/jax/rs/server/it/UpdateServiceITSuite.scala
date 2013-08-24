/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jax.rs.server.it

import com.sun.jersey.test.framework._
import java.io.FilterInputStream
import java.util.zip.ZipInputStream
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MediaType._
import net.java.trueupdate.core.io._
import net.java.trueupdate.core.it.Loan._
import net.java.trueupdate.core.zip.model.ZipDiffModel
import net.java.trueupdate.jax.rs.client.UpdateClient
import org.junit.Test
import org.scalatest.matchers.ShouldMatchers._
import net.java.trueupdate.core.TestContext
import net.java.trueupdate.artifact.spec.ArtifactResolverTestContext
import java.util.logging.Level

/** @author Christian Schlichtherle */
class UpdateServiceITSuite extends JerseyTest {
  context: TestContext with ArtifactResolverTestContext =>

  @Test def testLifeCycle() {
    assertVersion()
    assertDiff()
  }

  private def assertVersion() {
    val updateVersion = updateVersionAs(TEXT_PLAIN_TYPE)
    logger log (Level.FINE, "Resolved update for artifact {0} to version {1}.",
      Array(artifactDescriptor, updateVersion))
    updateVersionAs(APPLICATION_JSON_TYPE) should
      be ('"' + updateVersion + '"')
    updateVersionAs(APPLICATION_XML_TYPE) should
      be ("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><version>" + updateVersion + "</version>")
  }

  private def updateVersionAs(mediaType: MediaType) =
    artifactUpdateClient.version(artifactDescriptor, mediaType)

  private def assertDiff() {
    val updateVersion = updateVersionAs(TEXT_PLAIN_TYPE)
    val source = artifactUpdateClient.diff(artifactDescriptor, updateVersion)
    loan(new ZipInputStream(source input ())) to { zipIn =>
      val entry = zipIn getNextEntry ()
      entry.getName should be (ZipDiffModel.ENTRY_NAME)
      val source = new Source {
        def input() = new FilterInputStream(zipIn) {
          override def close() { zipIn closeEntry () }
        }
      }
      val store = memoryStore
      Copy copy (source, store)
      logger log (Level.FINE, "\n{0}", utf8String(store))
    }
  }

  private def artifactUpdateClient =
    new UpdateClient(resource.getURI, client)
}
