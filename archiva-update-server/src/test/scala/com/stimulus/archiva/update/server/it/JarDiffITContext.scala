/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.it

import java.io.File
import java.util.jar.JarFile
import edu.umd.cs.findbugs.annotations.CreatesObligation
import com.stimulus.archiva.update.server.jardiff.{ContentComparator, JarDiff}
import com.stimulus.archiva.update.server.jardiff.util.MessageDigests

/**
 * @author Christian Schlichtherle
 */
trait JarDiffITContext {
  @CreatesObligation def jarFile1() = new JarFile(file("test1.jar"))
  @CreatesObligation def jarFile2() = new JarFile(file("test2.jar"))

  private def file(resourceName: String) =
    new File((classOf[JarDiffITContext] getResource resourceName).toURI)

  def jarDiff = new JarDiff(comparator)
  def comparator = new ContentComparator(digest)
  def digest = MessageDigests.sha1
}
