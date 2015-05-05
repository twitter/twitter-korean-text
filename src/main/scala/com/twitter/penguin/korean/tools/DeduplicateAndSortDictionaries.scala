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

import scala.io.Source

/**
 * Clean up resources by removing duplicates and sorting.
 */
object DeduplicateAndSortDictionaries extends Runnable  {

  private[this] def readWords(filename: String): Set[String] = {
    Source.fromFile(filename)(io.Codec("UTF-8"))
        .getLines()
        .map(_.trim)
        .filter(_.length > 0)
        .toSet
  }

  private val RESOURCES_TO_CLEANUP = Seq(
    "noun/nouns.txt", "noun/entities.txt", "noun/spam.txt",
    "noun/names.txt", "noun/twitter.txt", "noun/lol.txt",
    "noun/slangs.txt", "noun/company_names.txt",
    "noun/foreign.txt", "noun/geolocations.txt", "noun/profane.txt",
    "noun/kpop.txt", "noun/bible.txt",
    "noun/wikipedia_title_nouns.txt", "noun/pokemon.txt", "noun/congress.txt",

    "substantives/noun_prefix.txt", "substantives/suffix.txt",
    "substantives/family_names.txt", "substantives/given_names.txt",

    "adjective/adjective.txt", "adverb/adverb.txt",

    "auxiliary/determiner.txt", "auxiliary/exclamation.txt", "auxiliary/conjunctions.txt",

    "josa/josa.txt", "typos/typos.txt",

    "verb/eomi.txt", "verb/pre_eomi.txt", "verb/verb.txt", "verb/verb_prefix.txt"
  )

  def run {
    RESOURCES_TO_CLEANUP.foreach {
      f: String =>
        val outputFolder = "src/main/resources/com/twitter/penguin/korean/util/"
        System.err.println("Processing %s.".format(f))
        val words = readWords(outputFolder + f).toList.sorted

        val out = new FileOutputStream(outputFolder + f)

        words.foreach {
          word: String => out.write((word + "\n").getBytes)
        }
        out.close()
    }
  }
}
