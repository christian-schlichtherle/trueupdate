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
        val diff = withZipDiff(_ computeZipDiffModel ())
        import collection.JavaConverters._
        import diff._
        removedEntries.asScala map (_.entryName) should
          equal (List("entryOnlyInFile1"))
        addedEntries.asScala map (_.entryName) should
          equal (List("entryOnlyInFile2"))
        unchangedEntries.asScala map (_.entryName) should
          equal (List("META-INF/", "META-INF/MANIFEST.MF", "differentEntryTime", "equalEntry"))
        changedEntries.asScala map (_.entryName) should
          equal (List("differentEntrySize"))
      }
    }
  }
}
