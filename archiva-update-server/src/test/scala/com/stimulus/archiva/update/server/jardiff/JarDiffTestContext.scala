/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jardiff

import java.io.File
import java.util.jar.JarFile
import edu.umd.cs.findbugs.annotations.CreatesObligation

/**
 * @author Christian Schlichtherle
 */
trait JarDiffTestContext {
  @CreatesObligation def jarFile1() = new JarFile(file("test1.jar"))
  @CreatesObligation def jarFile2() = new JarFile(file("test2.jar"))

  private def file(resourceName: String) =
    new File((classOf[JarDiffTestContext] getResource resourceName).toURI)

  def jarDiff = new JarDiff(MetadataComparator)
}
