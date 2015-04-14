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

  test("tokenizeToStrings should tokenize without normalization or stemming") {
    assert(tokenizeToStrings("한국어가 있는 Sentence", normalize = false, stem = false)
        === Seq("한국어", "가", "있는", "Sentence"))
    assert(tokenizeToStrings("지각하겠닼ㅋㅋㅋㅋㅋ 그쵸", normalize = false, stem = false)
        === Seq("지각", "하겠", "닼", "ㅋㅋㅋㅋㅋ", "그", "쵸"))
  }

  test("tokenizeToStrings should tokenize with normalization") {
    assert(tokenizeToStrings("한국어가 있는 Sentence", normalize = true, stem = false)
        === Seq("한국어", "가", "있는", "Sentence"))
    assert(tokenizeToStrings("지각하겠닼ㅋㅋㅋㅋㅋ 그쵸", normalize = true, stem = false)
        === Seq("지각", "하겠", "다", "ㅋㅋ", "그렇", "죠"))
  }

  test("tokenizeToStrings should tokenize with stemming") {
    assert(tokenizeToStrings("한국어가 있는 Sentence", normalize = false, stem = true)
        === Seq("한국어", "가", "있다", "Sentence"))
    assert(tokenizeToStrings("지각하겠닼ㅋㅋㅋㅋㅋ 그쵸", normalize = false, stem = true)
        === Seq("지각", "하다", "닼", "ㅋㅋㅋㅋㅋ", "그", "쵸"))
  }

  test("tokenizeToStrings should tokenize with normalization and stemming") {
    assert(tokenizeToStrings("한국어가 있는 Sentence")
        === Seq("한국어", "가", "있다", "Sentence"))
    assert(tokenizeToStrings("지각하겠닼ㅋㅋㅋㅋㅋ 그쵸")
        === Seq("지각", "하다", "ㅋㅋ", "그렇다"))
    assert(tokenizeToStrings("라고만")
        === Seq("라고만"))
  }

  test("tokenizeToStrings should tokenize Korean tweets correctly") {
    assert(tokenizeToStrings("정지영 감독·명필름 심재명 대표 등 " +
        "영화인 20여 명이 세월호 참사 유가족의 광화문광장 단식 농성에 동참. http://bit.ly/1r9ZGul  pic.twitter.com/INLw7AF9Gu")
        === ("정지영, 감독, ·, 명필름, 심재명, 대표, 등, 영화인, 20, 여, 명, 이, " +
        "세월호, 참사, 유가족, 의, 광화문, 광장, 단식, " +
        "농성, 에, 동참, ., http://bit.ly/1r9ZGul, pic.twitter.com/INLw7AF9Gu").split(", ").toSeq)
  }

  test("tokenize should tokenize into a sequence of KoreanTokens") {
    assert(
      tokenize("이태민 복근있다..!!!!!!  11자...ㅋㅋㅋㅋ 요뎡왕댜는 복근 따위 없어도돼 얼굴이 대신하니까!!!! ").mkString(" ")
          === "이태민(Noun: 0, 3) 복근(Noun: 4, 2) 있다(Adjective: 6, 2) ..!!!!!!(Punctuation: 8, 8) " +
        "11(Number: 18, 2) 자(Noun: 20, 1) ...(Punctuation: 21, 3) ㅋㅋ(KoreanParticle: 24, 2) " +
        "요정(Noun: 27, 2) 왕자(Noun: 29, 2) 는(Josa: 31, 1) 복근(Noun: 33, 2) 따위(Noun: 36, 2) " +
        "없다(Adjective: 39, 3) 돼다(Verb: 42, 1) 얼굴(Noun: 44, 2) 이(Josa: 46, 1) 대신(Noun: 48, 2) " +
        "하다(Verb: 50, 3) !!!!(Punctuation: 53, 4)"
    )

    assert(
      tokenize("축하드리구요 부상얼른얼른 나으셔서 더좋은모습계속계속 보여주세요!! 얼른 부산오십쇼!! 보고싶습니다!!! 사랑해여 김캡틴♥♥♥").mkString(" ")
          === "축하(Noun: 0, 2) 드리다(Verb: 2, 4) 부상(Noun: 7, 2) 얼른얼른(Adverb: 9, 4) " +
        "낫다(Verb: 14, 4) 더(Noun: 19, 1) 좋다(Adjective: 20, 2) 모습(Noun: 22, 2) " +
        "계속계속(Adverb: 24, 4) 보이다(Verb: 29, 5) !!(Punctuation: 34, 2) 얼른(Noun: 37, 2) " +
        "부산(Noun: 40, 2) 오십(Noun: 42, 2) 쇼(Noun: 44, 1) !!(Punctuation: 45, 2) " +
        "보다(Verb: 48, 6) !!!(Punctuation: 54, 3) 사랑(Noun: 58, 2) 하다(Verb: 60, 2) " +
        "김(Noun: 63, 1) 캡틴(Noun: 64, 2) ♥♥♥(Foreign: 66, 3)"
    )

    assert(
      tokenize("와아아 페르세우스 유성우가 친창에 떨어진다!!!! 별이다!!!").mkString(" ")
          === "와아아(Exclamation: 0, 3) 페르세우스(Noun: 4, 5) 유성우(Noun: 10, 3) 가(Josa: 13, 1) " +
        "친창*(Noun: 15, 2) 에(Josa: 17, 1) 떨어지다(Verb: 19, 4) !!!!(Punctuation: 23, 4) " +
        "별(Noun: 28, 1) 이다(Josa: 29, 2) !!!(Punctuation: 31, 3)"
    )

    assert(
      tokenize("'넥서스' 갤럭시 Galaxy S5").mkString(" ")
          === "'(Punctuation: 0, 1) 넥서스(Noun: 1, 3) '(Punctuation: 4, 1) " +
        "갤럭시(Noun: 6, 3) Galaxy(Alpha: 10, 6) S(Alpha: 17, 1) 5(Number: 18, 1)"
    )
  }

  test("tokenize should correctly tokenize ignoring punctuations") {
    assert(
      tokenize("^///^규앙ㅇ").mkString(" ")
          === "^///^(Punctuation: 0, 5) 규앙(Exclamation: 5, 2) ㅇ(KoreanParticle: 7, 1)"
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

  test("tokenizeToStrings should tokenize company names correctly") {
    assert(
      tokenizeToStrings("삼성전자서비스") === Seq("삼성", "전자", "서비스")
    )

    assert(
      tokenizeToStrings("삼성정밀화학") === Seq("삼성", "정밀", "화학")
    )

    assert(
      tokenizeToStrings("삼성그룹 현대중공업 한화케미칼 삼성전자스토어") ===
          Seq("삼성", "그룹", "현대", "중공업", "한화", "케미칼", "삼성", "전자", "스토어")
    )
  }

  test("tokenize should correctly tokenize the example set") {
    assertExamples(
      "current_parsing.txt", LOG,
      TwitterKoreanProcessor.tokenize(_).mkString("/")
    )
  }

  test("extractPhrases should correctly extract phrases") {
    assert(
      TwitterKoreanProcessor.extractPhrases(
        "시발 토토가의 인기폭발을 보니 미국에서 뉴키즈온더블럭 백스트릿보이스 조인트 컨서트"
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
        "시발 토토가의 인기폭발을 보니 미국에서 뉴키즈온더블럭 백스트릿보이스 조인트 컨서트", filterSpam = true
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
