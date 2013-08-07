/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.artifact.maven.it

import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers._
import net.java.trueupdate.artifact.maven.MavenArtifactResolverAdapter

/** @author Christian Schlichtherle */
@RunWith(classOf[JUnitRunner])
class MavenArtifactResolverAdapterTest
extends WordSpec with MavenArtifactResolverTestContext {

  def adapter = new MavenArtifactResolverAdapter

  "A maven artifact repository adapter" should {
    "support round-trip conversion" in {
      val original = testRepositories()
      val clone = adapter marshal (adapter unmarshal original)
      clone should equal (original)
      clone should not be theSameInstanceAs (original)
    }
  }
}
