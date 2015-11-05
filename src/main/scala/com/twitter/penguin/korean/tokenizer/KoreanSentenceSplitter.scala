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

case class Sentence(text: String, start: Int, end: Int) {
  override def toString: String = s"$text($start,$end)"
}

/**
 * Sentence Splitter
 */
object KoreanSentenceSplitter {
  private val re =
    """(?x)[^.!?…\s]   # First char is non-punct, non-ws
      [^.!?…]*         # Greedily consume up to punctuation.
      (?:              # Group for unrolling the loop.
        [.!?…]         # (special) inner punctuation ok if
        (?!['\"]?\s|$) # not followed by ws or EOS.
        [^.!?…]*       # Greedily consume up to punctuation.
      )*               # Zero or more (special normal*)
      [.!?…]?          # Optional ending punctuation.
      ['\"]?           # Optional closing quote.
      (?=\s|$)""".r

  def split(s: CharSequence) = {
    re.findAllMatchIn(s)
      .map(m => Sentence(m.group(0), m.start(0), m.end(0)))
      .toSeq
  }
}
