/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.it

import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import net.java.trueupdate.core.io.MessageDigests.sha1
import net.java.trueupdate.core.zip.model.DiffModel

/** @author Christian Schlichtherle */
@RunWith(classOf[JUnitRunner])
class DiffModelIT extends WordSpec with ZipITContext {

  "A diff model" when {
    "constructed with no data" should {
      "be round-trip XML-serializable" in {
        assertRoundTripXmlSerializable(DiffModel.builder.messageDigest(sha1).build)
      }
    }

    "computed from a ZIP diff" should {
      "be round-trip XML-serializable" in {
        assertRoundTripXmlSerializable(loanZipDiff(_ model ()))
      }
    }
  }
}
