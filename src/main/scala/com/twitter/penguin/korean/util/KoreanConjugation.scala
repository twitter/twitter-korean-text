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

import com.twitter.penguin.korean.util.Hangul._
import com.twitter.penguin.korean.util.KoreanDictionaryProvider._

import scala.collection.JavaConversions._

/**
 * Expands Korean verbs and adjectives to all possible conjugation forms.
 */
object KoreanConjugation {
  // ㅋ, ㅎ for 잨ㅋㅋㅋㅋ 잔댛ㅎㅎㅎㅎ
  private[this] val CODAS_COMMON = Seq('ㅂ', 'ㅆ', 'ㄹ', 'ㄴ', 'ㅁ')
  // 파랗다 -> 파래, 파램, 파랠, 파랬
  private[this] val CODAS_FOR_CONTRACTION = Seq('ㅆ', 'ㄹ', 'ㅁ')
  private[this] val CODAS_NO_PAST = Seq('ㅂ', 'ㄹ', 'ㄴ', 'ㅁ')

  private[this] val CODAS_SLANG_CONSONANT = Seq('ㅋ', 'ㅎ')
  private[this] val CODAS_SLANG_VOWEL = Seq('ㅜ', 'ㅠ')

  private[this] val PRE_EOMI_COMMON = "거게겠고구기긴길네다더던도든면자잖재져죠지진질".toSeq
  private[this] val PRE_EOMI_1_1 = "야서써도준".toSeq
  private[this] val PRE_EOMI_1_2 = "어었".toSeq
  private[this] val PRE_EOMI_1_3 = "아았".toSeq
  private[this] val PRE_EOMI_1_4 = "워웠".toSeq
  private[this] val PRE_EOMI_1_5 = "여였".toSeq

  private[this] val PRE_EOMI_2 = "노느니냐".toSeq
  private[this] val PRE_EOMI_3 = "러려며".toSeq
  private[this] val PRE_EOMI_4 = "으".toSeq
  private[this] val PRE_EOMI_5 = "은".toSeq
  private[this] val PRE_EOMI_6 = "는".toSeq
  private[this] val PRE_EOMI_7 = "운".toSeq

  private[this] val PRE_EOMI_RESPECT = "세시실신셔습셨십".toSeq

  // 모음 어말어미
  private[this] val PRE_EOMI_VOWEL = PRE_EOMI_COMMON ++ PRE_EOMI_2 ++ PRE_EOMI_3 ++ PRE_EOMI_RESPECT

  private[this] def addPreEomi(lastChar: Char, charsToAdd: Seq[Char]): Seq[String] = {
    charsToAdd.map(lastChar + _.toString)
  }

  /**
   * Wrap conjugatePredicated
   *
   * @param words Set of adjectives or verbs.
   * @param isAdjective True if the set contains adjectives.
   * @return CharArraySet with conjugated words.
   */
  protected[korean] def conjugatePredicatesToCharArraySet(words: Set[String], isAdjective: Boolean = false): CharArraySet = {
    val expanded: Set[String] = conjugatePredicated(words, isAdjective)

    val newSet = newCharArraySet
    newSet.addAll(expanded)
    newSet
  }

  /**
   * Conjugate adjectives and verbs.
   *
   * @param words Set of adjectives or verbs.
   * @param isAdjective True if the set contains adjectives.
   * @return Set of conjugated words
   */
  protected[korean] def conjugatePredicated(words: Set[String], isAdjective: Boolean): Set[String] = {
    lazy val expanded = words.flatMap { word: String =>
      val init = word.init
      val lastChar = word.last
      val lastCharString = lastChar.toString
      val lastCharDecomposed = decomposeHangul(lastChar)

      val expandedLast: Seq[String] = lastCharDecomposed match {
        // Cases without codas
        // 하다, special case
        case HangulChar('ㅎ', 'ㅏ', ' ') =>
          val endings = if (isAdjective) Seq("합", "해", "히", "하") else Seq("합", "해")
          addPreEomi(lastChar, PRE_EOMI_COMMON ++ PRE_EOMI_2 ++ PRE_EOMI_6 ++ PRE_EOMI_RESPECT) ++
              CODAS_COMMON.map {
                case c: Char if c == 'ㅆ' => composeHangul('ㅎ', 'ㅐ', c).toString
                case c: Char => composeHangul('ㅎ', 'ㅏ', c).toString
              } ++ addPreEomi('하', PRE_EOMI_VOWEL ++ PRE_EOMI_1_5 ++ PRE_EOMI_6) ++
              addPreEomi('해', PRE_EOMI_1_1) ++ endings

        // 쏘다
        case HangulChar(o: Char, 'ㅗ', ' ') =>
          addPreEomi(lastChar, PRE_EOMI_VOWEL ++ PRE_EOMI_2 ++ PRE_EOMI_1_3 ++ PRE_EOMI_6) ++
              CODAS_NO_PAST.map(composeHangul(o, 'ㅗ', _).toString) ++
              Seq(composeHangul(o, 'ㅘ', ' ').toString,
                composeHangul(o, 'ㅘ', 'ㅆ').toString,
                lastCharString)

        // 맞추다, 겨누다, 재우다,
        case HangulChar(o: Char, 'ㅜ', ' ') =>
          addPreEomi(lastChar, PRE_EOMI_VOWEL ++ PRE_EOMI_1_2 ++ PRE_EOMI_2 ++ PRE_EOMI_6) ++
              CODAS_NO_PAST.map(composeHangul(o, 'ㅜ', _).toString) ++
              Seq(composeHangul(o, 'ㅝ').toString,
                composeHangul(o, 'ㅝ', 'ㅆ').toString,
                lastCharString)

        // 치르다, 구르다, 굴르다, 뜨다, 모으다, 고르다, 골르다
        case HangulChar(o: Char, 'ㅡ', ' ') =>
          addPreEomi(lastChar, PRE_EOMI_2 ++ PRE_EOMI_6) ++
              CODAS_NO_PAST.map(composeHangul(o, 'ㅡ', _).toString) ++
              Seq(composeHangul(o, 'ㅝ').toString,
                composeHangul(o, 'ㅓ').toString,
                composeHangul(o, 'ㅏ').toString,
                composeHangul(o, 'ㅝ', 'ㅆ').toString,
                composeHangul(o, 'ㅓ', 'ㅆ').toString,
                composeHangul(o, 'ㅏ', 'ㅆ').toString,
                lastCharString)

        // 사귀다
        case HangulChar('ㄱ', 'ㅟ', ' ') =>
          addPreEomi(lastChar, PRE_EOMI_2 ++ PRE_EOMI_6) ++
              CODAS_NO_PAST.map(composeHangul('ㄱ', 'ㅟ', _).toString) ++
              Seq(composeHangul('ㄱ', 'ㅕ', ' ').toString, composeHangul('ㄱ', 'ㅕ', 'ㅆ').toString) ++
              Seq(lastCharString)

        // 쥐다
        case HangulChar(o: Char, 'ㅟ', ' ') =>
          CODAS_NO_PAST.map(composeHangul(o, 'ㅟ', _).toString) ++
              addPreEomi(lastChar, PRE_EOMI_2 ++ PRE_EOMI_6) ++
              Seq(lastCharString)

        // 마시다, 엎드리다, 치다, 이다, 아니다
        case HangulChar(o: Char, 'ㅣ', ' ') =>
          CODAS_NO_PAST.map(composeHangul(o, 'ㅣ', _).toString) ++
              addPreEomi(lastChar, PRE_EOMI_1_2 ++ PRE_EOMI_2 ++ PRE_EOMI_6) ++
              Seq(composeHangul(o, 'ㅣ', 'ㅂ') + "니",
                composeHangul(o, 'ㅕ', ' ').toString,
                composeHangul(o, 'ㅕ', 'ㅆ').toString,
                lastCharString)

        // 꿰다, 꾀다
        case HangulChar(o: Char, v: Char, ' ') if v == 'ㅞ' || v == 'ㅚ' || v == 'ㅙ' =>
          addPreEomi(lastChar, PRE_EOMI_2 ++ PRE_EOMI_6) ++
              CODAS_COMMON.map(composeHangul(o, v, _).toString) ++
              Seq(lastCharString)

        // All other vowel endings: 둘러서다, 켜다, 세다, 캐다, 차다
        case HangulChar(o: Char, v: Char, ' ') =>
          CODAS_COMMON.map(composeHangul(o, v, _).toString) ++
              addPreEomi(lastChar, PRE_EOMI_VOWEL ++ PRE_EOMI_1_1 ++ PRE_EOMI_2 ++ PRE_EOMI_6) ++
              Seq(lastCharString)

        // Cases with codas
        // 만들다, 알다, 풀다
        case HangulChar(o: Char, v: Char, 'ㄹ') if (o == 'ㅁ' && v == 'ㅓ') || v == 'ㅡ' || v == 'ㅏ' || v == 'ㅜ' =>
          addPreEomi(lastChar, PRE_EOMI_1_2 ++ PRE_EOMI_3) ++
              addPreEomi(composeHangul(o, v, ' '),
                PRE_EOMI_2 ++ PRE_EOMI_6 ++ PRE_EOMI_RESPECT) ++
              Seq(composeHangul(o, v, 'ㄻ').toString,
                composeHangul(o, v, 'ㄴ').toString,
                lastCharString)

        // 낫다, 뺴앗다
        case HangulChar(o: Char, 'ㅏ', 'ㅅ') =>
          addPreEomi(lastChar, PRE_EOMI_2 ++ PRE_EOMI_6) ++
              addPreEomi(composeHangul(o, 'ㅏ'), PRE_EOMI_4 ++ PRE_EOMI_5) ++
              Seq(lastCharString)

        // 묻다
        case HangulChar('ㅁ', 'ㅜ', 'ㄷ') =>
          addPreEomi(lastChar, PRE_EOMI_2 ++ PRE_EOMI_6) ++
              Seq(composeHangul('ㅁ', 'ㅜ', 'ㄹ').toString,
                lastCharString)

        // 붇다
        case HangulChar(o: Char, 'ㅜ', 'ㄷ') =>
          addPreEomi(lastChar, PRE_EOMI_2 ++ PRE_EOMI_6) ++
              addPreEomi(composeHangul(o, 'ㅜ', ' '),
                PRE_EOMI_1_2 ++ PRE_EOMI_1_4 ++ PRE_EOMI_4 ++ PRE_EOMI_5) ++
              Seq(composeHangul(o, 'ㅜ', 'ㄹ').toString,
                lastCharString)

        // 눕다
        case HangulChar(o: Char, 'ㅜ', 'ㅂ') =>
          addPreEomi(lastChar, PRE_EOMI_2 ++ PRE_EOMI_6) ++
              addPreEomi(composeHangul(o, 'ㅜ', ' '), PRE_EOMI_1_4 ++ PRE_EOMI_4 ++ PRE_EOMI_5) ++
              Seq(lastCharString)

        // 간지럽다, 갑작스럽다 -> 갑작스런
        case HangulChar(o: Char, 'ㅓ', 'ㅂ') if isAdjective =>
          addPreEomi(composeHangul(o, 'ㅓ', ' '), PRE_EOMI_1_4 ++ PRE_EOMI_7) ++
              Seq(composeHangul(o, 'ㅓ', ' ').toString, composeHangul(o, 'ㅓ', 'ㄴ').toString, lastCharString)

        // 아름답다, 가볍다, 덥다, 간지럽다
        case HangulChar(o: Char, v: Char, 'ㅂ') if isAdjective =>
          addPreEomi(composeHangul(o, v, ' '), PRE_EOMI_1_4 ++ PRE_EOMI_7) ++
              Seq(composeHangul(o, v, ' ').toString, lastCharString)

        // 놓다
        case HangulChar(o: Char, 'ㅗ', 'ㅎ') =>
          addPreEomi(lastChar, PRE_EOMI_2 ++ PRE_EOMI_6) ++
              CODAS_COMMON.map(composeHangul(o, 'ㅗ', _).toString) ++
              Seq(composeHangul(o, 'ㅘ', ' ').toString, composeHangul(o, 'ㅗ', ' ').toString, lastCharString)

        // 파랗다, 퍼렇다, 어떻다
        case HangulChar(o: Char, v: Char, 'ㅎ') if isAdjective =>
          CODAS_COMMON.map(composeHangul(o, v, _).toString) ++
              CODAS_FOR_CONTRACTION.map(composeHangul(o, 'ㅐ', _).toString) ++
              Seq(composeHangul(o, 'ㅐ', ' ').toString,
                composeHangul(o, v, ' ').toString,
                lastCharString)

        // 1 char with coda adjective, 있다, 컸다
        case HangulChar(o: Char, v: Char, c: Char) if word.length == 1 || (isAdjective && c == 'ㅆ') =>
          addPreEomi(lastChar,
            PRE_EOMI_COMMON ++ PRE_EOMI_1_2 ++ PRE_EOMI_1_3 ++ PRE_EOMI_2 ++ PRE_EOMI_4 ++ PRE_EOMI_5 ++ PRE_EOMI_6) ++
              Seq(lastCharString)

        // 1 char with coda adjective, 밝다
        case HangulChar(o: Char, v: Char, c: Char) if word.length == 1 && isAdjective =>
          addPreEomi(lastChar,
            PRE_EOMI_COMMON ++ PRE_EOMI_1_2 ++ PRE_EOMI_1_3 ++ PRE_EOMI_2 ++ PRE_EOMI_4 ++ PRE_EOMI_5) ++
              Seq(lastCharString)

        // 부여잡다, 얻어맞다, 얻어먹다
        case _ =>
          Seq(lastCharString)

      }

      expandedLast.map(init + _)
    }

    if (isAdjective) {
      expanded
    } else {
      // Edge cases: these more likely to be a conjugation of an adjective than a verb
      expanded -- Set("아니", "입", "입니", "나는")
    }
  }
}
