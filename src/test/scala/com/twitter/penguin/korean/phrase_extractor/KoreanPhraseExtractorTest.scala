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
      stemming = false, keepSpace = true
    )
    assert(KoreanPhraseExtractor.collapsePos(tokens).mkString("") ===
      "이날Noun Space미국Noun의Josa Space" +
        "수백만Noun Space소비자들Noun은Josa Space" +
        "크리스마스Noun Space선물Noun을Josa Space" +
        "할인Noun된Verb Space가격Noun에Josa Space" +
        "사는Verb Space것Noun을Josa Space주Noun Space" +
        "목적Noun으로Josa Space쇼핑Noun을Josa Space" +
        "한다Verb.Punctuation")
  }
}
