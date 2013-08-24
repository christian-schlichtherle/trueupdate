/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.core.util

import org.scalatest.WordSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers._
import org.scalatest.prop.PropertyChecks._
import net.java.trueupdate.core.io.Sources

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class MessageDigestsTest extends WordSpec {

  "Computation of digests" should {
    "yield correct values" in {
      val table = Table(
        ("SHA-1 digest reference value", "resource name"),
        ("47a013e660d408619d894b20806b1d5086aab03b", "helloWorld"),
        // Note that the most significant bit is set to test signum conversion
        ("f3172822c7d08f23764aa5baee9d73ef32797b46", "twoTimesHelloWorld")
      )
      forAll(table) { (referenceValue, resourceName) =>
        import MessageDigests._
        val digest = sha1
        updateDigestFrom(digest,
          Sources.forResource(resourceName, classOf[MessageDigestsTest]))
        valueOf(digest) should equal (referenceValue)
      }
    }
  }
}
