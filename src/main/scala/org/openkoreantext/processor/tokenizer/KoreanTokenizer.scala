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

package org.openkoreantext.processor.tokenizer

import java.util

import org.openkoreantext.processor.stemmer.KoreanStemmer
import org.openkoreantext.processor.tokenizer.KoreanChunker._
import org.openkoreantext.processor.util.KoreanDictionaryProvider._
import org.openkoreantext.processor.util.KoreanPos
import org.openkoreantext.processor.util.KoreanPos._
import org.openkoreantext.processor.util.KoreanSubstantive._

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
  * Provides Korean tokenization.
  *
  * Chunk: 어절 - 공백으로 구분되어 있는 단위 (사랑하는사람을)
  * Word: 단어 - 하나의 문장 구성 요소 (사랑하는, 사람을)
  * Token: 토큰 - 형태소와 비슷한 단위이지만 문법적으로 정확하지는 않음 (사랑, 하는, 사람, 을)
  *
  * Whenever there is an updates in the behavior of KoreanParser,
  * the initial cache has to be updated by running tools.CreateInitialCache.
  */
object KoreanTokenizer {
  private val TOP_N_PER_STATE = 5
  private val MAX_TRACE_BACK = 8
  /**
    * 0 for optional, 1 for required
    * * for optional repeatable, + for required repeatable
    *
    * Substantive: 체언 (초거대기업의)
    * Predicate: 용언 (하였었습니다, 개예뻤었다)
    * Modifier: 수식언 (모르는 할수도있는 보이기도하는 예뻐 예쁜 완전 레알 초인간적인 잘 잘한)
    * Standalone: 독립언
    * Functional: 관계언 (조사)
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
    * m Modifier: 관형사 ('초'대박)
    * v VerbPrefix: 동사 접두어 ('쳐'먹어)
    * s Suffix: 접미사 (~적)
    */
  private val SequenceDefinition = Map(
    // Substantive
    "D0m*N1s0j0" -> Noun,
    // Predicate 초기뻐하다, 와주세요, 초기뻤었고, 추첨하다, 구경하기힘들다, 기뻐하는, 기쁜, 추첨해서, 좋아하다, 걸려있을
    "v*V1r*e0" -> Verb,
    "v*J1r*e0" -> Adjective,
    // Modifier 부사
    "A1" -> Adverb,
    // Standalone
    "C1" -> Conjunction,
    "E+" -> Exclamation,
    "j1" -> Josa
  )
  private val koreanPosTrie = KoreanPos.getTrie(SequenceDefinition)

  /**
    * Parse Korean text into a sequence of KoreanTokens with custom parameters
    *
    * @param text Input Korean chunk
    * @return sequence of KoreanTokens
    */
  def tokenize(text: CharSequence,
               profile: TokenizerProfile = TokenizerProfile.defaultProfile
              ): Seq[KoreanToken] = {
    val tokenized = tokenizeTopN(text, 1, profile).flatMap(_.head)
    KoreanStemmer.stem(tokenized)
  }

  /**
    * Parse Korean text into a sequence of KoreanTokens with custom parameters
    *
    * @param text Input Korean chunk
    * @return sequence of KoreanTokens
    */
  def tokenizeTopN(text: CharSequence,
                   topN: Int = 1,
                   profile: TokenizerProfile = TokenizerProfile.defaultProfile
                  ): Seq[Seq[Seq[KoreanToken]]] = {
    try
      chunk(text).map {
        case token: KoreanToken if token.pos == Korean =>
          // Get the best parse of each chunk
          val parsed = parseKoreanChunk(token, profile, topN)

          // Collapse sequence of one-char nouns into one unknown noun: (가Noun 회Noun -> 가회Noun*)
          parsed.map(collapseNouns)
        case token: KoreanToken => Seq(Seq(token))
      }
    catch {
      case e: Exception =>
        System.err.println(s"Error tokenizing a chunk: $text")
        throw e
    }
  }

  /**
    * Find the best parse using dynamic programming.
    *
    * @param chunk Input chunk. The input has to be entirely. Check for input validity is skipped
    *              for performance optimization. This method is private and is called only by tokenize.
    * @return The best possible parse.
    */
  private[this] def parseKoreanChunk(chunk: KoreanToken,
                                     profile: TokenizerProfile = TokenizerProfile.defaultProfile,
                                     topN: Int = 1
                                    ): Seq[Seq[KoreanToken]] = {

    val candidates = findTopCandidates(chunk, profile)
    candidates.take(topN)
  }

  private def findTopCandidates(chunk: KoreanToken, profile: TokenizerProfile) = {
    val directMatch: Seq[Seq[KoreanToken]] = findDirectMatch(chunk)

    // Buffer for solutions
    val solutions: util.HashMap[Int, List[CandidateParse]] = new java.util.HashMap[Int, List[CandidateParse]]

    // Initial state
    solutions.put(0, List(
      CandidateParse(
        ParsedChunk(Seq[KoreanToken](), 1, profile),
        koreanPosTrie, ending = None
      )
    ))

    // Find N best parses per state
    for (
      end <- 1 to chunk.length;
      start <- end - 1 to(Seq(end - MAX_TRACE_BACK, 0).max, -1)
    ) {
      val word = chunk.text.slice(start, end)

      val curSolutions = solutions.get(start)

      val candidates = curSolutions.flatMap {
        solution =>
          val possiblePoses: Seq[PossibleTrie] = if (solution.ending.isDefined) {
            solution.curTrie.map(t => PossibleTrie(t, 0)) ++ koreanPosTrie.map(
              t => PossibleTrie(t, 1))
          } else {
            solution.curTrie.map(t => PossibleTrie(t, 0))
          }

          possiblePoses.view.filter { t =>
            t.curTrie.curPos == Noun || koreanDictionary.get(t.curTrie.curPos).contains(
              word.toCharArray)
          }.map { t: PossibleTrie =>
            val candidateToAdd =
              if (t.curTrie.curPos == Noun && !koreanDictionary.get(Noun).contains(word.toCharArray)) {
                val isWordName: Boolean = isName(word)
                val isWordKoreanNameVariation: Boolean = isKoreanNameVariation(word)

                val unknown = !isWordName && !isKoreanNumber(word) && !isWordKoreanNameVariation
                val pos = Noun
                ParsedChunk(Seq(KoreanToken(word, pos, chunk.offset + start, word.length, unknown = unknown)),
                  t.words, profile)
              } else {
                val pos = t.curTrie.curPos
                ParsedChunk(Seq(KoreanToken(word, pos, chunk.offset + start, word.length)), t.words,
                  profile)
              }

            val nextTrie = t.curTrie.nextTrie.map {
              case nt: KoreanPosTrie if nt == selfNode => t.curTrie
              case nt: KoreanPosTrie => nt
            }

            CandidateParse(solution.parse ++ candidateToAdd, nextTrie, t.curTrie.ending)
          }
      }

      val currentSolutions = if (solutions.containsKey(end)) solutions.get(end) else List()

      solutions.put(end, (currentSolutions ++ candidates).sortBy {
        c => (c.parse.score, c.parse.posTieBreaker)
      }.take(TOP_N_PER_STATE))
    }


    val topCandidates: Seq[Seq[KoreanToken]] = if (solutions.get(chunk.length).isEmpty) {
      // If the chunk is not parseable, treat it as a unknown noun chunk.
      Seq(Seq(KoreanToken(chunk.text, Noun, 0, chunk.length, unknown = true)))
    } else {
      // Return the best parse of the final state
      solutions.get(chunk.length).sortBy(c => c.parse.score).map { p => p.parse.posNodes }
    }

    (directMatch ++ topCandidates).distinct
  }

  private def findDirectMatch(chunk: KoreanToken): Seq[Seq[KoreanToken]] = {
    // Direct match
    // This may produce 하 -> PreEomi
    koreanDictionary.asScala.foreach {
      case (pos, dict) =>
        if (dict.contains(chunk.text)) {
          return Seq(Seq(KoreanToken(chunk.text, pos, chunk.offset, chunk.length)))
        }
    }
    Seq()
  }

  case class KoreanToken(text: String, pos: KoreanPos, offset: Int, length: Int,
                         stem: Option[String] = None, unknown: Boolean = false) {
    override def toString: String = {
      val unknownStar = if (unknown) "*" else ""
      val stemString = if (stem.isDefined) "(%s)".format(stem.get) else ""
      s"$text$unknownStar(${pos.toString}$stemString: $offset, $length)"
    }

    def copyWithNewPos(pos: KoreanPos): KoreanToken = {
      KoreanToken(this.text, pos, this.offset, this.length, unknown = this.unknown)
    }
  }

  private case class CandidateParse(parse: ParsedChunk, curTrie: List[KoreanPosTrie],
                                    ending: Option[KoreanPos])

  private case class PossibleTrie(curTrie: KoreanPosTrie, words: Int)

}