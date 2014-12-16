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

package com.twitter.penguin.korean.util

import com.twitter.penguin.korean.util.KoreanConjugation._
import com.twitter.penguin.korean.util.KoreanDictionaryProvider._
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.collection.JavaConversions._

@RunWith(classOf[JUnitRunner])
class KoreanConjugationTest extends FunSuite {

  def matchGoldenset(predicate: String, newExpanded: CharArraySet, examples: String): Boolean = {
    val newExpandedString = newExpanded.map { case word: Array[Char] => new String(word)}.toSeq.sorted.mkString(", ")
    val isSameToGoldenset = newExpandedString == examples
    if (!isSameToGoldenset) {
      System.err.println(("%s:\n" +
          "  Previous: %s\n" +
          "  New: %s").format(
            predicate,
            examples,
            newExpandedString
          ))
    }
    isSameToGoldenset
  }

  def assertConjugations(filename: String, isAdjective: Boolean) {
    val input = readFileByLineFromResources(filename)
    val loaded: Seq[(String, String)] = input.toSeq.map {
      s =>
        val sp = s.split("\t")
        (sp(0), sp(1))
    }

    assert(
      loaded.foldLeft(true) {
        case (output: Boolean, (predicate: String, goldensetExpanded: String)) =>
          matchGoldenset(
            predicate,
            conjugatePredicatesToCharArraySet(Set(predicate), isAdjective),
            goldensetExpanded
          ) && output
      }
    )
  }

  test("conjugatePredicates should expand codas of verbs correctly") {
    assertConjugations("verb_conjugate.txt", isAdjective = false)
  }

  test("conjugatePredicates should expand codas of adjectives correctly") {
    assertConjugations("adj_conjugate.txt", isAdjective = true)
  }
}

