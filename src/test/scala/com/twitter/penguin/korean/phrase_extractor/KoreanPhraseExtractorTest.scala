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

  def tokenize(text: String) = TwitterKoreanProcessor.tokenize(
    text, stemming = false, keepSpace = true
  )

  test("collapsePos correctly collapse KoreanPos sequences") {
    assert(KoreanPhraseExtractor.collapsePos(tokenize(text1)).mkString("") ===
        "블랙프라이데이Noun:Punctuation 이날 Noun미국Noun의 Josa수백만 Noun소비자들Noun은 " +
            "Josa크리스마스 Noun선물Noun을 Josa할인Noun된 Verb가격Noun에 Josa사는 Verb것Noun을 " +
            "Josa주 Noun목적Noun으로 Josa블랙프라이데이 Noun쇼핑Noun을 Josa한다Verb.Punctuation")

    assert(KoreanPhraseExtractor.collapsePos(tokenize(text2)).mkString("") ===
        "결정Noun했어Verb.Punctuation 마키 Noun코레썸 Noun사주시는 Verb분께는 Verb허니버터칩 Noun" +
            "한 봉지Noun를 Josa선물할 Verb것Noun이다Josa.Punctuation")
  }

  test("extractPhrases correctly extracts phrases") {
    assert(KoreanPhraseExtractor.extractPhrases(tokenize(text1)).mkString(", ") ===
        "블랙프라이데이Noun, 이날 미국의 수백만 소비자들Noun, 수백만 소비자들Noun, 크리스마스 선물Noun, " +
            "할인된 가격Noun, 주 목적Noun, 블랙프라이데이 쇼핑Noun, 수백만Noun, 소비자들Noun, 크리스마스Noun")
  }

  test("extractPhrases correctly extracts phrases from a string") {
    assert(KoreanPhraseExtractor.extractPhrases(text1).mkString(", ") ===
        "블랙프라이데이, 이날 미국의 수백만 소비자들, 수백만 소비자들, " +
            "크리스마스 선물, 할인된 가격, 주 목적, 블랙프라이데이 쇼핑, 수백만, 소비자들, 크리스마스")
    assert(KoreanPhraseExtractor.extractPhrases(text2).mkString(", ") ===
        "마키 코레썸 사주시는 분께는 허니버터칩 한 봉지, 허니버터칩 한 봉지, 코레썸, 허니버터칩, 한 봉지")
    assert(KoreanPhraseExtractor.extractPhrases(text3).mkString(", ") ===
        "새 고용 형태, 중규직, 정규직, 상규직, 비정규직, 하규직, 창조적")
  }

  test("extractPhrases should extract long noun-only phrases in reasonable time") {
    assert(KoreanPhraseExtractor.extractPhrases("허니버터칩정규직크리스마스" * 50).mkString(", ") ===
        "")
  }

}
