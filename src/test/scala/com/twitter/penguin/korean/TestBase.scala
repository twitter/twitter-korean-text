/*
 * Twitter Korean Text - Scala library to process Korean text
 *
 * Copyright 2014 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twitter.penguin.korean

import java.util.logging.{Logger, Level}

import com.twitter.penguin.korean.util.KoreanDictionaryProvider._
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

object TestBase {

  case class ParseTime(time: Long, chunk: String)

  def time[R](block: => R): Long = {
    val t0 = System.currentTimeMillis()
    block
    val t1 = System.currentTimeMillis()
    t1 - t0
  }

  def assertExamples(exampleFiles: String, log: Logger, f: (String => String)) {
    assert({
      val input = readFileByLineFromResources(exampleFiles)

      val (parseTimes, hasErrors) = input.foldLeft((List[ParseTime](), true)) {
        case ((l: List[ParseTime], output: Boolean), line: String) =>
          val s = line.split("\t")
          val (chunk, parse) = (s(0), if (s.length == 2) s(1) else "")

          val oldTokens = parse
          val t0 = System.currentTimeMillis()
          val newTokens = f(chunk)
          val t1 = System.currentTimeMillis()

          val oldParseMatches = oldTokens == newTokens

          if (!oldParseMatches) {
            System.err.println("Example set match error: %s \n - EXPECTED: %s\n - ACTUAL  : %s".format(
              chunk, oldTokens, newTokens))
          }

          (ParseTime(t1 - t0, chunk) :: l, output && oldParseMatches)
      }

      val averageTime = parseTimes.map(_.time).sum.toDouble / parseTimes.size
      val maxItem = parseTimes.maxBy(_.time)

      log.log(Level.INFO, ("Parsed %d chunks. \n" +
          "       Total time: %d ms \n" +
          "       Average time: %.2f ms \n" +
          "       Max time: %d ms, %s").format(
            parseTimes.size,
            parseTimes.map(_.time).sum,
            averageTime,
            maxItem.time,
            maxItem.chunk
          ))
      hasErrors
    }, "Some parses did not match the example set.")
  }
}

@RunWith(classOf[JUnitRunner])
abstract class TestBase extends FunSuite