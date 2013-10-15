/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.tomcat

import java.io.File
import org.apache.catalina.util.ContextName
import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.arquillian.junit.Arquillian
import org.jboss.shrinkwrap.api.ShrinkWrap
import org.jboss.shrinkwrap.api.exporter.ZipExporter
import org.jboss.shrinkwrap.api.spec.WebArchive
import org.junit.Test
import org.junit.runner.RunWith
import net.java.trueupdate.installer.core.io.Files._
import net.java.trueupdate.installer.tomcat.TomcatUpdateInstallerIT._
import net.java.trueupdate.manager.spec._
import net.java.trueupdate.manager.spec.tx._

/**
 * @author Christian Schlichtherle
 */
object TomcatUpdateInstallerIT {

  @Deployment def createDeployment = ShrinkWrap
    .create(classOf[WebArchive])
    .addClass(classOf[TomcatUpdateInstaller])

  def testArchive = ShrinkWrap
    .create(classOf[WebArchive])
    .addClass(classOf[GoodbyeWorld])

  def updateArchive = ShrinkWrap
    .create(classOf[WebArchive])
    .addClass(classOf[HelloWorld])
}

@RunWith(classOf[Arquillian])
class TomcatUpdateInstallerIT {

  @Test def test() {
    val location = "/test"

    val installer = new TomcatUpdateInstaller
    val appBase = installer.appBase
    val testWar = new File(appBase,
                           new ContextName(location).getBaseName + ".war")

    testArchive as classOf[ZipExporter] exportTo testWar
    val ad = installer updateParameters (new UpdateContext {
        override def currentLocation = location
        override def updateLocation = location
        override def deltaZip = null
        override def decorate(id: Action, tx: Transaction) = tx
      })
    Transactions execute ad.deploymentTransaction

    Transactions execute ad.undeploymentTransaction
    deletePath(ad.currentPath)

    updateArchive as classOf[ZipExporter] exportTo testWar
    Transactions execute ad.deploymentTransaction
  }
}
