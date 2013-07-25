/*
 * Copyright (C) 2005-2013 Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.server.it

import org.scalatest.WordSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers._
import org.scalatest.prop.PropertyChecks._
import com.stimulus.archiva.update.core.io.Sources
import com.stimulus.archiva.update.server.jardiff.util.MessageDigests

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class MessageDigestsIT extends WordSpec {

  "Computation of digests" should {
    "work as expected" in {
      val table = Table(
        ("SHA-1", "resource name"),
        ("47a013e660d408619d894b20806b1d5086aab03b", "helloWorld"),
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
