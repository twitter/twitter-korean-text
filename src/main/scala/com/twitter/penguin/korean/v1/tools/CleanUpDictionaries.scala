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

package com.twitter.penguin.korean.v1.tools

import java.io.FileOutputStream

import scala.io.Source

/**
 * Clean up resources by removing duplicates and sorting.
 *
 * usage: ./pants goal run src/scala/com/twitter/penguin/korean/tools:clean_up_dictionaries
 * --jvm-run-args="/Users/hohyonryu/workspace/penguin-binaries/src/com/twitter/penguin/korean"
 *
 */
object CleanUpDictionaries {

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
    "noun/wikipedia_title_nouns.txt", "noun/pokemon.txt",

    "substantives/noun_prefix.txt", "substantives/suffix.txt",
    "substantives/family_names.txt", "substantives/given_names.txt",

    "adjective/adjective.txt", "adverb/adverb.txt",

    "aux/determiner.txt", "aux/exclamation.txt", "aux/conjunctions.txt",

    "josa/josa.txt", "typos/typos.txt",

    "verb/eomi.txt", "verb/pre_eomi.txt", "verb/verb.txt", "verb/verb_prefix.txt"
  )

  def main(args: Array[String]) {

    if (args.size != 1) {
      throw new IllegalArgumentException("Please enter the dictionary directory.")
    }

    RESOURCES_TO_CLEANUP.foreach {
      f: String =>
        val outputFolder = args(0) + "/"
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
