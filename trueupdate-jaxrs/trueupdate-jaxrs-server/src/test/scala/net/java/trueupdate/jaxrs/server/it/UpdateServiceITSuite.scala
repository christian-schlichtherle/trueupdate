/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.jaxrs.server.it

import com.sun.jersey.test.framework._
import java.io._
import java.util.zip.ZipInputStream
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MediaType._
import net.java.trueupdate.core.io._
import net.java.trueupdate.core.zip.model.DiffModel
import net.java.trueupdate.jaxrs.client.UpdateClient
import org.junit.Test
import org.scalatest.matchers.ShouldMatchers._
import net.java.trueupdate.artifact.spec._
import net.java.trueupdate.core.TestContext
import java.util.logging.Level

/** @author Christian Schlichtherle */
abstract class UpdateServiceITSuite extends JerseyTest {
  context: TestContext =>

  def artifactDescriptor: ArtifactDescriptor

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
    updateClient.version(artifactDescriptor, mediaType)

  private def assertDiff() {
    val updateVersion = updateVersionAs(TEXT_PLAIN_TYPE)
    val source = updateClient diff (artifactDescriptor, updateVersion)

    class DiffTask extends InputTask[Unit, IOException] {
      override def execute(in: InputStream) {
        val zipIn = new ZipInputStream(in)
        val entry = zipIn getNextEntry ()
        entry.getName should be (DiffModel.ENTRY_NAME)
        val source = new Source {
          def input() = new FilterInputStream(zipIn) {
            override def close() { zipIn closeEntry () }
          }
        }
        val store = memoryStore
        Copy copy (source, store)
        store.data.length should be > (0)
        logger log (Level.FINE, "\n{0}", utf8String(store))
      }
    }

    Sources execute new DiffTask on source
  }

  private def updateClient = new UpdateClient(resource.getURI, client)
}
