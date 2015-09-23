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

package com.twitter.penguin.korean.normalizer

import java.util.regex.Matcher

import com.twitter.penguin.korean.util.Hangul
import com.twitter.penguin.korean.util.Hangul._
import com.twitter.penguin.korean.util.KoreanDictionaryProvider._
import com.twitter.penguin.korean.util.KoreanPos._

import scala.util.matching.Regex.Match

/**
 * Normalize Korean colloquial text
 */
object KoreanNormalizer {
  private[this] val EXTENTED_KOREAN_REGEX = """([ㄱ-ㅣ가-힣]+)""".r
  private[this] val KOREAN_TO_NORMALIZE_REGEX = """([가-힣]+)(ㅋ+|ㅎ+|[ㅠㅜ]+)""".r
  protected[korean] val REPEATING_CHAR_REGEX = """(.)\1{2,}|[ㅠㅜ]{2,}""".r
  private[this] val REPEATING_2CHAR_REGEX = """(..)\1{2,}""".r

  private[this] val WHITESPACE_REGEX = """\s+""".r

  private[this] case class Segment(text: String, matchData: Option[Match])

  private[this] val CODA_N_EXCPETION = "은는운인텐근른픈닌든던".toSet

  /**
   * Normalize Korean CharSequence text
   * ex) 하댘ㅋㅋㅋ -> 하대, 머구뮤ㅠㅠㅠ -> 머굼
   * 하즤 -> 하지
   *
   * @param input input CharSequence
   * @return normalized CharSequence
   */
  def normalize(input: CharSequence): CharSequence = {
    EXTENTED_KOREAN_REGEX.replaceAllIn(input, m => normalizeKoreanChunk(m.group(0)).toString)
  }

  private[this] def normalizeKoreanChunk(input: CharSequence): CharSequence = {
    // Normalize endings: 안됔ㅋㅋㅋ -> 안돼ㅋㅋ
    val endingNormalized = KOREAN_TO_NORMALIZE_REGEX.replaceAllIn(
      input, m => processNormalizationCandidate(m).toString
    )

    // Normalize repeating chars: ㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋ -> ㅋㅋ
    val exclamationNormalized = REPEATING_CHAR_REGEX.replaceAllIn(
      endingNormalized, m => {
        Matcher.quoteReplacement(m.group(0).take(2).toString)
      }
    )
    // Normalize repeating chars: 훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍 -> 훌쩍훌쩍
    val repeatingNormalized = REPEATING_2CHAR_REGEX.replaceAllIn(
      exclamationNormalized, m => {
        Matcher.quoteReplacement(m.group(0).take(4).toString)
      }
    )

    // Coda normalization (명사 + ㄴ 첨가 정규화): 소린가 -> 소리인가
    val codaNNormalized = normalizeCodaN(repeatingNormalized)
    // Typo correction: 하겟다 -> 하겠다
    val typoCorrected = correctTypo(codaNNormalized)
    // Spaces, tabs, new lines are replaced with a single space.
    WHITESPACE_REGEX.replaceAllIn(typoCorrected, " ")
  }

  protected[korean] def correctTypo(chunk: CharSequence): CharSequence = {
    typoDictionaryByLength.foldLeft(chunk) {
      case (output: String, (wordLen: Int, typoMap: Map[String, String])) =>
        output.sliding(wordLen).foldLeft(output) {
          case (sliceOutput: String, slice: String) if typoMap.contains(slice) =>
            sliceOutput.replaceAll(slice, typoMap(slice))
          case (sliceOutput: String, slice: String) =>
            sliceOutput
        }
    }
  }

  protected[korean] def normalizeCodaN(chunk: CharSequence): CharSequence = {
    if (chunk.length < 2) return chunk

    val lastTwo = chunk.subSequence(chunk.length() - 2, chunk.length())
    val last = chunk.charAt(chunk.length() - 1)

    val lastTwoHead = lastTwo.charAt(0)

    // Exception cases
    if (koreanDictionary(Noun).contains(chunk) ||
        koreanDictionary(Conjunction).contains(chunk) ||
        koreanDictionary(Adverb).contains(chunk) ||
        koreanDictionary(Noun).contains(lastTwo) ||
        lastTwoHead < '가' || lastTwoHead > '힣' ||
        CODA_N_EXCPETION.contains(lastTwoHead)
    ) {
      return chunk
    }

    val hc = decomposeHangul(lastTwoHead)

    val newHead = new StringBuilder()
        .append(chunk.subSequence(0, chunk.length() - 2))
        .append(composeHangul(hc.onset, hc.vowel))

    if (hc.coda == 'ㄴ' &&
        (last == '데' || last == '가' || last == '지') &&
        koreanDictionary(Noun).contains(newHead)
    ) {
      val mid = if (hc.vowel == 'ㅡ') "은" else "인"
      newHead + mid + last
    } else {
      chunk
    }
  }

  private[this] def processNormalizationCandidate(m: Match): CharSequence = {
    val chunk = m.group(1)
    val toNormalize = m.group(2)

    val normalizedChunk = if (koreanDictionary(Noun).contains(chunk) ||
        koreanDictionary(Eomi).contains(chunk.takeRight(1)) ||
        koreanDictionary(Eomi).contains(chunk.takeRight(2))) {
      chunk
    } else {
      normalizeEmotionAttachedChunk(chunk, toNormalize)
    }
    normalizedChunk + toNormalize
  }

  private[this] def normalizeEmotionAttachedChunk(s: CharSequence, toNormalize: CharSequence): CharSequence = {
    val init = s.subSequence(0, s.length() - 1)
    val secondToLastDecomposed = init match {
      case si: CharSequence if si.length > 0 =>
        val hc = decomposeHangul(si.charAt(si.length() - 1))
        if (hc.coda == ' ') Some(hc) else None
      case _ => None
    }

    decomposeHangul(s.charAt(s.length() - 1)) match {
      case hc: HangulChar if hc.coda == 'ㅋ' || hc.coda == 'ㅎ' =>
        new StringBuilder()
            .append(init)
            .append(composeHangul(hc.onset, hc.vowel))
      case HangulChar(o: Char, v: Char, ' ') if secondToLastDecomposed.isDefined &&
          (v == toNormalize.charAt(0)) &&
          Hangul.CODA_MAP.contains(o) =>
        val hc = secondToLastDecomposed.get
        new StringBuilder()
            .append(init.subSequence(0, init.length() - 1))
            .append(composeHangul(hc.onset, hc.vowel, o))
      case _ => s
    }
  }
}
