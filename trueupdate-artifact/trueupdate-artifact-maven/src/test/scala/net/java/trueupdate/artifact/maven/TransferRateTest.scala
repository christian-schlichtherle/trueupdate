/*
 * Copyright (C) 2013 Schlichtherle IT Services & Stimulus Software.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.artifact.maven

import java.util.Locale

import org.junit.runner.RunWith
import org.scalatest.Matchers._
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.prop.PropertyChecks._

/** @author Christian Schlichtherle */
@RunWith(classOf[JUnitRunner])
class TransferRateTest extends WordSpec {

  "A transfer rate" when {
    "given negative sizes or durations" should {
      "throw an IllegalArgumentException" in {
        val table = Table(
          ("size", "millis"),
          (-1L, 1L),
          (1L, -1L),
          (-1024L, 1L),
          (1024L, -1L),
          (Long.MinValue, 1L),
          (Long.MaxValue, -1L)
        )
        forAll(table) { (sizeBytes, durationMillis) =>
          intercept[IllegalArgumentException] { new TransferRate(sizeBytes, durationMillis) }
        }
      }
    }

    "given non-negative sizes and durations" should {
      "represent the transferred number of bytes" in {
        val table = Table(
          ("size", "millis", "string"),
          (0L, 1000L, "0.00 bytes/second"),
          (1L, 1000L, "1.00 bytes/second"),
          (1023L, 1000L, "1,023.00 bytes/second"),
          (1024L, 1000L, "1.00 KB/second"),
          (1024L * 1024 - 1, 1000L, "1,024.00 KB/second"),
          (1024L * 1024, 1000L, "1.00 MB/second"),
          (1024L * 1024 * 1024 - 1, 1000L, "1,024.00 MB/second"),
          (1024L * 1024 * 1024, 1000L, "1.00 GB/second"),
          (1024L * 1024 * 1024 * 1024 - 1, 1000L, "1,024.00 GB/second"),
          (1024L * 1024 * 1024 * 1024, 1000L, "1,024.00 GB/second"),
          (1024L * 1024 * 1024 * 1024 * 1024, 1000L, "1,048,576.00 GB/second")
        )
        forAll(table) { (sizeBytes, durationMillis, string) =>
          new TransferRate(sizeBytes, durationMillis) toString Locale.ENGLISH should equal (string)
        }
      }
    }
  }
}
