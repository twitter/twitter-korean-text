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

package com.twitter.penguin.korean.tools

import java.io.FileOutputStream

import com.twitter.penguin.korean.TwitterKoreanProcessor._
import com.twitter.penguin.korean.tokenizer.KoreanTokenizer.KoreanToken
import com.twitter.penguin.korean.util.KoreanDictionaryProvider._

/**
 * Create Korean Parsing examples.
 */
object CreateParsingExamples extends Runnable  {
  case class ParsingExample(text: String, parse: Seq[KoreanToken])

  def run {
    System.err.println("Reading the goldenset..")

    val parsedPairs = readFileByLineFromResources("example_chunks.txt").flatMap {
      case line if line.length > 0 =>
        val chunk = line.trim
        val parsed = tokenize(chunk)
        Some(ParsingExample(chunk, parsed))
      case line => None
    }.toSet


    val outputFile: String = "src/test/resources/com/twitter/penguin/korean/util/current_parsing.txt"

    System.err.println("Writing the new goldenset to " + outputFile)

    val out = new FileOutputStream(outputFile)
    parsedPairs.toSeq.sortBy(_.text).foreach {
      p =>
        out.write(p.text.getBytes)
        out.write("\t".getBytes)
        out.write(p.parse.mkString("/").getBytes)
        out.write("\n".getBytes)
    }
    out.close()

    System.err.println("Testing the new goldenset " + outputFile)
  }
}
