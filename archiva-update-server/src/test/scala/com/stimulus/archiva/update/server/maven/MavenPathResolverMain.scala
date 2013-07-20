/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.maven

import com.stimulus.archiva.update.server.finder.ArtifactDescriptor
import org.eclipse.aether.repository._

/** @author Christian Schlichtherle */
object MavenPathResolverMain {

  def main(args: Array[String]) {
    System.out.println("Resolved artifact path: " +
      resolver.resolveArtifactPath(descriptor))
    System.out.println("Resolved update path: " +
      resolver.resolveUpdatePath(descriptor))
  }

  private lazy val resolver =
    new MavenPathResolver(userRepository, centralRepository)

  private def userRepository = new LocalRepository(userHome + "/.m2/repository")

  private def userHome = System.getProperty("user.home")

  private def centralRepository = new RemoteRepository.Builder(
    "central", "default", "http://repo1.maven.org/maven2/").build

  private def descriptor =
    new ArtifactDescriptor.Builder()
      .groupId("net.java.truevfs")
      .artifactId("truevfs-profile-full")
      .classifier("shaded")
      .extension("jar")
      .version("0.9")
      .build
}
