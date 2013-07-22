package com.stimulus.archiva.update.server.maven

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers._
import java.io.File

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class ContextResolverForArtifactResolverTest extends WordSpec {

  private def localRepositoryDir =
    new File(System.getProperty("user.home"), ".m2/repository");

  "A context resolver for artifact resolver" when {
    "its system property is not set" should {
      System.getProperty(ContextResolverForArtifactResolver.propertyKey) should
        be (null)

      "default to the user's local Maven repository" in {
        ContextResolverForArtifactResolver.localRepository.getBasedir should
        be (localRepositoryDir)
      }
    }
  }
}
