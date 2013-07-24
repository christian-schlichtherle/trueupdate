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
trait DifferTestContext {
  @CreatesObligation def jarFile1() = new JarFile(file("test1.jar"))
  @CreatesObligation def jarFile2() = new JarFile(file("test2.jar"))

  private def file(name: String) =
    new File((classOf[DifferTestContext] getResource name).toURI)

  def differ = new Differ(MetadataComparator)
}
