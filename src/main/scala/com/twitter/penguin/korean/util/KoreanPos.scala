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
 * Korean Part-of-Speech
 *
 * N Noun: 명사 (Nouns, Pronouns, Company Names, Proper Noun, Person Names, Numerals, Standalone, Dependent)
 * V Verb: 동사 (하, 먹, 자, 차)
 * J Adjective: 형용사 (예쁘다, 크다, 작다)
 * A Adverb: 부사 (잘, 매우, 빨리, 반드시, 과연)
 * D Determiner: 관형사 (새, 헌, 참, 첫, 이, 그, 저)
 * E Exclamation: 감탄사 (헐, ㅋㅋㅋ, 어머나, 얼씨구)
 *
 * C Conjunction: 접속사
 *
 * j SubstantiveJosa: 조사 (의, 에, 에서)
 * l AdverbialJosa: 부사격 조사 (~인, ~의, ~일)
 * e Eomi: 어말어미 (다, 요, 여, 하댘ㅋㅋ)
 * r PreEomi: 선어말어미 (었)
 *
 * p NounPrefix: 접두사 ('초'대박)
 * v VerbPrefix: 동사 접두어 ('쳐'먹어)
 * s Suffix: 접미사 (~적)
 *
 * f Foreign: 한글이 아닌 문자들
 *
 * 지시사는 Derterminant로 대체하기로 함
 * Derterminant is used for demonstratives.
 *
 * Korean: Korean chunk (candidate for parsing)
 * Foreign: Mixture of non-Korean strings
 * Number: 숫자
 * Emotion: Korean Single Character Emotions (ㅋㅋㅋㅋ, ㅎㅎㅎㅎ, ㅠㅜㅠㅜ)
 * Alpha: Alphabets 알파벳
 * Punctuation: 문장부호
 * Hashtag: Twitter Hashtag 해쉬태그 #Korean
 * ScreenName: Twitter username (@nlpenguin)
 *
 * Unkown: Could not parse the string.
 */
object KoreanPos extends Enumeration {
  type KoreanPos = Value

  // Word leved POS
  val Noun, Verb, Adjective,
  Adverb, Determiner, Exclamation,
  Josa, Eomi, PreEomi, Conjunction,
  NounPrefix, VerbPrefix, Suffix, Unknown,

  // Chunk level POS
  Korean, Foreign, Number, KoreanParticle, Alpha,
  Punctuation, Hashtag, ScreenName,
  Email, URL, CashTag,

  // Functional POS
  Space, Others,

  ProperNoun = Value

  val OtherPoses = Set(Korean, Foreign, Number, KoreanParticle, Alpha,
    Punctuation, Hashtag, ScreenName,
    Email, URL, CashTag)

  val shortCut = Map(
    'N' -> Noun,
    'V' -> Verb,
    'J' -> Adjective,
    'A' -> Adverb,
    'D' -> Determiner,
    'E' -> Exclamation,
    'C' -> Conjunction,

    'j' -> Josa,
    'e' -> Eomi,
    'r' -> PreEomi,
    'p' -> NounPrefix,
    'v' -> VerbPrefix,
    's' -> Suffix,

    'a' -> Alpha,
    'n' -> Number,

    'o' -> Others
  )

  case class KoreanPosTrie(curPos: KoreanPos, nextTrie: List[KoreanPosTrie], ending: Option[KoreanPos])

  val selfNode = KoreanPosTrie(null, null, ending = None)

  protected[korean] def buildTrie(s: String, ending_pos: KoreanPos): List[KoreanPosTrie] = {
    def isFinal(rest: String): Boolean = {
      val isNextOptional = rest.foldLeft(true) {
        case (output: Boolean, c: Char) if c == '+' || c == '1' => false
        case (output: Boolean, c: Char) => output
      }
      rest.length == 0 || isNextOptional
    }

    if (s.length < 2) {
      return List()
    }

    val pos = shortCut(s.charAt(0))
    val rule = s.charAt(1)
    val rest = if (s.length > 1) {
      s.slice(2, s.length)
    } else {
      ""
    }

    val end: Option[KoreanPos] = if (isFinal(rest)) Some(ending_pos) else None

    rule match {
      case '+' =>
        List(KoreanPosTrie(pos, selfNode :: buildTrie(rest, ending_pos), end))
      case '*' =>
        List(KoreanPosTrie(pos, selfNode :: buildTrie(rest, ending_pos), end)) ++ buildTrie(rest, ending_pos)
      case '1' =>
        List(KoreanPosTrie(pos, buildTrie(rest, ending_pos), end))
      case '0' =>
        List(KoreanPosTrie(pos, buildTrie(rest, ending_pos), end)) ++ buildTrie(rest, ending_pos)
    }
  }

  protected[korean] def getTrie(sequences: Map[String, KoreanPos]): List[KoreanPosTrie] =
    sequences.foldLeft(List[KoreanPosTrie]()) {
      case (results: List[KoreanPosTrie], (s: String, ending_pos: KoreanPos)) =>
        buildTrie(s, ending_pos) ::: results
    }

  val Predicates = Set(Verb, Adjective)
}




