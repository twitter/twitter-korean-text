/*
 * Twitter Korean Text - Scala library to process Korean text
 *
 * Copyright 2015 Twitter, Inc.
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

package com.twitter.penguin.korean.tokenizer

import com.twitter.penguin.korean.tokenizer.KoreanTokenizer.KoreanToken
import com.twitter.penguin.korean.util.KoreanPos

/**
  * Detokenizes a list of tokenized words into a readable sentence.
  */
object KoreanDetokenizer {
  val SuffixPos = Set(KoreanPos.Josa, KoreanPos.Eomi, KoreanPos.PreEomi, KoreanPos.Suffix,
    KoreanPos.Punctuation)
  val PrefixPos = Set(KoreanPos.NounPrefix, KoreanPos.VerbPrefix)

  def detokenize(input: Iterable[String]) = {
    // Space guide prevents tokenizing a word that was not tokenized in the input.
    val spaceGuide: Set[Int] = getSpaceGuide(input)

    // Tokenize a merged text with the space guide.
    val tokenized = KoreanTokenizer.tokenize(input.mkString(""), TokenizerProfile(spaceGuide = spaceGuide))

    // Attach suffixes and prefixes.
    collapseTokens(tokenized).mkString(" ")
  }

  private def collapseTokens(tokenized: Seq[KoreanToken]): List[String] = {
    val (output, isPrefix) = tokenized.foldLeft((List[String](), false)) {
      case ((output: List[String], isPrefix: Boolean), token: KoreanToken) =>
        if (output.nonEmpty && (isPrefix || SuffixPos.contains(token.pos))) {
          val attached = output.lastOption.getOrElse("") + token.text
          (output.init :+ attached, false)
        } else if (PrefixPos.contains(token.pos)) {
          (output :+ token.text, true)
        } else {
          (output :+ token.text, false)
        }
    }
    output
  }

  private def getSpaceGuide(input: Iterable[String]): Set[Int] = {
    val (spaceGuide, index) = input.foldLeft((Set[Int](), 0)) {
      case ((output: Set[Int], i: Int), word: String) =>
        (output + (i + word.length), i + word.length)
    }
    spaceGuide
  }
}
