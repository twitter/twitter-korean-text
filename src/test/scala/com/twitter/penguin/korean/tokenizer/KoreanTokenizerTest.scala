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

import com.twitter.penguin.korean.tokenizer.KoreanTokenizer._
import com.twitter.penguin.korean.util.KoreanPos._
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class KoreanTokenizerTest extends FunSuite {
  test("buildTrie should build Trie correctly for initial optionals with final non-optionals") {
    // 0 -> 1
    assert(
      buildTrie("p0N1") === List(
        KoreanPosTrie(NounPrefix, List(
          KoreanPosTrie(Noun, List(), ending = true)
        ), ending = false),
        KoreanPosTrie(Noun, List(), ending = true)
      )
    )
    // * -> +
    assert(
      buildTrie("p*N+") === List(
        KoreanPosTrie(NounPrefix, List(
          selfNode,
          KoreanPosTrie(Noun, List(selfNode), ending = true)
        ), ending = false),
        KoreanPosTrie(Noun, List(selfNode), ending = true)
      )
    )
  }
  test("buildTrie should build Trie correctly for initial optionals with multiple non-optionals") {
    // 0 -> 0 -> 1
    assert(
      buildTrie("p0N0s1") === List(
        KoreanPosTrie(NounPrefix, List(
          KoreanPosTrie(Noun, List(
            KoreanPosTrie(Suffix, List(), ending = true)
          ), ending = false),
          KoreanPosTrie(Suffix, List(), ending = true)
        ), ending = false),
        KoreanPosTrie(Noun, List(
          KoreanPosTrie(Suffix, List(), ending = true)
        ), ending = false),
        KoreanPosTrie(Suffix, List(), ending = true)
      )
    )
  }
  test("buildTrie should build Trie correctly for initial non-optionals with final non-optionals") {
    // 1 -> +
    assert(
      buildTrie("p1N+") === List(
        KoreanPosTrie(NounPrefix, List(
          KoreanPosTrie(Noun, List(
            selfNode
          ), ending = true)
        ), ending = false)
      )
    )
    // + -> 1
    assert(
      buildTrie("N+s1") === List(
        KoreanPosTrie(Noun, List(
          selfNode,
          KoreanPosTrie(Suffix, List(), ending = true)
        ), ending = false)
      )
    )
  }

  test("buildTrie should build Trie correctly for initial non-optionals with final optionals") {
    // 1 -> *
    assert(
      buildTrie("p1N*") === List(
        KoreanPosTrie(NounPrefix, List(
          KoreanPosTrie(Noun, List(
            selfNode
          ), ending = true)
        ), ending = true)
      )
    )
    // + -> 0
    assert(
      buildTrie("N+s0") === List(
        KoreanPosTrie(Noun, List(
          selfNode,
          KoreanPosTrie(Suffix, List(), ending = true)
        ), ending = true)
      )
    )
  }
  test("buildTrie should build Trie correctly for initial non-optionals with multiple non-optionals") {
    // + -> + -> 0
    assert(
      buildTrie("A+V+A0") === List(
        KoreanPosTrie(Adverb, List(
          selfNode,
          KoreanPosTrie(Verb, List(
            selfNode,
            KoreanPosTrie(Adverb, List(), ending = true)
          ), ending = true)
        ), ending = false)
      )
    )
  }

  val parsedChunk = ParsedChunk(
    List(KoreanToken("하", Noun), KoreanToken("하", Noun), KoreanToken("하", Noun)), 1
  )

  val parsedChunkWithTwoTokens = ParsedChunk(
    List(KoreanToken("하", Noun), KoreanToken("하", Noun)), 1
  )

  val parsedChunkWithUnknowns = ParsedChunk(
    List(KoreanToken("하하", Noun, unknown = true), KoreanToken("하", Noun, unknown = true), KoreanToken("하", Noun)), 1
  )

  val parsedChunkWithCommonNouns = ParsedChunk(
    List(KoreanToken("사람", Noun), KoreanToken("강아지", Noun)), 1
  )

  val parsedChunkWithVerbs = ParsedChunk(
    List(KoreanToken("사람", Noun), KoreanToken("하다", Verb)), 1
  )

  val parsedChunkWithExactMatch = ParsedChunk(
    List(KoreanToken("강아지", Noun)), 1
  )

  test("ParsedChunk should correctly count unknowns") {
    assert(
      parsedChunkWithUnknowns.countUnknowns === 2
    )

    assert(
      parsedChunk.countUnknowns === 0
    )
  }

  test("ParsedChunk should correctly count tokens") {
    assert(
      parsedChunk.countTokens === 3
    )

    assert(
      parsedChunkWithTwoTokens.countTokens === 2
    )
  }

  test("ParsedChunk should correctly return unknown coverage") {
    assert(
      parsedChunkWithUnknowns.getUnknownCoverage === 3
    )
    assert(
      parsedChunkWithTwoTokens.getUnknownCoverage === 0
    )
  }

  test("ParsedChunk should get correct frequency score") {
    assert(
      parsedChunkWithTwoTokens.getFreqScore === 1.0f
    )
    assert(
      parsedChunkWithCommonNouns.getFreqScore === 0.4544f
    )
  }

  test("ParsedChunk should correctly count POSes") {
    assert(
      parsedChunk.countPos(Noun) === 3
    )
    assert(
      parsedChunkWithVerbs.countPos(Noun) === 1
    )
    assert(
      parsedChunkWithVerbs.countPos(Verb) === 1
    )
  }

  test("ParsedChunk should correctly determine if the chunk is an exact match") {
    assert(
      parsedChunk.isExactMatch === 1
    )
    assert(
      parsedChunkWithExactMatch.isExactMatch === 0
    )
  }

  test("ParsedChunk should correctly determine if the chunk is all noun") {
    assert(
      parsedChunk.isAllNouns === 0
    )
    assert(
      parsedChunkWithVerbs.isAllNouns === 1
    )
  }

  test("tokenize should return expected tokens") {
    assert(
      tokenize("개루루야") ===
        List(KoreanToken("개", Noun), KoreanToken("루루", Noun), KoreanToken("야", Josa))
    )

    assert(
      tokenize("쵸귀여운") ===
        List(KoreanToken("쵸", VerbPrefix), KoreanToken("귀여운", Adjective))
    )

    assert(
      tokenize("이사람의") ===
        List(KoreanToken("이", Determiner), KoreanToken("사람", Noun), KoreanToken("의", Josa))
    )

    assert(
      tokenize("엄청작아서귀엽다") ===
        List(
          KoreanToken("엄청", Adverb),
          KoreanToken("작아", Adjective), KoreanToken("서", Eomi),
          KoreanToken("귀엽", Adjective), KoreanToken("다", Eomi))
    )

    assert(
      tokenize("안녕하셨어요") ===
        List(
          KoreanToken("안녕하셨", Adjective), KoreanToken("어요", Eomi)
        )
    )

    assert(
      tokenize("쵸귀여운개루루") ===
        List(
          KoreanToken("쵸", VerbPrefix), KoreanToken("귀여운", Adjective),
          KoreanToken("개", Noun), KoreanToken("루루", Noun)
        )
    )

    assert(
      tokenize("그리고") ===
        List(KoreanToken("그리고", Conjunction))
    )

    assert(
      tokenize("안녕ㅋㅋ") ===
        List(KoreanToken("안녕", Noun), KoreanToken("ㅋㅋ", KoreanParticle))
    )
  }

  test("tokenize should handle unknown nouns") {
    assert(
      tokenize("개컁컁아") ===
        List(KoreanToken("개컁컁", Noun, unknown = true), KoreanToken("아", Josa))
    )

    assert(
      tokenize("안녕하세요쿛툐캬님") ===
        List(KoreanToken("안녕하세", Adjective), KoreanToken("요", Eomi),
          KoreanToken("쿛툐캬", Noun, unknown = true), KoreanToken("님", Suffix))
    )
  }

  test("tokenize should handle edge cases") {
    assert(
      tokenize("이승기가") ===
        List(KoreanToken("이승기", Noun), KoreanToken("가", Josa))
    )

    assert(
      tokenize("야이건뭐").mkString(", ") ===
          "야Exclamation, 이건Noun, 뭐Noun"
    )

    assert(
      tokenize("아이럴수가").mkString(", ") ===
          "아Adverb, 이럴Adjective, 수PreEomi, 가Eomi"
    )
  }

  test("tokenize should be able to tokenize long non-space-correctable ones") {
    assert(
      tokenize("훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌쩍훌")
        .map(_.text).mkString(" ") ===
        "훌쩍 훌쩍 훌쩍 훌쩍 훌쩍 훌쩍 훌쩍 훌쩍 훌쩍 훌쩍 훌쩍 훌쩍 " +
          "훌쩍 훌쩍 훌쩍 훌쩍 훌쩍 훌쩍 훌쩍 훌쩍 훌쩍 훌쩍 훌쩍 훌쩍 훌"
    )
  }
}