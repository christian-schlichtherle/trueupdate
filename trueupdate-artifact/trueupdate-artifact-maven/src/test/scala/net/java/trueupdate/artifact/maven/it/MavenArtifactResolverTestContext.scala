/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.artifact.maven.it

import net.java.trueupdate.artifact.spec._
import net.java.trueupdate.core.TestContext
import javax.xml.bind.JAXBContext
import net.java.trueupdate.artifact.maven._
import net.java.trueupdate.artifact.maven.dto.MavenParametersDto

/** @author Christian Schlichtherle */
trait MavenArtifactResolverTestContext
extends TestContext with ArtifactResolverTestContext {

  override def artifactResolver = new MavenArtifactResolver(parameters)

  val parameters = MavenParameters.builder.parse(configuration()).build

  private def configuration() =
    jaxbContext
      .createUnmarshaller
      .unmarshal(classOf[MavenArtifactResolverTestContext]
        .getResource("repositories.xml"))
      .asInstanceOf[MavenParametersDto]

  final override lazy val jaxbContext =
    JAXBContext.newInstance(classOf[MavenParametersDto])
}
