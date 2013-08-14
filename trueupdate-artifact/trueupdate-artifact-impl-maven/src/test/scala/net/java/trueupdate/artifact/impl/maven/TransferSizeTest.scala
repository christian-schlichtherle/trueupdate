/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.artifact.impl.maven

import java.util.Locale
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers._
import org.scalatest.prop.PropertyChecks._

/** @author Christian Schlichtherle */
@RunWith(classOf[JUnitRunner])
class TransferSizeTest extends WordSpec {

  "A transfer size" when {
    "given negative sizes" should {
      "throw an IllegalArgumentException" in {
        val table = Table(
          ("size"),
          (-1L),
          (-1024L),
          (Long.MinValue)
        )
        forAll(table) { sizeBytes =>
          intercept[IllegalArgumentException] { new TransferSize(sizeBytes) }
        }
      }
    }

    "given non-negative sizes" should {
      "represent the transferred number of bytes" in {
        val table = Table(
          ("size", "string"),
          (0L, "0.00 bytes"),
          (1L, "1.00 bytes"),
          (1023L, "1,023.00 bytes"),
          (1024L, "1.00 KB"),
          (1024L * 1024 - 1, "1,024.00 KB"),
          (1024L * 1024, "1.00 MB"),
          (1024L * 1024 * 1024 - 1, "1,024.00 MB"),
          (1024L * 1024 * 1024, "1.00 GB"),
          (1024L * 1024 * 1024 * 1024 - 1, "1,024.00 GB"),
          (1024L * 1024 * 1024 * 1024, "1,024.00 GB"),
          (1024L * 1024 * 1024 * 1024 * 1024, "1,048,576.00 GB")
        )
        forAll(table) { (sizeBytes, string) =>
          new TransferSize(sizeBytes).toString(Locale.ENGLISH) should equal (string)
        }
      }
    }
  }
}
