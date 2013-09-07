/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.cargo

import java.net.URI
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers._
import org.scalatest.prop.PropertyChecks._
import org.junit.Ignore

/**
 * @author Christian Schlichtherle
 */
@Ignore("TODO: Needs Tomcat and test WAR installed at hardcoded paths.")
@RunWith(classOf[JUnitRunner])
class CargoContextIT extends WordSpec {

  "A cargo context" should {
    "vend correctly parameterized cargo objects" in {
      val containerId = "tomcat7x"
      val deployableType = "war"
      val deployableLocation = System.getProperty("user.home") +
        "/.m2/repository/net/java/trueupdate/trueupdate-server-appl-tomcat/0.1.10-SNAPSHOT/trueupdate-server-appl-tomcat-0.1.10-SNAPSHOT.war"
      val containerHome = "/Applications/NetBeans/apache-tomcat-7.0.34"
      val table = Table(
        ("contextConfiguration", "deployableType", "deployableLocation", "containerId", "containerType", "containerHome", "configurationType", "configurationHome"),
        //("%s://%s?context.container.id=%s&context.container.type=installed&context.container.home=%s&context.configuration.type=standalone".format(deployableType, deployableLocation, containerId, containerHome), "war", deployableLocation, containerId, "installed", containerHome, "standalone", ""),
        ("%s://%s?context.container.id=%s&context.container.type=installed&context.container.home=%s".format(deployableType, deployableLocation, containerId, containerHome), "war", deployableLocation, containerId, "installed", containerHome, "existing", containerHome),
        ("%s://%s?context.container.id=%s&cargo.remote.username=admin&cargo.remote.password=admin".format(deployableType, deployableLocation, containerId), "war", deployableLocation, containerId, "remote", "", "runtime", "")
      )
      forAll(table) { (contextConfiguration, deployableType, deployableLocation, containerId, containerType, containerHome, configurationType, configurationHome) =>
        val context = new CargoContext(new URI(contextConfiguration))

        context.deployableType.getType should be (deployableType)
        context.deployableLocation should be (deployableLocation)
        context.containerId should be (containerId)
        context.containerType.getType should be (containerType)
        context.containerHome should be (containerHome)
        context.configurationType.getType should be (configurationType)
        context.configurationHome should be (configurationHome)

        context.configuration should not be (null)
        context.container should not be (null)
        context.deployer should not be (null)
        context.deployable should not be (null)
      }
    }
  }
}
