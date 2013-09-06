/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.manager.core

import java.io._
import java.net.URI
import org.junit.runner.RunWith
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers._
import org.scalatest.mock.MockitoSugar.mock
import net.java.trueupdate.manager.spec.UpdateMessage
import net.java.trueupdate.manager.spec.UpdateMessage.Type
import net.java.trueupdate.manager.core.io.Files._
import net.java.trueupdate.manager.core.io.PathTask
import net.java.trueupdate.core.io._
import net.java.trueupdate.core.zip.diff.ZipDiff
import net.java.trueupdate.manager.core.tx.Transaction
import net.java.trueupdate.core.zip.JarFileStore

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class LocalUpdateInstallerIT extends WordSpec {

  def updateMessage(deployedPath: File) = UpdateMessage
    .builder
    .from(new URI("agent"))
    .to(new URI("manager"))
    .`type`(Type.INSTALLATION_REQUEST)
    .artifactDescriptor
    .groupId("groupId")
    .artifactId("artifactId")
    .extension("jar")
    .version("1")
    .inject
    .updateVersion("2")
    .currentLocation(deployedPath.toURI)
    .updateLocation(deployedPath.toURI)
    .build

  def updateInstaller = new LocalUpdateInstaller {
    protected def resolvePath(location: URI) = new File(location)
    protected def deploymentTx(location: URI) = mock[Transaction]
    protected def undeploymentTx(location: URI) = mock[Transaction]
  }

  "A basic update installer" should {
    "install the update" in {
      loanTempFile(new PathTask[Unit, Exception] {
        def execute(content: File) {
          loanTempFile(new PathTask[Unit, Exception] {
            def execute(input1Jar: File) {

              Sinks execute new OutputTask[Unit, IOException] {
                def execute(out: OutputStream) { out write 0 }
              } on content
              zip(new JarFileStore(input1Jar), content, content.getName)

              loanTempFile(new PathTask[Unit, Exception] {
                def execute(input2Jar: File) {

                  Sinks execute new OutputTask[Unit, IOException] {
                    def execute(out: OutputStream) { out write 0; out write 0 }
                  } on content
                  zip(new JarFileStore(input2Jar), content, content.getName)

                  input1Jar.length should not be (input2Jar.length)

                  loanTempFile(new PathTask[Unit, Exception] {
                    def execute(diffZip: File) {

                      ZipDiff
                        .builder
                        .input1(input1Jar)
                        .input2(input2Jar)
                        .build
                        .output(diffZip)

                      val resolver = mock[UpdateResolver]
                      when(resolver resolveZipDiffFile any()) thenReturn diffZip

                      loanTempFile(new PathTask[Unit, Exception] {
                        def execute(deployedZip: File) {

                          copyFile(input1Jar, deployedZip)

                          deployedZip.length should be (input1Jar.length)
                          updateInstaller install (resolver, updateMessage(deployedZip))
                          deployedZip.length should be (input2Jar.length)
                        }
                      }, "deployed", ".zip")

                      loanTempFile(new PathTask[Unit, Exception] {
                        def execute(deployedDir: File) {

                          deletePath(deployedDir)
                          unzip(input1Jar, deployedDir)

                          val deployedContent = new File(deployedDir, content.getName)
                          deployedContent.length should be (1)
                          updateInstaller install (resolver, updateMessage(deployedDir))
                          deployedContent.length should be (2)
                        }
                      }, "deployed", ".dir")
                    }
                  }, "diff", ".zip")
                }
              }, "input2", ".jar")
            }
          }, "input1", ".jar")
        }
      }, "content")
    }
  }
}
