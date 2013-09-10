/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.tomcat

import java.io.File
import java.net.URI
import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.junit.Arquillian
import org.jboss.shrinkwrap.api.ShrinkWrap
import org.jboss.shrinkwrap.api.asset.EmptyAsset
import org.jboss.shrinkwrap.api.exporter.ZipExporter
import org.jboss.shrinkwrap.api.spec.WebArchive
import org.junit.Test
import org.junit.runner.RunWith
import net.java.trueupdate.installer.core.io.Files._
import net.java.trueupdate.installer.core.tx.Transactions
import net.java.trueupdate.installer.tomcat.TomcatUpdateInstallerIT._
import net.java.trueupdate.manager.spec.UpdateMessage
import net.java.trueupdate.manager.spec.UpdateMessage.Type

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[Arquillian])
class TomcatUpdateInstallerIT {

  @Test
  def test() {
    val message = updateMessage
    val location = message.currentLocation

    val installer = new TomcatUpdateInstaller
    val appBase = installer.appBase
    val cn = TomcatUpdateInstaller contextName location
    val testWar = new File(appBase, cn.getBaseName + ".war")

    testArchive as classOf[ZipExporter] exportTo testWar
    val context = installer resolveContext (message, location)
    Transactions execute context.deploymentTransaction

    Transactions execute context.undeploymentTransaction
    deletePath(context.path)

    updateArchive as classOf[ZipExporter] exportTo testWar
    Transactions execute context.deploymentTransaction
  }
}

object TomcatUpdateInstallerIT {

  @Deployment
  def createDeployment = ShrinkWrap
    .create(classOf[WebArchive])
    .addClass(classOf[TomcatUpdateInstaller])

  def testArchive = ShrinkWrap
    .create(classOf[WebArchive])
    .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml") // can't be empty

  def updateArchive = ShrinkWrap
    .create(classOf[WebArchive])
    .addClass(classOf[HelloWorld])

  def updateMessage = UpdateMessage
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
    .currentLocation(new URI("context:/test"))
    .updateLocation(new URI("context:/test"))
    .build
}
