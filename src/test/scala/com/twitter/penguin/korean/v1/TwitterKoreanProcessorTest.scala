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

package com.twitter.penguin.korean.v1

import java.util.logging.{Level, Logger}

import com.twitter.penguin.korean.v1.TwitterKoreanProcessor.{tokenize, _}
import com.twitter.penguin.korean.v1.tokenizer.KoreanTokenizer._
import com.twitter.penguin.korean.v1.util.KoreanDictionaryProvider._
import com.twitter.penguin.korean.v1.util.KoreanPos._
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TwitterKoreanProcessorTest extends FunSuite {
  val LOG = Logger.getLogger(getClass.getSimpleName)

  case class ParseTime(time: Long, chunk: String)

  def time[R](block: => R): Long = {
    val t0 = System.currentTimeMillis()
    block
    val t1 = System.currentTimeMillis()
    t1 - t0
  }

  test("tokenizeText should tokenize Korean chunks correctly") {
    assert(tokenizeToStrings("한국어가 있는 Sentence") === Seq("한국어", "가", "있", "는", "Sentence"))
    assert(tokenizeToStrings("지각하겠닼ㅋㅋㅋㅋㅋ 그쵸") === Seq("지각", "하겠", "닼", "ㅋㅋㅋㅋㅋ", "그", "쵸"))
  }

  test("tokenizeToNormalizedStrings should tokenize Korean chunks correctly") {
    assert(tokenizeToNormalizedStrings("한국어가 있는 Sentence") === Seq("한국어", "가", "있", "는", "Sentence"))
    assert(tokenizeToNormalizedStrings("지각하겠닼ㅋㅋㅋㅋㅋ 그쵸") === Seq("지각", "하겠", "다", "ㅋㅋ", "그렇", "죠"))
  }

  test("tokenizeText should tokenize Korean tweets correctly") {
    assert(tokenizeToStrings("정지영 감독·명필름 심재명 대표 등 " +
        "영화인 20여 명이 세월호 참사 유가족의 광화문광장 단식 농성에 동참. http://bit.ly/1r9ZGul  pic.twitter.com/INLw7AF9Gu")
        === ("정지영, 감독, ·, 명필름, 심재명, 대표, 등, 영화인, 20, 여, 명, 이, " +
        "세월호, 참사, 유가족, 의, 광화문, 광장, 단식, " +
        "농성, 에, 동참, ., http://bit.ly/1r9ZGul, pic.twitter.com/INLw7AF9Gu").split(", ").toSeq)
  }

  test("tokenizeTextWithPos should tokenize into a sequence of KoreanTokens") {
    assert(
      tokenize("이태민 복근있다..!!!!!!  11자...ㅋㅋㅋㅋ 요뎡왕댜는 복근 따위 없어도돼 얼굴이 대신하니까!!!! ").mkString(" ")
          === "이태민Noun 복근Noun 있다Adjective ..!!!!!!Punctuation 11Number 자Noun " +
          "...Punctuation ㅋㅋㅋㅋKoreanParticle 요뎡왕댜Noun 는Josa 복근Noun 따위Noun " +
          "없어Adjective 도Eomi 돼Verb 얼굴Noun 이Josa 대신하니Verb 까Eomi !!!!Punctuation"
    )

    assert(
      tokenize("축하드리구요 부상얼른얼른 나으셔서 더좋은모습계속계속 보여주세요!! 얼른 부산오십쇼!! 보고싶습니다!!! 사랑해여 김캡틴♥♥♥").mkString(" ")
          === "축하Noun 드리Verb 구요Eomi 부상Noun 얼른얼른Adverb " +
          "나으Verb 셔PreEomi 서Eomi 더Noun 좋Adjective 은Eomi 모습Noun " +
          "계속계속Adverb 보여Verb 주PreEomi 세요Eomi !!Punctuation 얼른Noun 부산Noun 오십Noun " +
          "쇼Noun !!Punctuation 보고Verb 싶PreEomi 습니다Eomi !!!Punctuation " +
          "사랑해Verb 여Eomi 김Noun 캡틴Noun ♥♥♥Foreign"
    )

    assert(
      tokenize("와아아 페르세우스 유성우가 친창에 떨어진다!!!! 별이다!!!").mkString(" ")
          === "와아아Exclamation 페르세우스Noun 유성우Noun 가Josa 친창Noun* 에Josa " +
          "떨어진Verb 다Eomi !!!!Punctuation 별Noun 이다Josa !!!Punctuation"
    )
  }

  test("tokenizeTexWithIndex should correctly return indices of each token") {
    assert(
      tokenizeWithIndex("한국어가 있는 Sentence")
          === Seq(
        KoreanSegment(0, 3, KoreanToken("한국어", Noun)),
        KoreanSegment(3, 1, KoreanToken("가", Josa)),
        KoreanSegment(5, 1, KoreanToken("있", Adjective)),
        KoreanSegment(6, 1, KoreanToken("는", Eomi)),
        KoreanSegment(8, 8, KoreanToken("Sentence", Alpha))
      )
    )

    assert(
      tokenizeWithIndex("^///^규앙ㅇ")
          === Seq(
        KoreanSegment(0, 5, KoreanToken("^///^", Punctuation)),
        KoreanSegment(5, 2, KoreanToken("규앙", Exclamation)),
        KoreanSegment(7, 1, KoreanToken("ㅇ", KoreanParticle))
      )
    )
  }

  test("tokenizeWithNormalization should correctly tokenize applying tokenization") {
    assert(
      tokenizeWithNormalization("^///^규앙ㅇ").mkString(" ")
          === "^///^Punctuation 규앙Exclamation ㅇKoreanParticle"
    )
  }

  test("tokenizeWithNormalization should tokenize a long chunk within 0.5 sec") {
    // Ignore the first one to exclude the loading time.
    time(tokenizeWithNormalization("아그리고선생님"))

    assert(
      time(tokenizeWithNormalization("아그리고선생님")) < 1000
    )
    assert(
      time(tokenizeWithNormalization("아그리고선생님이사람의정말귀여운헐쵸귀여운개루루엄청작아서귀엽다안녕ㅋㅋ")) < 1000
    )
    assert(
      time(tokenizeWithNormalization("강원랜드잭팟이용하세요")) < 1000
    )
    assert(
      time(tokenizeWithNormalization("강원랜드잭팟강원랜드잭팟강원랜드잭팟강원랜드잭팟")) < 1000
    )
    assert(
      time(tokenizeWithNormalization("감동적인강남카지노브라보카지노라오스카지노강원랜드잭팟강원랜드잭팟강원랜드잭팟강원랜드잭팟강원랜드잭팟")) < 1000
    )
  }

  test("tokenizeToNormalizedStrings should tokenize company names correctly") {
    assert(
      tokenizeToNormalizedStrings("삼성전자서비스") === Seq("삼성", "전자", "서비스")
    )

    assert(
      tokenizeToNormalizedStrings("삼성정밀화학") === Seq("삼성", "정밀", "화학")
    )

    assert(
      tokenizeToNormalizedStrings("삼성그룹 현대중공업 한화케미칼 삼성전자스토어") ===
          Seq("삼성", "그룹", "현대", "중공업", "한화", "케미칼", "삼성", "전자", "스토어")
    )
  }

}
