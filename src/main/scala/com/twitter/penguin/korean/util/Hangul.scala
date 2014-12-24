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

/**
 * Hangul analysis helper. One Hangul character can be decomposed to consonants and a vowel.
 * This object helps analyze Korean character by consonant and vowel level.
 */
object Hangul {

  case class HangulChar(onset: Char, vowel: Char, coda: Char)

  private val HANGUL_BASE = 0xAC00

  private val ONSET_BASE = 21 * 28
  private val VOWEL_BASE = 28


  private val ONSET_LIST = List(
    'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ',
    'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
  )

  private val VOWEL_LIST = List(
    'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ',
    'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ', 'ㅙ', 'ㅚ',
    'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ',
    'ㅡ', 'ㅢ', 'ㅣ'
  )

  private val CODA_LIST = List(
    ' ', 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ',
    'ㄹ', 'ㄺ', 'ㄻ', 'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ', 'ㅀ',
    'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅊ',
    'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
  )

  private val ONSET_MAP = ONSET_LIST.zipWithIndex.toMap
  private val VOWEL_MAP = VOWEL_LIST.zipWithIndex.toMap
  protected[korean] val CODA_MAP = CODA_LIST.zipWithIndex.toMap

  case class DoubleCoda(first: Char, second: Char)

  protected[korean] val DOUBLE_CODAS = Map(
    'ㄳ' -> DoubleCoda('ㄱ', 'ㅅ'),
    'ㄵ' -> DoubleCoda('ㄴ', 'ㅈ'),
    'ㄶ' -> DoubleCoda('ㄴ', 'ㅎ'),
    'ㄺ' -> DoubleCoda('ㄹ', 'ㄱ'),
    'ㄻ' -> DoubleCoda('ㄹ', 'ㅁ'),
    'ㄼ' -> DoubleCoda('ㄹ', 'ㅂ'),
    'ㄽ' -> DoubleCoda('ㄹ', 'ㅅ'),
    'ㄾ' -> DoubleCoda('ㄹ', 'ㅌ'),
    'ㄿ' -> DoubleCoda('ㄹ', 'ㅍ'),
    'ㅀ' -> DoubleCoda('ㄹ', 'ㅎ'),
    'ㅄ' -> DoubleCoda('ㅂ', 'ㅅ')
  )

  /**
   * Decompose a Korean character to onset(초성), vowel(중성), and coda(종성).
   *
   * @param c A Korean character
   * @return (onset: Char, vowel: Char, coda: Char)
   */
  def decomposeHangul(c: Char): HangulChar = {
    require(!ONSET_MAP.contains(c) && !VOWEL_MAP.contains(c) && !CODA_MAP.contains(c),
      "Input character is not a valid Korean character")

    val u = c - HANGUL_BASE
    HangulChar(ONSET_LIST(u / ONSET_BASE),
      VOWEL_LIST((u % ONSET_BASE) / VOWEL_BASE),
      CODA_LIST(u % VOWEL_BASE))
  }

  /**
   * Check if a Korean character has a coda.
   *
   * @param c A Korean character
   * @return true if the character has a coda.
   */
  def hasCoda(c: Char): Boolean = (c - HANGUL_BASE) % VOWEL_BASE > 0

  /**
   * Compose a Korean character from the provided onset(초성), vowel(중성), and coda(종성).
   *
   * @param onset 초성
   * @param vowel 중성
   * @param coda 종성
   * @return A Korean character
   */
  def composeHangul(onset: Char, vowel: Char, coda: Char = ' '): Char = {
    require(onset != ' ' && vowel != ' ', "Input characters are not valid")

    (HANGUL_BASE +
        (ONSET_MAP(onset) * ONSET_BASE) +
        (VOWEL_MAP(vowel) * VOWEL_BASE) +
        CODA_MAP(coda)).toChar
  }

  /**
   * Compose a Korean character from the provided onset(초성), vowel(중성), and coda(종성).
   *
   * @param hc HangulChar(onset, vowel, coda)
   * @return A Korean character
   */
  def composeHangul(hc: HangulChar): Char =
    composeHangul(hc.onset, hc.vowel, hc.coda)
}
