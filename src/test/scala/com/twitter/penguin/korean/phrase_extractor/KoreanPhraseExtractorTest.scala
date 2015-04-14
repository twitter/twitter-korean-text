package com.twitter.penguin.korean.phrase_extractor

import java.util.logging.Logger

import com.twitter.penguin.korean.TestBase._
import com.twitter.penguin.korean.tokenizer.KoreanTokenizer.KoreanToken
import com.twitter.penguin.korean.util.KoreanPos
import com.twitter.penguin.korean.{TestBase, TwitterKoreanProcessor}

class KoreanPhraseExtractorTest extends TestBase {
  val LOG = Logger.getLogger(getClass.getSimpleName)

  case class SampleTextPair(text: String, phrases: String)

  val sampleText = List[SampleTextPair](
    SampleTextPair(
      "블랙프라이데이: 이날 미국의 수백만 소비자들은 크리스마스 선물을 할인된 가격에 사는 것을 주 목적으로 블랙프라이데이 쇼핑을 한다.",
      "블랙프라이데이, 이날, 이날 미국, 이날 미국의 수백만, 미국의 수백만, 수백만, 이날 미국의 수백만 소비자들, " +
        "미국의 수백만 소비자들, 수백만 소비자들, 크리스마스, 크리스마스 선물, 할인, 할인된 가격, 가격, 주 목적, " +
        "블랙프라이데이 쇼핑, 미국, 소비자들, 선물, 목적, 쇼핑"
    ),
    SampleTextPair(
      "결정했어. 마키 코레썸 사주시는 분께는 허니버터칩 한 봉지를 선물할 것이다.",
      "결정, 마키, 마키 코레썸, 마키 코레썸 사주시는 분께는 허니버터칩, 코레썸 사주시는 분께는 허니버터칩, " +
        "허니버터칩, 마키 코레썸 사주시는 분께는 허니버터칩 한 봉지, 코레썸 사주시는 분께는 허니버터칩 한 봉지, " +
        "허니버터칩 한 봉지, 봉지, 코레썸"
    ),
    SampleTextPair(
      "[단독]정부, 새 고용 형태 ＇중규직＇ 만든다 http://url.com 이름도 바뀌겟군. 정규직은 상규직, " +
          "비정규직은 하규직. 중규직 참 창조적이다. 결국 기업은 비정규직으로 이용할게 뻔함.",
      "단독, 정부, 새 고용, 새 고용 형태, 고용 형태, 중규직, 이름, 정규직, 상규직, 비정규직, 하규직, 기업, 고용, 형태, 하규"
    ),
    SampleTextPair(
      "키? ...난 절대 키가 작은 게 아냐. 이소자키나 츠루기가 비정상적으로 큰거야. 1학년이 그렇게 큰 게 말이 돼!? ",
      "난 절대, 난 절대 키, 절대 키, 작은 게, 이소자키, 츠루기, 1학년, 절대, 이소, 자키, 학년"
    ),
    SampleTextPair(
      "Galaxy S5와 iPhone 6의 경쟁",
      "Galaxy, Galaxy S5, iPhone, iPhone 6의, iPhone 6의 경쟁, 6의 경쟁, S5, 경쟁"
    ),
    SampleTextPair(
      "ABCㅋㅋLTE갤럭시S4ㅋㅋ꼬마가",
      "ABC, LTE갤럭시S4, 꼬마, LTE, 갤럭시, S4"
    ),
    SampleTextPair(
      "아름다운 트위터 #해쉬태그 평화로운 트위터의 #hashtag @mention",
      "아름다운 트위터, 평화로운 트위터, 트위터, #해쉬태그, #hashtag"
    )
  )

  val spamText = "레알 시발 저거 카지노 포르노 야동 보다가 개빡쳤음"

  val superLongText: String = "허니버터칩정규직크리스마스" * 50

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
    assert(KoreanPhraseExtractor.collapsePos(
      Seq(
        KoreanToken("N", KoreanPos.Noun, 0, 1),
        KoreanToken("N", KoreanPos.Noun, 1, 1)
      )).mkString("/") ===
      "N(Noun: 0, 1)/N(Noun: 1, 1)"
    )

    assert(KoreanPhraseExtractor.collapsePos(
      Seq(
        KoreanToken("X", KoreanPos.KoreanParticle, 0, 1),
        KoreanToken("p", KoreanPos.NounPrefix, 1, 1),
        KoreanToken("N", KoreanPos.Noun, 2, 1)
      )).mkString("/") ===
      "X(KoreanParticle: 0, 1)/pN(Noun: 1, 2)"
    )

    assert(KoreanPhraseExtractor.collapsePos(
      Seq(
        KoreanToken("p", KoreanPos.NounPrefix, 0, 1),
        KoreanToken("X", KoreanPos.KoreanParticle, 1, 1),
        KoreanToken("N", KoreanPos.Noun, 2, 1)
      )).mkString("/") ===
      "p(Noun: 0, 1)/X(KoreanParticle: 1, 1)/N(Noun: 2, 1)"
    )

    assert(KoreanPhraseExtractor.collapsePos(
      Seq(
        KoreanToken("p", KoreanPos.NounPrefix, 0, 1),
        KoreanToken("N", KoreanPos.Noun, 1, 1),
        KoreanToken("X", KoreanPos.KoreanParticle, 2, 1)
      )).mkString("/") ===
      "pN(Noun: 0, 2)/X(KoreanParticle: 2, 1)"
    )

    assert(KoreanPhraseExtractor.collapsePos(tokenize(sampleText(0).text)).mkString("") ===
      "블랙프라이데이(Noun: 0, 7):(Punctuation: 7, 1) (Space: 8, 1)이날(Noun: 9, 2) (Space: 11, 1)" +
        "미국(Noun: 12, 2)의(Josa: 14, 1) (Space: 15, 1)수백만(Noun: 16, 3) (Space: 19, 1)" +
        "소비자들(Noun: 20, 4)은(Josa: 24, 1) (Space: 25, 1)크리스마스(Noun: 26, 5) (Space: 31, 1)" +
        "선물(Noun: 32, 2)을(Josa: 34, 1) (Space: 35, 1)할인(Noun: 36, 2)" +
        "된(Verb: 38, 1) (Space: 39, 1)가격(Noun: 40, 2)에(Josa: 42, 1) (Space: 43, 1)" +
        "사는(Verb: 44, 2) (Space: 46, 1)것(Noun: 47, 1)을(Josa: 48, 1) (Space: 49, 1)" +
        "주(Noun: 50, 1) (Space: 51, 1)목적(Noun: 52, 2)으로(Josa: 54, 2) (Space: 56, 1)" +
        "블랙프라이데이(Noun: 57, 7) (Space: 64, 1)쇼핑(Noun: 65, 2)을(Josa: 67, 1) (Space: 68, 1)" +
        "한다(Verb: 69, 2).(Punctuation: 71, 1)")

    assert(KoreanPhraseExtractor.collapsePos(tokenize(sampleText(1).text)).mkString("") ===
        "결정(Noun: 0, 2)했어(Verb: 2, 2).(Punctuation: 4, 1) (Space: 5, 1)" +
          "마키(Noun: 6, 2) (Space: 8, 1)코레썸(Noun: 9, 3) (Space: 12, 1)" +
          "사주시는(Verb: 13, 4) (Space: 17, 1)분께는(Verb: 18, 3) (Space: 21, 1)" +
          "허니버터칩(Noun: 22, 5) (Space: 27, 1)한(Verb: 28, 1) (Space: 29, 1)" +
          "봉지(Noun: 30, 2)를(Josa: 32, 1) (Space: 33, 1)선물할(Verb: 34, 3) (Space: 37, 1)" +
          "것(Noun: 38, 1)이다(Josa: 39, 2).(Punctuation: 41, 1)")
  }

  test("extractPhrases correctly extracts phrases") {
    assert(KoreanPhraseExtractor.extractPhrases(
      tokenize(sampleText(0).text), filterSpam = false
    ).mkString(", ") ===
        "블랙프라이데이(Noun: 0, 7), 이날(Noun: 9, 2), 이날 미국(Noun: 9, 5), 이날 미국의 수백만(Noun: 9, 10), " +
          "미국의 수백만(Noun: 12, 7), 수백만(Noun: 16, 3), 이날 미국의 수백만 소비자들(Noun: 9, 15), " +
          "미국의 수백만 소비자들(Noun: 12, 12), 수백만 소비자들(Noun: 16, 8), 크리스마스(Noun: 26, 5), " +
          "크리스마스 선물(Noun: 26, 8), 할인(Noun: 36, 2), 할인된 가격(Noun: 36, 6), 가격(Noun: 40, 2), " +
          "주 목적(Noun: 50, 4), 블랙프라이데이 쇼핑(Noun: 57, 10), " +
          "미국(Noun: 12, 2), 소비자들(Noun: 20, 4), 선물(Noun: 32, 2), 목적(Noun: 52, 2), " +
          "쇼핑(Noun: 65, 2)")
  }

  test("extractPhrases correctly extracts phrases from a string") {
    sampleText.foreach {
      case SampleTextPair(text: String, phrases: String) =>
        assert(KoreanPhraseExtractor.extractPhrases(text).mkString(", ") === phrases)
    }
  }

  test("extractPhrases should extract long noun-only phrases in reasonable time") {
    assert(KoreanPhraseExtractor.extractPhrases(superLongText).mkString(", ") === "허니버터칩, 정규직, 크리스마스")
    assert(time(KoreanPhraseExtractor.extractPhrases(superLongText)) < 10000)
  }

  test("extractPhrases should correctly extract the example set") {
    assertExamples(
      "current_phrases.txt", LOG,
      KoreanPhraseExtractor.extractPhrases(_).mkString("/")
    )
  }

  test("extractPhrases should filter out spam and profane words") {
    assert(KoreanPhraseExtractor.extractPhrases(spamText).mkString(", ") ===
        "레알, 레알 시발, 레알 시발 저거, 시발 저거, 레알 시발 저거 카지노, 시발 저거 카지노, 저거 카지노, " +
          "레알 시발 저거 카지노 포르노, 시발 저거 카지노 포르노, 저거 카지노 포르노, 카지노 포르노, " +
          "레알 시발 저거 카지노 포르노 야동, 시발 저거 카지노 포르노 야동, 저거 카지노 포르노 야동, " +
          "카지노 포르노 야동, 포르노 야동, 시발, 저거, 카지노, 포르노, 야동")
    assert(KoreanPhraseExtractor.extractPhrases(spamText, filterSpam = true).mkString(", ") ===
        "레알, 저거")
  }
}
