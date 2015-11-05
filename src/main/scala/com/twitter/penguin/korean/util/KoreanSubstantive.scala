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

import com.twitter.penguin.korean.tokenizer.KoreanTokenizer.KoreanToken
import com.twitter.penguin.korean.util.Hangul._
import com.twitter.penguin.korean.util.KoreanDictionaryProvider._
import com.twitter.penguin.korean.util.KoreanPos._

/**
 * Helper methods for Korean nouns and josas.
 */
object KoreanSubstantive {

  private val JOSA_HEAD_FOR_CODA: Set[Char] = Set('은', '이', '을', '과', '아')

  private val JOSA_HEAD_FOR_NO_CODA: Set[Char] = Set('는', '가', '를', '와', '야', '여', '라')


  protected[korean] def isJosaAttachable(prevChar: Char, headChar: Char): Boolean = {
    (hasCoda(prevChar) && !JOSA_HEAD_FOR_NO_CODA.contains(headChar)) ||
        (!hasCoda(prevChar) && !JOSA_HEAD_FOR_CODA.contains(headChar))
  }

  protected[korean] def isName(chunk: CharSequence): Boolean = {
    if (nameDictionary('full_name).contains(chunk) ||
      nameDictionary('given_name).contains(chunk)) return true

    chunk.length match {
      case 3 => {
        (nameDictionary('family_name).contains(chunk.charAt(0).toString) &&
        nameDictionary('given_name).contains(chunk.subSequence(1, 3).toString))
      }
      case 4 => {
        (nameDictionary('family_name).contains(chunk.subSequence(0, 2).toString) &&
          nameDictionary('given_name).contains(chunk.subSequence(2, 4).toString))
      }
      case _ => false
    }
  }

  private val NUMBER_CHARS = "일이삼사오육칠팔구천백십해경조억만".map(_.toInt).toSet
  private val NUMBER_LAST_CHARS = "일이삼사오육칠팔구천백십해경조억만원배분초".map(_.toInt).toSet

  protected[korean] def isKoreanNumber(chunk: CharSequence): Boolean =
    (0 to chunk.length() - 1).foldLeft(true) {
      case (output, i) if i < chunk.length() - 1 => output && NUMBER_CHARS.contains(chunk.charAt(i).toInt)
      case (output, i) => output && NUMBER_LAST_CHARS.contains(chunk.charAt(i).toInt)
    }

  /**
   * Check if this chunk is an 'ㅇ' omitted variation of a noun (우혀니 -> 우현, 우현이, 빠순이 -> 빠순, 빠순이)
   *
   * @param chunk input chunk
   * @return true if the chunk is an 'ㅇ' omitted variation
   */
  protected[korean] def isKoreanNameVariation(chunk: CharSequence): Boolean = {
    val nounDict = koreanDictionary(Noun)

    val s = chunk.toString
    if (isName(s)) return true
    if (s.length < 3 || s.length > 5) return false

    val decomposed = s.map { c: Char => decomposeHangul(c)}
    val lastChar = decomposed.last
    if (!Hangul.CODA_MAP.contains(lastChar.onset)) return false
    if (lastChar.onset == 'ㅇ' || lastChar.vowel != 'ㅣ' || lastChar.coda != ' ') return false
    if (decomposed.init.last.coda != ' ') return false

    // Recover missing 'ㅇ' (우혀니 -> 우현, 우현이, 빠순이 -> 빠순, 빠순이)
    val recovered = decomposed.zipWithIndex.map {
      case (hc: HangulChar, i: Int) if i == s.length - 1 => '이'
      case (hc: HangulChar, i: Int) if i == s.length - 2 =>
        composeHangul(HangulChar(hc.onset, hc.vowel, decomposed.last.onset))
      case (hc: HangulChar, i: Int) => composeHangul(hc)
    }.mkString("")

    Seq(recovered, recovered.init).exists(isName)
  }

  /**
   * Collapse all the one-char nouns into one unknown noun
   *
   * @param posNodes sequence of KoreanTokens
   * @return sequence of collapsed KoreanTokens
   */
  protected[korean] def collapseNouns(posNodes: Seq[KoreanToken]): Seq[KoreanToken] = {
    val (nodes, collapsing) = posNodes.foldLeft((List[KoreanToken](), false)) {
      case ((pl: List[KoreanToken], collapsing: Boolean), p: KoreanToken)
        if p.pos == Noun && p.text.length == 1 && collapsing =>
        val text = pl.head.text + p.text
        val offset = pl.head.offset
        (KoreanToken(text, Noun, offset, text.length, unknown = true) :: pl.tail, true)
      case ((pl: List[KoreanToken], collapsing: Boolean), p: KoreanToken)
        if p.pos == Noun && p.text.length == 1 && !collapsing =>
        (p :: pl, true)
      case ((pl: List[KoreanToken], collapsing: Boolean), p: KoreanToken) =>
        (p :: pl, false)
    }
    nodes.reverse.toSeq
  }
}
