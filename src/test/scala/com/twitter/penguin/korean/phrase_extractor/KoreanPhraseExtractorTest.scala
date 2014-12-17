package com.twitter.penguin.korean.phrase_extractor

import com.twitter.penguin.korean.TwitterKoreanProcessor
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class KoreanPhraseExtractorTest extends FunSuite {
  val text1 = "블랙프라이데이: 이날 미국의 수백만 소비자들은 크리스마스 선물을 할인된 가격에 사는 것을 주 목적으로 블랙프라이데이 쇼핑을 한다."
  val text2 = "결정했어. 마키 코레썸 사주시는 분께는 허니버터칩 한 봉지를 선물할 것이다."
  val text3 = "[단독]정부, 새 고용 형태 ＇중규직＇ 만든다 http://durl.me/7sm553  / 이름도 바뀌겟군. 정규직은 상규직, 비정규직은 하규직. 중규직 참 창조적이다. 결국 기업은 비정규직으로 이용할게 뻔함."
  val superLongText: String = "허니버터칩 정규직 크리스마스 " * 50

  def time[R](block: => R): Long = {
    val t0 = System.currentTimeMillis()
    block
    val t1 = System.currentTimeMillis()
    t1 - t0
  }

  def tokenize(text: String) = TwitterKoreanProcessor.tokenize(
    text, stemming = false, keepSpace = true
  )

  test("collapsePos correctly collapse KoreanPos sequences") {
    assert(KoreanPhraseExtractor.collapsePos(tokenize(text1)).mkString("") ===
        "블랙프라이데이Noun:Punctuation Space이날Noun Space미국Noun의Josa Space수백만Noun Space소비자들Noun은Josa" +
            " Space크리스마스Noun Space선물Noun을Josa Space할인Noun된Verb Space가격Noun에Josa Space사는Verb" +
            " Space것Noun을Josa Space주Noun Space목적Noun으로Josa Space블랙프라이데이Noun Space쇼핑Noun을Josa" +
            " Space한다Verb.Punctuation")

    assert(KoreanPhraseExtractor.collapsePos(tokenize(text2)).mkString("") ===
        "결정Noun했어Verb.Punctuation Space마키Noun Space코레썸Noun Space사주시는Verb Space분께는Verb" +
            " Space허니버터칩Noun Space한Noun 봉지Noun를Josa Space선물할Verb Space것Noun이다Josa.Punctuation")
  }

  test("extractPhrases correctly extracts phrases") {
    assert(KoreanPhraseExtractor.extractPhrases(tokenize(text1)).mkString(", ") ===
        "블랙프라이데이Noun, 이날 미국의 수백만 소비자들Noun, 미국의 수백만 소비자들Noun, 수백만 소비자들Noun, 소비자들Noun, " +
            "크리스마스 선물Noun, 할인된 가격Noun, 주 목적Noun, 블랙프라이데이 쇼핑Noun, 수백만Noun, 크리스마스Noun")
  }

  test("extractPhrases correctly extracts phrases from a string") {
    assert(KoreanPhraseExtractor.extractPhrases(text1).mkString(", ") ===
        "블랙프라이데이, 이날 미국의 수백만 소비자들, 미국의 수백만 소비자들, 수백만 소비자들, 소비자들, 크리스마스 선물, " +
            "할인된 가격, 주 목적, 블랙프라이데이 쇼핑, 수백만, 크리스마스")
    assert(KoreanPhraseExtractor.extractPhrases(text2).mkString(", ") ===
        "허니버터칩 한 봉지, 한 봉지, 코레썸, 허니버터칩")
    assert(KoreanPhraseExtractor.extractPhrases(text3).mkString(", ") ===
        "새 고용 형태, 고용 형태, 중규직, 정규직, 상규직, 비정규직, 하규직")
  }

  test("extractPhrases should extract long noun-only phrases in reasonable time") {
    assert(KoreanPhraseExtractor.extractPhrases(superLongText).mkString(", ") ===
        "크리스마스 허니버터칩 정규직 크리스마스, 허니버터칩 정규직 크리스마스, 정규직 크리스마스, 크리스마스, 허니버터칩, 정규직")
    assert(time(KoreanPhraseExtractor.extractPhrases(superLongText)) < 1000)
  }

}
