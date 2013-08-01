/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package com.stimulus.archiva.update.core.digest

import com.stimulus.archiva.update.core.io.Sources
import org.scalatest.WordSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers._
import org.scalatest.prop.PropertyChecks._

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class MessageDigestsTest extends WordSpec {

  "Computation of digests" should {
    "yield correct results" in {
      val sha1 = MessageDigests.sha1
      val table = Table(
        ("SHA-1 digest", "resource name"),
        ("47a013e660d408619d894b20806b1d5086aab03b", "helloWorld"),
        // Note that the most significant bit is set to test signum conversion
        ("f3172822c7d08f23764aa5baee9d73ef32797b46", "twoTimesHelloWorld")
      )
      forAll(table) { (digest, resourceName) =>
        MessageDigests.digestToHexString(sha1,
          Sources.forResource(resourceName, classOf[MessageDigestsTest])) should
          equal (digest)
      }
    }
  }
}
