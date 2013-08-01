/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.core.it

import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers._

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class ZipDiffIT extends WordSpec with ZipITContext {

  "A JAR diff" when {
    "computing  the test JAR files" should {
      "partition the entry names and digests correctly" in {
        val diff = withZipDiff(_ computeDiff ())
        import collection.JavaConverters._
        import diff._
        removed.values.asScala map (_.name) should
          equal (List("entryOnlyInFile1"))
        added.values.asScala map (_.name) should
          equal (List("entryOnlyInFile2"))
        unchanged.values.asScala map (_.name) should
          equal (List("META-INF/", "META-INF/MANIFEST.MF", "differentEntryTime", "equalEntry"))
        changed.values.asScala map (_.name) should
          equal (List("differentEntrySize"))
      }
    }
  }
}
