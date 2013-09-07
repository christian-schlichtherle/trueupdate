/*
 * Copyright (C) 2013 Stimulus Software & Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package net.java.trueupdate.installer.cargo

import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers._
import org.scalatest.prop.PropertyChecks._
import java.net.URI
import java.{util => ju}

/**
 * @author Christian Schlichtherle
 */
@RunWith(classOf[JUnitRunner])
class UrisTest extends WordSpec {

  private def scala(input: ju.Map[String, ju.List[String]])
  : Map[String, List[String]] = {
    import collection.JavaConverters._
    var output = Map.empty[String, List[String]]
    for ((name, value) <- input.asScala) output += name -> value.asScala.toList
    output
  }

  "The queryParameters function" should {
    "return correctly parsed multi-valued maps" in {
      val table = Table(
        ("uri", "map"),
        ("?name", Map("name" -> List(""))),
        ("?name=", Map("name" -> List(""))),
        ("?name=value", Map("name" -> List("value"))),
        ("?name=value1&name=value2", Map("name" -> List("value1", "value2"))),
        ("?%26name=%26value1&%26name=%26value2", Map("&name" -> List("&value1", "&value2")))
      )
      forAll(table) { (uri, map) =>
        scala(Uris.queryParameters(new URI(uri))) should equal (map)
      }
    }
  }
}
