/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.core.it

import com.stimulus.archiva.update.core.zip.model.Diff
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers._

/** @author Christian Schlichtherle */
@RunWith(classOf[JUnitRunner])
class DiffIT extends WordSpec with ZipITContext {

  def roundTrip(original: Diff) {
    val store = memoryStore
    jaxbCodec encode (store, original)
    logger debug ("\n{}", utf8String(store))
    val clone: Diff = jaxbCodec decode (store, classOf[Diff])
    clone should equal (original)
    clone should not be theSameInstanceAs (original)
  }

  "A diff model" when {
    "constructed with no data" should {
      "be round-trip XML-serializable" in {
        roundTrip(new Diff)
      }
    }

    "computed from a ZIP diff" should {
      "be round-trip XML-serializable" in {
        roundTrip(withZipDiff(_ computeDiff ()))
      }
    }
  }
}
