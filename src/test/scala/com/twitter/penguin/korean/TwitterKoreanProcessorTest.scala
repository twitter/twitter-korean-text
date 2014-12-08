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

import com.twitter.penguin.korean.TwitterKoreanProcessor.{tokenize, _}
import com.twitter.penguin.korean.thriftscala.{ParseItem, ParsingGoldenset}
import com.twitter.penguin.korean.tokenizer.KoreanTokenizer.KoreanToken
import com.twitter.penguin.korean.util.KoreanDictionaryProvider._
import com.twitter.penguin.korean.util.KoreanPos
import com.twitter.penguin.korean.util.KoreanPos._
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
        === "이태민Noun 복근Noun 있다Adjective ..!!!!!!Punctuation 11Number 자Noun " +
        "...Punctuation ㅋㅋKoreanParticle 요정Noun 왕자Noun 는Josa 복근Noun 따위Noun " +
        "없다Adjective 돼다Verb 얼굴Noun 이Josa 대신Noun 하다Verb !!!!Punctuation"
    )

    assert(
      tokenize("축하드리구요 부상얼른얼른 나으셔서 더좋은모습계속계속 보여주세요!! 얼른 부산오십쇼!! 보고싶습니다!!! 사랑해여 김캡틴♥♥♥").mkString(" ")
        === "축하Noun 드리다Verb 부상Noun 얼른얼른Adverb 낫다Verb 더Noun 좋다Adjective 모습Noun 계속계속Adverb 보이다Verb " +
        "!!Punctuation 얼른Noun 부산Noun 오십Noun 쇼Noun !!Punctuation 보다Verb " +
        "!!!Punctuation 사랑Noun 하다Verb 김Noun 캡틴Noun ♥♥♥Foreign"
    )

    assert(
      tokenize("와아아 페르세우스 유성우가 친창에 떨어진다!!!! 별이다!!!").mkString(" ")
        === "와아아Exclamation 페르세우스Noun 유성우Noun 가Josa 친창Noun* 에Josa " +
        "떨어지다Verb !!!!Punctuation 별Noun 이다Josa !!!Punctuation"
    )
  }

  test("tokenizeTexWithIndex should correctly return indices of each token") {
    assert(
      tokenizeWithIndex("한국어가 있는 Sentence")
        === Seq(
        KoreanSegment(0, 3, KoreanToken("한국어", Noun)),
        KoreanSegment(3, 1, KoreanToken("가", Josa)),
        KoreanSegment(5, 2, KoreanToken("있는", Adjective)),
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

  test("tokenizeTexWithIndex should correctly return indices of stemmed tokens") {
    assert(
      tokenizeWithIndexWithStemmer("한국어가 있는 Sentence")
        === KoreanSegmentWithText(
        (new StringBuilder).append("한국어가 있다 Sentence"), Seq(
          KoreanSegment(0, 3, KoreanToken("한국어", Noun)),
          KoreanSegment(3, 1, KoreanToken("가", Josa)),
          KoreanSegment(5, 2, KoreanToken("있다", Adjective)),
          KoreanSegment(8, 8, KoreanToken("Sentence", Alpha))
        )
      )
    )

    assert(
      tokenizeWithIndexWithStemmer("^///^규앙ㅇ")
        === KoreanSegmentWithText(
        (new StringBuilder).append("^///^규앙ㅇ"), Seq(
          KoreanSegment(0, 5, KoreanToken("^///^", Punctuation)),
          KoreanSegment(5, 2, KoreanToken("규앙", Exclamation)),
          KoreanSegment(7, 1, KoreanToken("ㅇ", KoreanParticle))
        )
      )
    )
  }

  test("tokenize should correctly tokenize ignoring punctuations") {
    assert(
      tokenize("^///^규앙ㅇ").mkString(" ")
        === "^///^Punctuation 규앙Exclamation ㅇKoreanParticle"
    )
  }

  test("tokenize should tokenize a long chunk within 0.5 sec") {
    // Ignore the first one to exclude the loading time.
    time(tokenize("아그리고선생님"))

    assert(
      time(tokenize("아그리고선생님")) < 1000
    )
    assert(
      time(tokenize("아그리고선생님이사람의정말귀여운헐쵸귀여운개루루엄청작아서귀엽다안녕ㅋㅋ")) < 1000
    )
    assert(
      time(tokenize("강원랜드잭팟이용하세요")) < 1000
    )
    assert(
      time(tokenize("강원랜드잭팟강원랜드잭팟강원랜드잭팟강원랜드잭팟")) < 1000
    )
    assert(
      time(tokenize("감동적인강남카지노브라보카지노라오스카지노강원랜드잭팟강원랜드잭팟강원랜드잭팟강원랜드잭팟강원랜드잭팟")) < 1000
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

  test("tokenize should correctly tokenize the goldenset") {
    assert({
      val input = readFileByLineFromResources("current_parsing.txt")

      val (parseTimes, hasErrors) = input.foldLeft((List[ParseTime](), true)) {
        case ((l: List[ParseTime], output: Boolean), line: String) =>
          val s = line.split("\t")
          val (chunk, parse) = (s(0), s(1))
          val oldTokens = parse
          val t0 = System.currentTimeMillis()
          val newTokens = TwitterKoreanProcessor.tokenize(chunk).mkString(" ")
          val t1 = System.currentTimeMillis()

          val oldParseMatches = oldTokens == newTokens

          if (!oldParseMatches) {
            System.err.println("Goldenset Match Error: %s (%s) -> (%s)".format(
              chunk, oldTokens, newTokens))
          }

          (ParseTime(t1 - t0, chunk) :: l, output && oldParseMatches)
      }

      val averageTime = parseTimes.map(_.time).sum.toDouble / parseTimes.size
      val maxItem = parseTimes.maxBy(_.time)

      LOG.log(Level.INFO, ("Parsed %d chunks. \n" +
        "       Total time: %d ms \n" +
        "       Average time: %.2f ms \n" +
        "       Max time: %d ms, %s").format(
          parseTimes.size,
          parseTimes.map(_.time).sum,
          averageTime,
          maxItem.time,
          maxItem.chunk
        ))
      hasErrors
    }, "Some parses did not match the goldenset.")
  }
}
