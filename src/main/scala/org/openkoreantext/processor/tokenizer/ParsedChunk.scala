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

package org.openkoreantext.processor.tokenizer

import org.openkoreantext.processor.tokenizer.KoreanTokenizer.KoreanToken
import org.openkoreantext.processor.tokenizer.ParsedChunk._
import org.openkoreantext.processor.util.KoreanDictionaryProvider._
import org.openkoreantext.processor.util.KoreanPos._

object ParsedChunk {
  val suffixes = Set(Suffix, Eomi, Josa, PreEomi)
  val preferredBeforeHaVerb = Set(Noun, ProperNoun, VerbPrefix)
}

/**
  * A candidate parse for a chunk.
  *
  * @param posNodes Sequence of KoreanTokens.
  * @param words Number of words in this candidate parse.
  */
case class ParsedChunk(posNodes: Seq[KoreanToken], words: Int,
    profile: TokenizerProfile = TokenizerProfile.defaultProfile) {

  // Using lazy val to cache the score
  lazy val score: Float = countTokens * profile.tokenCount +
      countUnknowns * profile.unknown +
      words * profile.wordCount +
      getUnknownCoverage * profile.unknownCoverage +
      getFreqScore * profile.freq +
      countPos(Unknown) * profile.unknownPosCount +
      isExactMatch * profile.exactMatch +
      isAllNouns * profile.allNoun +
      isPreferredPattern * profile.preferredPattern +
      countPos(Determiner) * profile.determinerPosCount +
      countPos(Exclamation) * profile.exclamationPosCount +
      isInitialPostPosition * profile.initialPostPosition +
      isNounHa * profile.haVerb +
      hasSpaceOutOfGuide * profile.spaceGuidePenalty

  def countUnknowns: Int = this.posNodes.count { p: KoreanToken => p.unknown }

  def countTokens: Int = this.posNodes.size

  def isInitialPostPosition: Int = if (suffixes.contains(this.posNodes.head.pos)) 1 else 0

  def isExactMatch: Int = if (this.posNodes.size == 1) 0 else 1

  def hasSpaceOutOfGuide: Int = if (profile.spaceGuide.isEmpty) {
    0
  } else {
    this.posNodes
        .filter{p: KoreanToken => !suffixes.contains(p.pos)}
        .count {
          p: KoreanToken => !profile.spaceGuide.contains(p.offset)
        }
  }


  def isAllNouns: Int = if (this.posNodes.exists(
    t => t.pos != Noun && t.pos != ProperNoun)) 1
  else 0

  def isPreferredPattern: Int = if (
    posNodes.size == 2 && profile.preferredPatterns.contains(posNodes.map(_.pos))
  ) 0
  else 1

  def isNounHa: Int = if (this.posNodes.size >= 2
      && preferredBeforeHaVerb.contains(this.posNodes.head.pos)
      && this.posNodes(1).pos == Verb
      && (this.posNodes(1).text.startsWith("하") || this.posNodes(1).text.startsWith("해")))
    0
  else
    1

  def posTieBreaker: Int = this.posNodes.map(_.pos.id).sum

  def getUnknownCoverage: Int = this.posNodes.foldLeft(0) {
    case (sum, p: KoreanToken) => if (p.unknown) sum + p.text.length else sum
  }

  def getFreqScore: Float = this.posNodes.foldLeft(0f) {
    case (output: Float, p: KoreanToken) if p.pos == Noun || p.pos == ProperNoun =>
      output + (1f - koreanEntityFreq.getOrDefault(p.text, 0f))
    case (output: Float, p: KoreanToken) => output + 1.0f
  } / this.posNodes.size

  def ++(that: ParsedChunk): ParsedChunk = {
    ParsedChunk(this.posNodes ++ that.posNodes, this.words + that.words, profile)
  }

  def countPos(pos: KoreanPos): Int = this.posNodes.count { p: KoreanToken => p.pos == pos }
}