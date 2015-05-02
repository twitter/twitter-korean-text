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

package com.twitter.penguin.korean

import java.util.logging.{Level, Logger}

import com.twitter.penguin.korean.TestBase._
import com.twitter.penguin.korean.TwitterKoreanProcessor.{tokenize, _}
import com.twitter.penguin.korean.tokenizer.KoreanTokenizer.KoreanToken
import com.twitter.penguin.korean.util.KoreanDictionaryProvider._
import com.twitter.penguin.korean.util.KoreanPos._

class TwitterKoreanProcessorTest extends TestBase {
  val LOG = Logger.getLogger(getClass.getSimpleName)
  test("normalize should correctly normalize") {
    assert(
      normalize("그랰ㅋㅋㅋ 샤릉햌ㅋㅋ") === "그래ㅋㅋ 사랑해ㅋㅋ"
    )
  }

  test("tokenize should tokenize into a sequence of KoreanTokens") {
    assert(
      tokenize("이태민 복근있다..!!!!!!  11자...ㅋㅋㅋㅋ").mkString("/")
          === "이태민(Noun: 0, 3)/ (Space: 3, 1)/복근(Noun: 4, 2)/있다(Adjective: 6, 2)/" +
        "..!!!!!!(Punctuation: 8, 8)/ (Space: 16, 1)/ (Space: 17, 1)/11(Number: 18, 2)/" +
        "자(Noun: 20, 1)/...(Punctuation: 21, 3)/ㅋㅋㅋㅋ(KoreanParticle: 24, 4)"
    )

    assert(
      tokenize("요뎡왕댜는 복근 따위 없어도돼 얼굴이 대신하니까!!!! ").mkString("/")
        === "요뎡왕댜(Noun: 0, 4)/는(Josa: 4, 1)/ (Space: 5, 1)/복근(Noun: 6, 2)/" +
        " (Space: 8, 1)/따위(Noun: 9, 2)/ (Space: 11, 1)/없어(Adjective: 12, 2)/도(Eomi: 14, 1)/" +
        "돼(Verb: 15, 1)/ (Space: 16, 1)/얼굴(Noun: 17, 2)/이(Josa: 19, 1)/ (Space: 20, 1)/" +
        "대신하니(Verb: 21, 4)/까(Eomi: 25, 1)/!!!!(Punctuation: 26, 4)/ (Space: 30, 1)"
    )

    assert(
      tokenize("얼른 부산오십쇼!! 보고싶습니다!!! 사랑해여 김캡틴♥♥♥").mkString("/")
          === "얼른(Noun: 0, 2)/ (Space: 2, 1)/부산(Noun: 3, 2)/오십(Noun: 5, 2)/쇼(Noun: 7, 1)/" +
        "!!(Punctuation: 8, 2)/ (Space: 10, 1)/보고(Verb: 11, 2)/싶(PreEomi: 13, 1)/" +
        "습니다(Eomi: 14, 3)/!!!(Punctuation: 17, 3)/ (Space: 20, 1)/사랑해(Verb: 21, 3)/" +
        "여(Eomi: 24, 1)/ (Space: 25, 1)/김(Noun: 26, 1)/캡틴(Noun: 27, 2)/♥♥♥(Foreign: 29, 3)"
    )

    assert(
      tokenize("와아아 페르세우스 유성우가 친창에 떨어진다!!!!").mkString("/")
          === "와아아(Exclamation: 0, 3)/ (Space: 3, 1)/페르세우스(Noun: 4, 5)/" +
        " (Space: 9, 1)/유성우(Noun: 10, 3)/가(Josa: 13, 1)/ (Space: 14, 1)/친창*(Noun: 15, 2)/" +
        "에(Josa: 17, 1)/ (Space: 18, 1)/떨어진(Verb: 19, 3)/다(Eomi: 22, 1)/!!!!(Punctuation: 23, 4)"
    )

    assert(
      tokenize("'넥서스' 갤럭시 Galaxy S5").mkString("/")
          === "'(Punctuation: 0, 1)/넥서스(Noun: 1, 3)/'(Punctuation: 4, 1)/ (Space: 5, 1)/" +
        "갤럭시(Noun: 6, 3)/ (Space: 9, 1)/Galaxy(Alpha: 10, 6)/ (Space: 16, 1)/" +
        "S(Alpha: 17, 1)/5(Number: 18, 1)"
    )
  }

  test("tokenize should correctly tokenize ignoring punctuations") {
    assert(
      tokenize("^///^규앙ㅇ").mkString("/")
          === "^///^(Punctuation: 0, 5)/규앙(Exclamation: 5, 2)/ㅇ(KoreanParticle: 7, 1)"
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
      TwitterKoreanProcessor.tokenize(_).mkString("/")
    )
  }

  test("stem should correctly stem the tokenized words") {
    val tokens = tokenize("게으른 아침이 밝았구나.")

    assert(stem(tokens).mkString("/") ===
      "게으르다(Adjective: 0, 3)/ (Space: 3, 1)/아침(Noun: 4, 2)/이(Josa: 6, 1)/ (Space: 7, 1)/" +
        "밝다(Verb: 8, 4)/.(Punctuation: 12, 1)")
  }

  test("extractPhrases should correctly extract phrases") {

    val tokens = tokenize("시발 토토가의 인기폭발을 보니 미국에서 뉴키즈온더블럭 백스트릿보이스 조인트 컨서트")

    assert(
      TwitterKoreanProcessor.extractPhrases(
        tokens
      ).mkString(", ") ===
          "시발(Noun: 0, 2), 시발 토토가(Noun: 0, 6), 시발 토토가의 인기폭발(Noun: 0, 12), " +
            "토토가의 인기폭발(Noun: 3, 9), 인기폭발(Noun: 8, 4), 미국(Noun: 17, 2), " +
            "뉴키즈온더블럭(Noun: 22, 7), 뉴키즈온더블럭 백스트릿보이스(Noun: 22, 15), " +
            "뉴키즈온더블럭 백스트릿보이스 조인트(Noun: 22, 19), 백스트릿보이스 조인트(Noun: 30, 11), " +
            "뉴키즈온더블럭 백스트릿보이스 조인트 컨서트(Noun: 22, 23), 백스트릿보이스 조인트 컨서트(Noun: 30, 15), " +
            "조인트 컨서트(Noun: 38, 7), 토토가(Noun: 3, 3), 인기(Noun: 8, 2), 폭발(Noun: 10, 2), " +
            "스트릿(Noun: 31, 3), 보이스(Noun: 34, 3), 조인트(Noun: 38, 3), 컨서트(Noun: 42, 3)"
    )

    assert(
      TwitterKoreanProcessor.extractPhrases(
        tokens, filterSpam = true
      ).mkString(", ") ===
          "토토가(Noun: 3, 3), 토토가의 인기폭발(Noun: 3, 9), 인기폭발(Noun: 8, 4), 미국(Noun: 17, 2), " +
            "뉴키즈온더블럭(Noun: 22, 7), 뉴키즈온더블럭 백스트릿보이스(Noun: 22, 15), " +
            "뉴키즈온더블럭 백스트릿보이스 조인트(Noun: 22, 19), 백스트릿보이스 조인트(Noun: 30, 11), " +
            "뉴키즈온더블럭 백스트릿보이스 조인트 컨서트(Noun: 22, 23), 백스트릿보이스 조인트 컨서트(Noun: 30, 15), " +
            "조인트 컨서트(Noun: 38, 7), 인기(Noun: 8, 2), 폭발(Noun: 10, 2), 스트릿(Noun: 31, 3), " +
            "보이스(Noun: 34, 3), 조인트(Noun: 38, 3), 컨서트(Noun: 42, 3)"
    )
  }
}
