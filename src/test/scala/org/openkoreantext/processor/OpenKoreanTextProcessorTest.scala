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

package org.openkoreantext.processor

import java.util.logging.Logger

import org.openkoreantext.processor.OpenKoreanTextProcessor._
import org.openkoreantext.processor.TestBase._
import org.openkoreantext.processor.tokenizer.TokenizerProfile
import org.openkoreantext.processor.util.{KoreanDictionaryProvider, KoreanPos}

class OpenKoreanTextProcessorTest extends TestBase {
  val LOG = Logger.getLogger(getClass.getSimpleName)
  test("normalize should correctly normalize") {
    assert(
      normalize("그랰ㅋㅋㅋㅋ 샤릉햌ㅋㅋ") === "그래ㅋㅋㅋ 사랑해ㅋㅋ"
    )
  }

  test("tokenizer should correctly reflect custom parameters") {
    assert(
      tokenize("스윗박스가 점점 좁아지더니, 의자 두개 붙여놓은 것만큼 좁아졌어요. 맘에드는이성분과 앉으면 가까워질거에요 ㅎㅎ").mkString("/")
          !== tokenize(
        "스윗박스가 점점 좁아지더니, 의자 두개 붙여놓은 것만큼 좁아졌어요. 맘에드는이성분과 앉으면 가까워질거에요 ㅎㅎ",
        TokenizerProfile(
          unknownPosCount = 1.0f,
          allNoun = 10,
          preferredPattern = 4
        ))
    )
  }

  test("tokenize should correctly tokenize ignoring punctuations") {
    assert(
      tokenize("^///^규앙ㅇ").mkString("/")
          === "^///^(Punctuation: 0, 5)/규앙(Exclamation: 5, 2)/ㅇ(KoreanParticle: 7, 1)"
    )
  }

  test("tokenize should correctly tokenize the example sentence") {
    assert(
      tokenize(normalize("한국어를 처리하는 예시입니닼ㅋㅋㅋㅋ")).mkString("/") ===
        "한국어(Noun: 0, 3)/를(Josa: 3, 1)/ (Space: 4, 1)/처리(Noun: 5, 2)/하는(Verb(하다): 7, 2)/" +
          " (Space: 9, 1)/예시(Noun: 10, 2)/입니다(Adjective(이다): 12, 3)/ㅋㅋㅋ(KoreanParticle: 15, 3)"
    )
  }

  test("extractPharase should correctly extract phrase from the example sentence") {
    assert(
      extractPhrases(tokenize(normalize("한국어를 처리하는 예시입니닼ㅋㅋㅋㅋ"))).mkString("/") ===
        "한국어(Noun: 0, 3)/처리(Noun: 5, 2)/처리하는 예시(Noun: 5, 7)/예시(Noun: 10, 2)"
    )
  }

  test("tokenize should tokenize a long chunk within reasonable time") {
    // Ignore the first one to exclude the loading time.
    time(tokenize("아그리고선생님"))

    assert(
      time(tokenize("아그리고선생님")) < 10000
    )
    assert(
      time(tokenize("아그리고선생님이사람의정말귀여운헐쵸귀여운개루루엄청작아서귀엽다안녕ㅋㅋ")) < 10000
    )
    assert(
      time(tokenize("강원랜드잭팟이용하세요")) < 10000
    )
    assert(
      time(tokenize("강원랜드잭팟강원랜드잭팟강원랜드잭팟강원랜드잭팟")) < 10000
    )
    assert(
      time(tokenize("감동적인강남카지노브라보카지노라오스카지노강원랜드잭팟강원랜드잭팟강원랜드잭팟강원랜드잭팟강원랜드잭팟")) < 10000
    )
    assert(
      time(tokenize("마키코레썸크리스마스블랙프라이데이" * 100)) < 100000
    )
  }

  test("tokenize should tokenize company names correctly") {
    assert(
      tokenize("삼성전자서비스").mkString("/") === "삼성(Noun: 0, 2)/전자(Noun: 2, 2)/서비스(Noun: 4, 3)"
    )

    assert(
      tokenize("삼성정밀화학").mkString("/") === "삼성(Noun: 0, 2)/정밀(Noun: 2, 2)/화학(Noun: 4, 2)"
    )

    assert(
      tokenize("삼성그룹 현대중공업 한화케미칼 삼성전자스토어").mkString("/") ===
          "삼성(Noun: 0, 2)/그룹(Noun: 2, 2)/ (Space: 4, 1)/현대(Noun: 5, 2)/중공업(Noun: 7, 3)/" +
              " (Space: 10, 1)/한화(Noun: 11, 2)/케미칼(Noun: 13, 3)/ (Space: 16, 1)/삼성(Noun: 17, 2)/" +
              "전자(Noun: 19, 2)/스토어(Noun: 21, 3)"
    )
  }

  test("tokenize should correctly tokenize the example set") {
    assertExamples(
      "current_parsing.txt", LOG,
      OpenKoreanTextProcessor.tokenize(_).mkString("/")
    )
  }

  test("splitSentences should correctly split sentences") {
    assert(
      splitSentences("가을이다! 남자는 가을을 탄다...... 그렇지? 루루야! 버버리코트 사러 가자!!!!").mkString("/") ===
          "가을이다!(0,5)/남자는 가을을 탄다......(6,22)/그렇지?(23,27)/루루야!(28,32)/버버리코트 사러 가자!!!!(33,48)"
    )
  }

  test("addNounsToDictionary should add nouns to the dictionary") {
    assert(!KoreanDictionaryProvider.koreanDictionary.get(KoreanPos.Noun).contains("후랴오교"))
    addNounsToDictionary(List("후랴오교"))
    assert(KoreanDictionaryProvider.koreanDictionary.get(KoreanPos.Noun).contains("후랴오교"))
  }

  test("tokenizeTopN should return top candidates") {
    assert(OpenKoreanTextProcessor.tokenizeTopN("대선 후보", 3).toString() ===
      "List(" +
        "List(" +
          "List(대선(Noun: 0, 2)), " +
          "List(대(Modifier: 0, 1), 선(Noun: 1, 1)), " +
          "List(대(Verb: 0, 1), 선(Noun: 1, 1))), " +
        "List(List( (Space: 2, 1))), " +
        "List(" +
          "List(후보(Noun: 3, 2)), " +
          "List(후보*(Noun: 3, 2)), " +
          "List(후(Noun: 3, 1), 보(Verb: 4, 1))))"
    )
  }
}
