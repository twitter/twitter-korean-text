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
import com.twitter.penguin.korean.normalizer.KoreanNormalizer
import com.twitter.penguin.korean.phrase_extractor.KoreanPhraseExtractor.KoreanPhrase
import com.twitter.penguin.korean.util.KoreanDictionaryProvider._

/**
 * Create Korean Phrase Extraction Examples.
 */
object CreatePhraseExtractionExamples extends Runnable {

  case class PhraseExample(text: String, phrases: Seq[KoreanPhrase])

  def run {
    System.err.println("Reading the sample tweets..")

    val phrasePairs = readFileByLineFromResources("example_tweets.txt").flatMap {
      case line if line.length > 0 =>
        val chunk = line.trim
        val normalized = KoreanNormalizer.normalize(chunk)
        val tokens = tokenize(normalized)
        val phrases = extractPhrases(tokens)
        Some(PhraseExample(chunk, phrases))
      case line => None
    }.toSet


    val outputFile: String = "src/test/resources/com/twitter/penguin/korean/util/current_phrases.txt"

    System.err.println("Writing the new phrases to " + outputFile)

    val out = new FileOutputStream(outputFile)
    phrasePairs.toSeq.sortBy(_.text).foreach {
      p =>
        out.write(p.text.getBytes)
        out.write("\t".getBytes)
        out.write(p.phrases.mkString("/").getBytes)
        out.write("\n".getBytes)
    }
    out.close()

    System.err.println("Testing the new phrases " + outputFile)
  }
}
