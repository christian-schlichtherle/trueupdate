/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.jardiff

import org.scalatest.WordSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers._
import org.scalatest.prop.PropertyChecks._
import com.stimulus.archiva.update.core.io.Sources

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class MessageDigestsIT extends WordSpec {

  "Computation of digests" should {
    "work as expected" in {
      val table = Table(
        ("SHA-1", "resource name"),
        ("4690d26560ecaed2e9820e2135cd8eae76003f9f", "test1.jar"),
        ("2ec083ed5a4a914af0d8faf84a9f03c9d4dc2686", "test2.jar"),
        // Note that the most significant bit is set to test signum conversion
        ("f3172822c7d08f23764aa5baee9d73ef32797b46", "twoTimesHelloWorld")
      )
      forAll(table) { (sha1, resourceName) =>
        MessageDigests.digestToHexString(MessageDigests.sha1,
          Sources.forResource(resourceName, classOf[MessageDigestsIT])) should
          equal (sha1)
      }
    }
  }
}
