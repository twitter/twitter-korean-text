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

package com.twitter.penguin.korean.tokenizer

import java.util.regex.Matcher

import com.twitter.Regex
import com.twitter.penguin.korean.tokenizer.KoreanTokenizer.KoreanToken
import com.twitter.penguin.korean.util.KoreanPos
import com.twitter.penguin.korean.util.KoreanPos._

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

case class KoreanChunk(text: String, offset: Int, length: Int)

/**
 * Split input text into Korean Chunks (어절)
 */
object KoreanChunker {
  private val POS_PATTERNS = Map(
    Korean -> """([가-힣]+)""".r.pattern,
    Alpha -> """(\p{Alpha}+)""".r.pattern,
    Number -> ("""(\$?\p{Digit}+"""
      + """(,\p{Digit}{3})*([/~:\.-]\p{Digit}+)?"""
      + """(천|만|억|조)*(%|원|달러|위안|옌|엔|유로|등|년|월|일|회|시간|시|분|초)?)""").r.pattern,
    KoreanParticle -> """([ㄱ-ㅣ]+)""".r.pattern,
    Punctuation -> """([\p{Punct}·…’]+)""".r.pattern,
    URL -> Regex.VALID_URL,
    Email -> """([\p{Alnum}\.\-_]+@[\p{Alnum}\.]+)""".r.pattern,
    Hashtag -> Regex.VALID_HASHTAG,
    ScreenName -> Regex.VALID_MENTION_OR_LIST,
    CashTag -> Regex.VALID_CASHTAG,
    Space -> """\s+""".r.pattern
  )

  private val CHUNKING_ORDER = Seq(URL, Email, ScreenName, Hashtag, CashTag, Number, Korean, KoreanParticle, Alpha, Punctuation)

  protected[korean] def getChunks(input: String, keepSpace: Boolean = false): Seq[String] = {
    chunk(input).map(_.text)
  }

  private[this] case class ChunkMatch(start: Int, end: Int, text: String, pos: KoreanPos) {
    def disjoint(that: ChunkMatch): Boolean = {
      (that.start < this.start && that.end <= this.start) ||
        (that.start >= this.end && that.end > this.end)
    }
  }

  def splitBySpaceKeepingSpace(s: CharSequence): Seq[String] = {
    val space = """\s+""".r.pattern
    val m = space.matcher(s)

    var tokens = new ListBuffer[String]()
    var index = 0
    while(m.find()) {
      if (index < m.start) {
        tokens += s.subSequence(index, m.start).toString
      }
      tokens += s.subSequence(m.start, m.end).toString
      index = m.end
    }

    if (index < s.length()) {
      tokens += s.subSequence(index, s.length()).toString
    }

    return tokens.toList
  }

  /**
   * Recursively call m.find() to find all the matches.
   * Use tail-recursion optimization to avoid stack overflow.
   *
   * @param m input Matcher
   * @param pos KoreanPos to attach
   * @param matches ouput list of ChunkMatch
   * @return list of ChunkMatches
   */
  @scala.annotation.tailrec
  private[this] def findAllPatterns(m: Matcher, pos: KoreanPos, matches: List[ChunkMatch] = List()): List[ChunkMatch] = {
    if (m.find()) {
      findAllPatterns(m, pos, ChunkMatch(m.start, m.end, m.group(), pos) :: matches)
    } else {
      matches
    }
  }

  private[this] def splitChunks(text: String): List[ChunkMatch] = {
    if (text.charAt(0).isSpaceChar) {
      List(ChunkMatch(0, text.length, text, Space))
    } else {
      val chunksBuf = new ListBuffer[ChunkMatch]()
      var matchedLen = 0
      CHUNKING_ORDER.foreach { pos =>
        if (matchedLen < text.length) {
          val m = POS_PATTERNS(pos).matcher(text)
          while (m.find()) {
            val cm = ChunkMatch(m.start, m.end, m.group(), pos)
            if (chunksBuf.forall(cm.disjoint)) {
              chunksBuf += cm
              matchedLen += cm.end - cm.start
            }
          }
        }
      }

      val chunks = chunksBuf.sortBy(cm => cm.start).toList
      fillInUnmatched(text, chunks, Foreign)
    }
  }

  /**
   * Fill in unmatched segments with given pos
   *
   * @param text input text
   * @param chunks matched chunks
   * @param pos KoreanPos to attach to the unmatched chunk
   * @return list of ChunkMatches
   */
  private[this] def fillInUnmatched(text: String,
                                    chunks: Seq[ChunkMatch],
                                    pos: KoreanPos.Value): List[ChunkMatch] = {

    // Add Foreign for unmatched parts
    val (chunksWithForeign, prevEnd) = chunks.foldLeft((List[ChunkMatch](), 0)) {
      case ((l: List[ChunkMatch], prevEnd: Int), cm: ChunkMatch) if cm.start == prevEnd =>
        (cm :: l, cm.end)
      case ((l: List[ChunkMatch], prevEnd: Int), cm: ChunkMatch) if cm.start > prevEnd =>
        (cm :: ChunkMatch(prevEnd, cm.start, text.slice(prevEnd, cm.start), pos) :: l, cm.end)
      case ((l: List[ChunkMatch], prevEnd: Int), cm: ChunkMatch) =>
        throw new IllegalStateException("Non-disjoint chunk matches found.")
    }

    val output = if (prevEnd < text.length) {
      ChunkMatch(prevEnd, text.length, text.slice(prevEnd, text.length), pos) :: chunksWithForeign
    } else {
      chunksWithForeign
    }

    output.reverse
  }


  /**
   * Get chunks by given pos.
   *
   * @param input input string
   * @param pos one of supported KoreanPos's: URL, Email, ScreenName, Hashtag,
   *            CashTag, Korean, KoreanParticle, Number, Alpha, Punctuation
   * @return sequence of Korean chunk strings
   */
  def getChunksByPos(input: String, pos: KoreanPos): Seq[KoreanToken] = {
    chunk(input).filter(_.pos == pos)
  }

  /**
   * Split input text into a sequnce of KoreanToken. A candidate for Korean parser
   * gets tagged with KoreanPos.Korean.
   *
   * @param input input string
   * @return sequence of KoreanTokens
   */
  def chunk(input: CharSequence): Seq[KoreanToken] = {
    val s = input.toString
    val (l: List[KoreanToken], i: Int) = splitBySpaceKeepingSpace(s).flatMap {
      s => splitChunks(s)
    }.foldLeft(List[KoreanToken](), 0) {
      case ((l: List[KoreanToken], i: Int), m: ChunkMatch) =>
        val segStart = s.indexOf(m.text, i)
        (KoreanToken(m.text, m.pos, segStart, m.text.length) :: l, segStart + m.text.length)
    }
    l.reverse
  }
}
