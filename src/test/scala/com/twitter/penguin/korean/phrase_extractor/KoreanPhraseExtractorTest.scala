package com.twitter.penguin.korean.phrase_extractor

import com.twitter.penguin.korean.TwitterKoreanProcessor
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class KoreanPhraseExtractorTest extends FunSuite {
  test("collapsePos correctly collapse KoreanPos sequences") {
    val tokens = TwitterKoreanProcessor.tokenize(
      "이날 미국의 수백만 소비자들은 크리스마스 선물을 할인된 가격에 사는 것을 주 목적으로 쇼핑을 한다.",
      stemming = false
    )
    println(tokens)
    println(
      "-------->", KoreanPhraseExtractor.collapsePos(tokens)
    )

  }

}
