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

package org.openkoreantext.processor.tools

import org.openkoreantext.processor.OpenKoreanTextProcessor._
import org.openkoreantext.processor.tokenizer.KoreanTokenizer.KoreanToken
import org.openkoreantext.processor.util.KoreanDictionaryProvider._

/**
  * Create Korean Parsing examples.
  */
object CreateChunkParsingCandidates extends Runnable {

  def run {
    val parsedPairs = readFileByLineFromResources("example_chunks.txt").flatMap {
      case line if line.length > 0 =>
        val chunk = line.trim
        val parsed = tokenizeTopN(chunk, 5).head
        val parsedString: Seq[String] = parsed.map { p =>
          val tokens = p.map{
            token => KoreanToken(token.text, token.pos, token.offset, token.length)}
          tokens.mkString("/")
        }.distinct
        Some(Seq(chunk) ++ parsedString)
      case line => None
    }

    parsedPairs.foreach {
      p => println(p.mkString("\t"))
    }
  }
}
