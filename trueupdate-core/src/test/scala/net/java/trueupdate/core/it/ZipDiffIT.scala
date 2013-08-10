/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.it

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
        val diff = withZipDiff(_ computeDiffModel ())
        import collection.JavaConverters._
        import diff._
        removed.asScala map (_.name) should
          equal (List("entryOnlyInFile1"))
        added.asScala map (_.name) should
          equal (List("entryOnlyInFile2"))
        unchanged.asScala map (_.name) should
          equal (List("META-INF/", "META-INF/MANIFEST.MF", "differentEntryTime", "equalEntry"))
        changed.asScala map (_.name) should
          equal (List("differentEntrySize"))
      }
    }
  }
}
