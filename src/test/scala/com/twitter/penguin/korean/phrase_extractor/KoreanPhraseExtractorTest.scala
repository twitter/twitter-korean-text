package com.twitter.penguin.korean.phrase_extractor

import java.util.logging.Logger

import com.twitter.penguin.korean.TestBase
import com.twitter.penguin.korean.TestBase._
import com.twitter.penguin.korean.TwitterKoreanProcessor.tokenize
import com.twitter.penguin.korean.normalizer.KoreanNormalizer
import com.twitter.penguin.korean.tokenizer.KoreanTokenizer.KoreanToken
import com.twitter.penguin.korean.util.KoreanPos

class KoreanPhraseExtractorTest extends TestBase {
  val LOG = Logger.getLogger(getClass.getSimpleName)

  case class SampleTextPair(text: String, phrases: String)

  val sampleText = List[SampleTextPair](
    SampleTextPair(
      "블랙프라이데이: 이날 미국의 수백만 소비자들은 크리스마스 선물을 할인된 가격에 사는 것을 주 목적으로 블랙프라이데이 쇼핑을 한다.",
      "블랙프라이데이(Noun: 0, 7), 이날(Noun: 9, 2), 이날 미국(Noun: 9, 5), 이날 미국의 수백만(Noun: 9, 10), " +
          "미국의 수백만(Noun: 12, 7), 수백만(Noun: 16, 3), 이날 미국의 수백만 소비자들(Noun: 9, 15), " +
          "미국의 수백만 소비자들(Noun: 12, 12), 수백만 소비자들(Noun: 16, 8), 크리스마스(Noun: 26, 5), " +
          "크리스마스 선물(Noun: 26, 8), 할인(Noun: 36, 2), 할인된 가격(Noun: 36, 6), 가격(Noun: 40, 2), " +
          "주 목적(Noun: 50, 4), 블랙프라이데이 쇼핑(Noun: 57, 10), 미국(Noun: 12, 2), 소비자들(Noun: 20, 4), " +
          "선물(Noun: 32, 2), 목적(Noun: 52, 2), 쇼핑(Noun: 65, 2)"
    ),
    SampleTextPair(
      "결정했어. 마키 코레썸 사주시는 분께는 허니버터칩 한 봉지를 선물할 것이다.",
      "결정(Noun: 0, 2), 마키(Noun: 6, 2), 마키 코레썸(Noun: 6, 6), " +
          "마키 코레썸 사주시는 분께는 허니버터칩(Noun: 6, 21), 코레썸 사주시는 분께는 허니버터칩(Noun: 9, 18), " +
          "허니버터칩(Noun: 22, 5), 마키 코레썸 사주시는 분께는 허니버터칩 한 봉지(Noun: 6, 26), " +
          "코레썸 사주시는 분께는 허니버터칩 한 봉지(Noun: 9, 23), 허니버터칩 한 봉지(Noun: 22, 10), " +
          "봉지(Noun: 30, 2), 코레썸(Noun: 9, 3)"
    ),
    SampleTextPair(
      "[단독]정부, 새 고용 형태 ＇중규직＇ 만든다 http://url.com 이름도 바뀌겟군. 정규직은 상규직, " +
          "비정규직은 하규직. 중규직 참 창조적이다. 결국 기업은 비정규직으로 이용할게 뻔함.",
      "단독(Noun: 1, 2), 정부(Noun: 4, 2), 새 고용(Noun: 8, 4), 새 고용 형태(Noun: 8, 7), " +
          "고용 형태(Noun: 10, 5), 중규직(Noun: 17, 3), 이름(Noun: 41, 2), 정규직(Noun: 51, 3), " +
          "상규직(Noun: 56, 3), 비정규직(Noun: 61, 4), 하규직(Noun: 67, 3), 기업(Noun: 88, 2), " +
          "고용(Noun: 10, 2), 형태(Noun: 13, 2), 하규(Noun: 67, 2)"
    ),
    SampleTextPair(
      "키? ...난 절대 키가 작은 게 아냐. 이소자키나 츠루기가 비정상적으로 큰거야. 1학년이 그렇게 큰 게 말이 돼!? ",
      "난 절대(Noun: 6, 4), 난 절대 키(Noun: 6, 6), 절대 키(Noun: 8, 4), 작은 게(Noun: 14, 4), " +
          "이소자키(Noun: 23, 4), 츠루기(Noun: 29, 3), 1학년(Noun: 46, 3), 절대(Noun: 8, 2), " +
          "이소(Noun: 23, 2), 자키(Noun: 25, 2), 학년(Noun: 47, 2)"
    ),
    SampleTextPair(
      "Galaxy S5와 iPhone 6의 경쟁",
      "Galaxy(Noun: 0, 6), Galaxy S5(Noun: 0, 9), iPhone(Noun: 11, 6), " +
          "iPhone 6의(Noun: 11, 9), iPhone 6의 경쟁(Noun: 11, 12), 6의 경쟁(Noun: 18, 5), " +
          "S5(Noun: 7, 2), 경쟁(Noun: 21, 2)"
    ),
    SampleTextPair(
      "ABCㅋㅋLTE갤럭시S4ㅋㅋ꼬마가",
      "ABC(Noun: 0, 3), LTE갤럭시S4(Noun: 5, 8), 꼬마(Noun: 15, 2), LTE(Noun: 5, 3), " +
          "갤럭시(Noun: 8, 3), S4(Noun: 11, 2)"
    ),
    SampleTextPair(
      "아름다운 트위터 #해쉬태그 평화로운 트위터의 #hashtag @mention",
      "아름다운 트위터(Noun: 0, 8), 평화로운 트위터(Noun: 15, 8), 트위터(Noun: 5, 3), " +
          "#해쉬태그(Hashtag: 9, 5), #hashtag(Hashtag: 25, 8)"
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
            "미국(ProperNoun: 12, 2)의(Josa: 14, 1) (Space: 15, 1)수백만(Noun: 16, 3) (Space: 19, 1)" +
            "소비자들(Noun: 20, 4)은(Josa: 24, 1) (Space: 25, 1)크리스마스(Noun: 26, 5) (Space: 31, 1)" +
            "선물(Noun: 32, 2)을(Josa: 34, 1) (Space: 35, 1)할인(Noun: 36, 2)" +
            "된(Verb: 38, 1) (Space: 39, 1)가격(Noun: 40, 2)에(Josa: 42, 1) (Space: 43, 1)" +
            "사는(Verb: 44, 2) (Space: 46, 1)것(Noun: 47, 1)을(Josa: 48, 1) (Space: 49, 1)" +
            "주(Noun: 50, 1) (Space: 51, 1)목적(Noun: 52, 2)으로(Josa: 54, 2) (Space: 56, 1)" +
            "블랙프라이데이(Noun: 57, 7) (Space: 64, 1)쇼핑(Noun: 65, 2)을(Josa: 67, 1) (Space: 68, 1)" +
            "한다(Verb: 69, 2).(Punctuation: 71, 1)")

    assert(KoreanPhraseExtractor.collapsePos(tokenize(sampleText(1).text)).mkString("") ===
        "결정(Noun: 0, 2)했어(Verb: 2, 2).(Punctuation: 4, 1) (Space: 5, 1)" +
            "마키(Noun: 6, 2) (Space: 8, 1)코레썸(ProperNoun: 9, 3) (Space: 12, 1)" +
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

  test("extractPhrases should not deduplicate phrases") {
    assert(KoreanPhraseExtractor.extractPhrases(
      tokenize("성탄절 쇼핑 성탄절 쇼핑 성탄절 쇼핑 성탄절 쇼핑"), filterSpam = false
    ).mkString(", ") === "성탄절(Noun: 0, 3), 성탄절 쇼핑(Noun: 0, 6), 성탄절 쇼핑 성탄절(Noun: 0, 10), " +
        "쇼핑 성탄절(Noun: 4, 6), 성탄절 쇼핑 성탄절 쇼핑(Noun: 0, 13), 쇼핑 성탄절 쇼핑(Noun: 4, 9), " +
        "성탄절 쇼핑 성탄절 쇼핑 성탄절(Noun: 0, 17), 쇼핑 성탄절 쇼핑 성탄절(Noun: 4, 13), 성탄절 쇼핑 성탄절 " +
        "쇼핑 성탄절 쇼핑(Noun: 0, 20), 쇼핑 성탄절 쇼핑 성탄절 쇼핑(Noun: 4, 16), 성탄절 쇼핑 성탄절 쇼핑 " +
        "성탄절 쇼핑 성탄절(Noun: 0, 24), 쇼핑 성탄절 쇼핑 성탄절 쇼핑 성탄절(Noun: 4, 20), 성탄절 쇼핑 성탄절 " +
        "쇼핑 성탄절 쇼핑 성탄절 쇼핑(Noun: 0, 27), 쇼핑 성탄절 쇼핑 성탄절 쇼핑 성탄절 쇼핑(Noun: 4, 23), " +
        "쇼핑(Noun: 4, 2)")
  }

  test("extractPhrases correctly extracts phrases from a string") {
    sampleText.foreach {
      case SampleTextPair(text: String, phrases: String) =>
        assertExtraction(text, phrases)
    }
  }

  test("extractPhrases should extract long noun-only phrases in reasonable time") {
    assertExtraction(superLongText, "허니버터칩(Noun: 0, 5), 정규직(Noun: 5, 3), 크리스마스(Noun: 8, 5)")

    val tokens = tokenize(superLongText)
    assert(time(KoreanPhraseExtractor.extractPhrases(tokens)) < 10000)
  }

  test("extractPhrases should correctly extract the example set") {
    def phraseExtractor(text: String) = {
      val normalized = KoreanNormalizer.normalize(text)
      val tokens = tokenize(normalized)
      KoreanPhraseExtractor.extractPhrases(tokens).mkString("/")
    }
    assertExamples(
      "current_phrases.txt", LOG,
      phraseExtractor
    )
  }

  test("extractPhrases should filter out spam and profane words") {
    assertExtraction(spamText,
      "레알(Noun: 0, 2), 레알 시발(Noun: 0, 5), 레알 시발 저거(Noun: 0, 8), 시발 저거(Noun: 3, 5), " +
          "레알 시발 저거 카지노(Noun: 0, 12), 시발 저거 카지노(Noun: 3, 9), 저거 카지노(Noun: 6, 6), " +
          "레알 시발 저거 카지노 포르노(Noun: 0, 16), 시발 저거 카지노 포르노(Noun: 3, 13), " +
          "저거 카지노 포르노(Noun: 6, 10), 카지노 포르노(Noun: 9, 7), " +
          "레알 시발 저거 카지노 포르노 야동(Noun: 0, 19), 시발 저거 카지노 포르노 야동(Noun: 3, 16), " +
          "저거 카지노 포르노 야동(Noun: 6, 13), 카지노 포르노 야동(Noun: 9, 10), 포르노 야동(Noun: 13, 6), " +
          "시발(Noun: 3, 2), 저거(Noun: 6, 2), 카지노(Noun: 9, 3), 포르노(Noun: 13, 3), 야동(Noun: 17, 2)")


    assert(
      KoreanPhraseExtractor.extractPhrases(tokenize(spamText), filterSpam = true).mkString(", ") ===
          "레알(Noun: 0, 2), 저거(Noun: 6, 2)")
  }

  test("extractPhrases should detect numbers with special chars") {
    assertExtraction("트위터 25.2% 상승.",
      "트위터(Noun: 0, 3), 트위터 25.2%(Noun: 0, 9), 트위터 25.2% 상승(Noun: 0, 12), " +
          "25.2% 상승(Noun: 4, 8), 25.2%(Noun: 4, 5), 상승(Noun: 10, 2)")

    assertExtraction("짜장면 3400원.", "짜장면(Noun: 0, 3), 짜장면 3400원(Noun: 0, 9), 3400원(Noun: 4, 5)")

    assertExtraction("떡볶이 3,444,231원 + 400원.",
      "떡볶이(Noun: 0, 3), 떡볶이 3,444,231원(Noun: 0, 14), 400원(Noun: 17, 4), 3,444,231원(Noun: 4, 10)")

    assertExtraction("트위터 $200으로 상승",
      "트위터(Noun: 0, 3), 트위터 $200(Noun: 0, 8), 상승(Noun: 11, 2), $200(Noun: 4, 4)")

    assertExtraction("1,200.34원. 1,200.34엔. 1,200.34옌. 1,200.34위안.",
      "1,200.34원(Noun: 0, 9), 1,200.34엔(Noun: 11, 9), 1,200.34옌(Noun: 22, 9), 1,200.34위안(Noun: 33, 10)")

    assertExtraction("200달러 3위 3000유로",
      "200달러(Noun: 0, 5), 200달러 3위(Noun: 0, 8), 200달러 3위 3000유로(Noun: 0, 15), " +
          "3위 3000유로(Noun: 6, 9), 3000유로(Noun: 9, 6)")
  }

  def assertExtraction(s: String, expected: String): Unit = {
    val tokens = tokenize(s)
    assert(KoreanPhraseExtractor.extractPhrases(tokens).mkString(", ") ===
        expected)
  }
}
