/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.it

import java.io.File
import java.util.jar.JarFile
import edu.umd.cs.findbugs.annotations.CreatesObligation
import com.stimulus.archiva.update.server.jardiff.JarDiff
import com.stimulus.archiva.update.server.util.MessageDigests

/**
 * @author Christian Schlichtherle
 */
trait JarDiffITContext {

  def index() = withJars(new JarDiff(_, _).compute(digest))

  def withJars[A](fun: (JarFile, JarFile) => A) = {
    val jar1 = jarFile1()
    try {
      val jar2 = jarFile2()
      try {
        fun(jar1, jar2)
      } finally {
        jar2 close ()
      }
    } finally {
      jar1 close ()
    }
  }

  @CreatesObligation def jarFile1() = new JarFile(file("test1.jar"))
  @CreatesObligation def jarFile2() = new JarFile(file("test2.jar"))

  private def file(resourceName: String) =
    new File((classOf[JarDiffITContext] getResource resourceName).toURI)


  def digest = MessageDigests.sha1
}
