/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.core

import java.io._
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers._
import org.scalatest.mock.MockitoSugar.mock
import net.java.trueupdate.core.io._
import net.java.trueupdate.core.zip.diff.ZipDiff
import net.java.trueupdate.core.zip.io.JarFileStore
import net.java.trueupdate.installer.core.LocalUpdateInstaller.LocationContext
import net.java.trueupdate.installer.core.io.Files._
import net.java.trueupdate.installer.core.io.PathTask
import net.java.trueupdate.installer.core.tx.Transaction
import net.java.trueupdate.manager.spec._
import net.java.trueupdate.message._
import LogMessage.Level
import UpdateMessage.Type

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class LocalUpdateInstallerIT extends WordSpec {

  def updateMessage(deployedPath: File) = UpdateMessage
    .builder
    .from("agent")
    .to("manager")
    .`type`(Type.INSTALLATION_REQUEST)
    .artifactDescriptor
    .groupId("groupId")
    .artifactId("artifactId")
    .extension("jar")
    .version("1")
    .inject
    .updateVersion("2")
    .currentLocation(deployedPath.getPath)
    .updateLocation(deployedPath.getPath)
    .build

  def updateInstaller: UpdateInstaller = new LocalUpdateInstaller {
    def locationContext(context: UpdateContext, location: String) =
      new LocationContext {
        def path = new File(location)
        def deploymentTransaction() = mock[Transaction]
        def undeploymentTransaction() = mock[Transaction]
      }
  }

  def updateContext(deployedPath: File, diffZipFile: File): UpdateContext =
    new UpdateContext {
      val um = updateMessage(deployedPath)

      def updateDescriptor = um.updateDescriptor
      def currentLocation = um.currentLocation
      def updateLocation = um.updateLocation
      def diffZip = diffZipFile
      def isLoggable(level: Level) = true
      def log(level: Level, code: String, args: AnyRef*) { }
      def prepareUndeployment() { }
      def performUndeployment() { }
      def rollbackUndeployment() { }
      def commitUndeployment() { }
    }

  "A local update installer" should {
    "install the update" in {
      loanTempDir(new PathTask[Unit, Exception] {
        def execute(tempDir: File) {
          val content = new File(tempDir, "content")
          Sinks execute new OutputTask[Unit, IOException] {
            def execute(out: OutputStream) { out write 0 }
          } on content

          val input1Jar = new File(tempDir, "input1.jar")
          zip(new JarFileStore(input1Jar), content, content.getName)

          Sinks execute new OutputTask[Unit, IOException] {
            def execute(out: OutputStream) { out write 0; out write 0 }
          } on content
          val input2Jar = new File(tempDir, "input2.jar")

          zip(new JarFileStore(input2Jar), content, content.getName)
          input1Jar.length should not be input2Jar.length

          val diffZipFile = new File(tempDir, "diff.zip")
          ZipDiff
            .builder
            .input1(input1Jar)
            .input2(input2Jar)
            .build
            .output(diffZipFile)

          val deployedZip = new File(tempDir, "deployed.zip")
          copyFile(input1Jar, deployedZip)
          deployedZip.length should be (input1Jar.length)

          updateInstaller install updateContext(deployedZip, diffZipFile)
          deployedZip.length should be (input2Jar.length)

          val deployedDir = new File(tempDir, "deployed.dir")
          deletePath(deployedDir)
          unzip(input1Jar, deployedDir)

          val deployedContent = new File(deployedDir, content.getName)
          deployedContent.length should be (1)
          updateInstaller install updateContext(deployedDir, diffZipFile)
          deployedContent.length should be (2)
        }
      }, "dir", null, null)
    }
  }
}
