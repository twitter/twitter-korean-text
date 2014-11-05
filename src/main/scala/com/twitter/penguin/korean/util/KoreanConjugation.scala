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
import org.apache.lucene.analysis.util.CharArraySet

import scala.collection.JavaConversions._

/**
 * Expands Korean verbs and adjectives to all possible conjugation forms.
 */
object KoreanConjugation {
  // ㅋ, ㅎ for 잨ㅋㅋㅋㅋ 잔댛ㅎㅎㅎㅎ
  private[this] val CODAS_COMMON = Seq('ㅂ', 'ㅆ', 'ㄹ', 'ㄴ', 'ㅁ')
  private[this] val CODAS_NO_PAST = Seq('ㅂ', 'ㄹ', 'ㄴ', 'ㅁ')

  private[this] val CODAS_SLANG_CONSONANT = Seq('ㅋ', 'ㅎ')
  private[this] val CODAS_SLANG_VOWEL = Seq('ㅜ', 'ㅠ')

  private[this] val PRE_EOMI_COMMON = "노느러도게면네더던든고거구기긴길다자잖재죠져지진질겠".toSeq
  private[this] val PRE_EOMI_1_1 = "야서써도준".toSeq
  private[this] val PRE_EOMI_1_2 = "어었".toSeq
  private[this] val PRE_EOMI_1_3 = "아았".toSeq
  private[this] val PRE_EOMI_1_4 = "워웠".toSeq
  private[this] val PRE_EOMI_1_5 = "여였".toSeq

  private[this] val PRE_EOMI_2 = "니냐".toSeq
  private[this] val PRE_EOMI_3 = "려며".toSeq
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
   * Conjugate adjectives and verbs.
   *
   * @param words Set of adjectives or verbs.
   * @param isAdjective True if the set contains adjectives.
   * @return CharArraySet with conjugated words.
   */
  protected[korean] def conjugatePredicates(words: Set[String], isAdjective: Boolean = false): CharArraySet = {
    val expanded = words.flatMap { word: String =>
      val init = word.init
      val lastChar = word.last
      val lastCharString = lastChar.toString
      val lastCharDecomposed = decomposeHangul(lastChar)

      val expandedLast: Seq[String] = lastCharDecomposed match {
        // 하다, special case
        case ('ㅎ', 'ㅏ', ' ') =>
          val endings = if (isAdjective) Seq("합", "해", "히", "") else Seq("합", "해")
          addPreEomi(lastChar, PRE_EOMI_COMMON ++ PRE_EOMI_2 ++ PRE_EOMI_6 ++ PRE_EOMI_RESPECT) ++
              CODAS_COMMON.map {
                case c: Char if c == 'ㅆ' => composeHangul('ㅎ', 'ㅐ', c).toString
                case c: Char => composeHangul('ㅎ', 'ㅏ', c).toString
              } ++ addPreEomi('하', PRE_EOMI_VOWEL ++ PRE_EOMI_1_5 ++ PRE_EOMI_6) ++
              addPreEomi('해', PRE_EOMI_1_1) ++ endings

        // 쏘다
        case (o: Char, 'ㅗ', ' ') =>
          addPreEomi(lastChar, PRE_EOMI_VOWEL ++ PRE_EOMI_2 ++ PRE_EOMI_1_3 ++ PRE_EOMI_6) ++
              CODAS_NO_PAST.map(composeHangul(o, 'ㅗ', _).toString) ++
              Seq(composeHangul(o, 'ㅘ', ' ').toString,
                composeHangul(o, 'ㅘ', 'ㅆ').toString,
                lastCharString)

        // 맞추다, 겨누다, 재우다,
        case (o: Char, 'ㅜ', ' ') =>
          addPreEomi(lastChar, PRE_EOMI_VOWEL ++ PRE_EOMI_1_2 ++ PRE_EOMI_6) ++
              CODAS_NO_PAST.map(composeHangul(o, 'ㅜ', _).toString) ++
              Seq(composeHangul(o, 'ㅝ').toString,
                composeHangul(o, 'ㅝ', 'ㅆ').toString,
                lastCharString)


        // 치르다, 구르다, 굴르다, 뜨다, 모으다, 고르다, 골르다
        case (o: Char, 'ㅡ', ' ') =>
          CODAS_NO_PAST.map(composeHangul(o, 'ㅡ', _).toString) ++
              Seq(composeHangul(o, 'ㅝ').toString,
                composeHangul(o, 'ㅓ').toString,
                composeHangul(o, 'ㅏ').toString,
                composeHangul(o, 'ㅝ', 'ㅆ').toString,
                composeHangul(o, 'ㅓ', 'ㅆ').toString,
                composeHangul(o, 'ㅏ', 'ㅆ').toString,
                lastCharString)

        // 사귀다
        case (o: Char, 'ㅟ', ' ') =>
          CODAS_NO_PAST.map(composeHangul(o, 'ㅟ', _).toString) ++
              Seq(composeHangul(o, 'ㅕ', ' ').toString, composeHangul(o, 'ㅕ', 'ㅆ').toString) ++
              Seq(lastCharString)

        // 마시다, 엎드리다, 치다
        case (o: Char, 'ㅣ', ' ') =>
          CODAS_NO_PAST.map(composeHangul(o, 'ㅣ', _).toString) ++
              Seq(composeHangul(o, 'ㅕ', ' ').toString,
                composeHangul(o, 'ㅕ', 'ㅆ').toString,
                lastCharString)

        // 꿰다, 꾀다
        case (o: Char, v: Char, ' ') if v == 'ㅞ' || v == 'ㅚ' || v == 'ㅙ' =>
          CODAS_COMMON.map(composeHangul(o, v, _).toString) ++
              Seq(lastCharString)


        // All other vowel endings: 둘러서다, 켜다, 세다, 캐다
        case (o: Char, v: Char, ' ') =>
          CODAS_COMMON.map(composeHangul(o, v, _).toString) ++
              addPreEomi(lastChar, PRE_EOMI_VOWEL ++ PRE_EOMI_1_1 ++ PRE_EOMI_6) ++
              Seq(lastCharString)

        // 만들다, 알다, 풀다
        case (o: Char, v: Char, 'ㄹ') if v == 'ㅡ' || v == 'ㅏ' || v == 'ㅜ' =>
          addPreEomi(composeHangul(o, v, ' '), PRE_EOMI_2 ++ PRE_EOMI_3 ++ PRE_EOMI_RESPECT) ++
              Seq(composeHangul(o, v, 'ㄻ').toString,
                composeHangul(o, v, 'ㄴ').toString,
                composeHangul(o, v, ' ').toString,
                lastCharString)

        // 낫다
        case (o: Char, 'ㅏ', 'ㅅ') =>
          addPreEomi(composeHangul(o, 'ㅏ'), PRE_EOMI_3 ++ PRE_EOMI_4 ++ PRE_EOMI_5) ++
              Seq(lastCharString)

        // 묻다
        case ('ㅁ', 'ㅜ', 'ㄷ') =>
          Seq(composeHangul('ㅁ', 'ㅜ', 'ㄹ').toString,
            lastCharString)

        // 붇다
        case (o: Char, 'ㅜ', 'ㄷ') =>
          addPreEomi(composeHangul(o, 'ㅜ', ' '), PRE_EOMI_1_2 ++ PRE_EOMI_1_4 ++ PRE_EOMI_4 ++ PRE_EOMI_5) ++
              Seq(composeHangul(o, 'ㅜ', 'ㄹ').toString,
                lastCharString)

        // 눕다
        case (o: Char, 'ㅜ', 'ㅂ') =>
          addPreEomi(composeHangul(o, 'ㅜ', ' '), PRE_EOMI_1_4 ++ PRE_EOMI_4 ++ PRE_EOMI_5) ++
              Seq(lastCharString)

        // 간지럽다, 갑작스럽다 -> 갑작스런
        case (o: Char, 'ㅓ', 'ㅂ') if isAdjective =>
          addPreEomi(composeHangul(o, 'ㅓ', ' '), PRE_EOMI_1_4 ++ PRE_EOMI_7) ++
              Seq(composeHangul(o, 'ㅓ', ' ').toString, composeHangul(o, 'ㅓ', 'ㄴ').toString, lastCharString)

        // 아름답다, 가볍다, 덥다, 간지럽다
        case (o: Char, v: Char, 'ㅂ') if isAdjective =>
          addPreEomi(composeHangul(o, v, ' '), PRE_EOMI_1_4 ++ PRE_EOMI_7) ++
              Seq(composeHangul(o, v, ' ').toString, lastCharString)

        // 놓다
        case (o: Char, 'ㅗ', 'ㅎ') =>
          CODAS_COMMON.map(composeHangul(o, 'ㅗ', _).toString) ++
              Seq(composeHangul(o, 'ㅘ', ' ').toString, composeHangul(o, 'ㅗ', ' ').toString, lastCharString)

        // 파랗다, 퍼렇다
        case (o: Char, v: Char, 'ㅎ') if isAdjective =>
          CODAS_COMMON.map(composeHangul(o, v, _).toString) ++
              Seq(composeHangul(o, v, ' ').toString, lastCharString)

        // 1 char with coda, 작다
        case (o: Char, v: Char, c: Char) if word.length == 1 =>
          addPreEomi(lastChar, PRE_EOMI_COMMON ++ PRE_EOMI_1_2 ++ PRE_EOMI_1_3 ++ PRE_EOMI_4 ++ PRE_EOMI_5) ++
              Seq(lastCharString)

        // 부여잡다, 얻어맞다, 얻어먹다
        case _ =>
          Seq(lastCharString)

      }

      expandedLast.map(init + _)
    }

    val newSet = newCharArraySet
    newSet.addAll(expanded)
    newSet
  }


  /**
   * Adds intentional typos at verb endings.
   * 가래 -> 가랰ㅋㅋㅋㅋㅋ
   * 먹 -> 머구ㅜㅜㅜㅜㅜ
   * 닭 -> 달규ㅠㅠㅠ
   *
   * @param word Input string.
   * @return Expanded words.
   */
  def addTypoEndings(word: String, applyToOneChar: Boolean = true): Seq[String] = {
    decomposeHangul(word.last) match {
      case (o: Char, v: Char, ' ') if applyToOneChar || word.length > 1 =>
        Seq(word) ++
            CODAS_SLANG_CONSONANT.map(word.init + composeHangul(o, v, _))
      case (o: Char, v: Char, c: Char) if DOUBLE_CODAS.contains(c) && word.length > 1 =>
        val doubleCoda = DOUBLE_CODAS(c)
        Seq(word) ++
            CODAS_SLANG_VOWEL.map(word.init + composeHangul(o, v, doubleCoda.first) + composeHangul(doubleCoda.second, _))
      case (o: Char, v: Char, c: Char) if word.length > 1 =>
        Seq(word) ++
            CODAS_SLANG_VOWEL.map(word.init + composeHangul(o, v, ' ') + composeHangul(c, _))
      case _ => Seq(word)
    }
  }
}
