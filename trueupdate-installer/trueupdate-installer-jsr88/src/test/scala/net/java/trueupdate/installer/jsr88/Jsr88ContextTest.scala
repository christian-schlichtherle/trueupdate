/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.jsr88

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.WordSpec
import org.scalatest.Matchers._
import org.scalatest.prop.PropertyChecks._
import java.net.URI
import javax.enterprise.deploy.spi.factories.DeploymentFactory
import org.scalatest.mock.MockitoSugar.mock
import javax.enterprise.deploy.shared.{CommandType, StateType, ModuleType}
import java.io.{FileOutputStream, File}
import javax.enterprise.deploy.spi.{TargetModuleID, DeploymentManager}
import org.mockito.Matchers._
import org.mockito.Mockito._
import net.java.trueupdate.manager.spec.tx.Commands
import javax.enterprise.deploy.spi.status.{DeploymentStatus, ProgressObject}
import javax.enterprise.deploy.spi
import net.java.trueupdate.installer.core.io.Files

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class Jsr88ContextTest extends WordSpec {

  "A JSR 88 context" should {
    "behave correctly" in {
      val dir = File.createTempFile("tmp", ".dir")
      Files deletePath dir
      dir mkdir ()
      try {
        val subjects = Table(
          ("location", "moduleType", "moduleArchive", "moduleID", "uri", "username", "password", "deploymentPlan"),
          ("ear://" + dir.getPath + "/moduleArchive.ear?moduleID=product&username=christian&password=secret&deploymentPlan=/deployment.plan&uri=foo:bar", ModuleType.EAR, new File(dir, "/moduleArchive.ear"), "product", "foo:bar", "christian", "secret", new File("/deployment.plan"))
        )
        forAll(subjects) { (location, moduleType, moduleArchive, moduleID, uri, username, password, deploymentPlan) =>

          new FileOutputStream(moduleArchive) close ()

          val target = mock[spi.Target]
          when(target.getName) thenReturn "server"
          when(target.getDescription) thenReturn "description"

          val targets = Array[spi.Target](target)

          val tmid = mock[TargetModuleID]
          when(tmid.getModuleID) thenReturn moduleID
          when(tmid.getTarget) thenReturn target

          val tmids = Array[spi.TargetModuleID](tmid)

          val stopDs = mock[DeploymentStatus]
          when(stopDs.getCommand) thenReturn CommandType.STOP
          when(stopDs.getState) thenReturn StateType.COMPLETED

          val stopPo = mock[ProgressObject]
          when(stopPo.getDeploymentStatus) thenReturn stopDs

          val undeployDs = mock[DeploymentStatus]
          when(undeployDs.getCommand) thenReturn CommandType.UNDEPLOY
          when(undeployDs.getState) thenReturn StateType.COMPLETED

          val undeployPo = mock[ProgressObject]
          when(undeployPo.getDeploymentStatus) thenReturn undeployDs

          val distributeDs = mock[DeploymentStatus]
          when(distributeDs.getCommand) thenReturn CommandType.DISTRIBUTE
          when(distributeDs.getState) thenReturn StateType.COMPLETED

          val distributePo = mock[ProgressObject]
          when(distributePo.getDeploymentStatus) thenReturn distributeDs

          val startDs = mock[DeploymentStatus]
          when(startDs.getCommand) thenReturn CommandType.START
          when(startDs.getState) thenReturn StateType.COMPLETED

          val startPo = mock[ProgressObject]
          when(startPo.getDeploymentStatus) thenReturn startDs

          val dm = mock[DeploymentManager]
          when(dm.getTargets) thenReturn targets
          when(dm.getAvailableModules(moduleType, targets)) thenReturn tmids
          when(dm stop any()) thenReturn stopPo
          when(dm undeploy any()) thenReturn undeployPo
          when(dm distribute (targets, moduleArchive, deploymentPlan)) thenReturn distributePo
          when(dm start any()) thenReturn startPo

          val df = mock[DeploymentFactory]
          when(df getDeploymentManager (uri, username, password)) thenReturn dm

          val ctx = new Jsr88Context(new URI(location), df)
          ctx.moduleType() should be (moduleType)
          ctx.moduleArchive() should be (moduleArchive)
          ctx.moduleID() should be (moduleID)
          ctx.uri() should be (uri)
          ctx.username() should be (username)
          ctx.password() should be (password)
          ctx.deploymentPlan() should be (deploymentPlan)

          Commands execute ctx.undeploymentTransaction
          Commands execute ctx.deploymentTransaction
        }
      } finally {
        Files deletePath dir
      }
    }
  }
}
