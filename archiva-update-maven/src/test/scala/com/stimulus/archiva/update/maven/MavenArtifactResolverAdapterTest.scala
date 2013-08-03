/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.maven

import com.stimulus.archiva.update.core.TestContext
import com.stimulus.archiva.update.core.io.Sources
import com.stimulus.archiva.update.maven.model.Repositories
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers._

/** @author Christian Schlichtherle */
@RunWith(classOf[JUnitRunner])
class MavenArtifactResolverAdapterTest extends WordSpec with TestContext {

  def repositories(): Repositories =
    jaxbCodec decode (config, classOf[Repositories])

  def config = Sources.forResource("repositories.xml",
    classOf[MavenArtifactResolverAdapterTest])

  override lazy val jaxbContext = Repositories.jaxbContext

  def adapter = new MavenArtifactResolverAdapter

  "A maven artifact resolver adapter" should {
    "support round-trip conversion" in {
      val original = repositories()
      val clone = adapter marshal (adapter unmarshal original)
      clone should equal (original)
      clone should not be theSameInstanceAs (original)
    }
  }
}
