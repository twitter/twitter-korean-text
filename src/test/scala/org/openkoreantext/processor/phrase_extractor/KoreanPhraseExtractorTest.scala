package org.openkoreantext.processor.phrase_extractor

import java.util.logging.Logger

import org.openkoreantext.processor.TestBase
import org.openkoreantext.processor.TestBase._
import org.openkoreantext.processor.normalizer.KoreanNormalizer
import org.openkoreantext.processor.tokenizer.KoreanTokenizer.KoreanToken
import org.openkoreantext.processor.util.KoreanPos
import org.openkoreantext.processor.OpenKoreanTextProcessor._
import org.openkoreantext.processor.phrase_extractor.KoreanPhraseExtractor.KoreanPhrase

class KoreanPhraseExtractorTest extends TestBase {
  val LOG = Logger.getLogger(getClass.getSimpleName)

  case class SampleTextPair(text: String, phrases: String)

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
        KoreanToken("m", KoreanPos.Modifier, 1, 1),
        KoreanToken("N", KoreanPos.Noun, 2, 1)
      )).mkString("/") ===
        "X(KoreanParticle: 0, 1)/mN(Noun: 1, 2)"
    )

    assert(KoreanPhraseExtractor.collapsePos(
      Seq(
        KoreanToken("m", KoreanPos.Modifier, 0, 1),
        KoreanToken("X", KoreanPos.KoreanParticle, 1, 1),
        KoreanToken("N", KoreanPos.Noun, 2, 1)
      )).mkString("/") ===
        "m(Noun: 0, 1)/X(KoreanParticle: 1, 1)/N(Noun: 2, 1)"
    )

    assert(KoreanPhraseExtractor.collapsePos(
      Seq(
        KoreanToken("m", KoreanPos.Modifier, 0, 1),
        KoreanToken("N", KoreanPos.Noun, 1, 1),
        KoreanToken("X", KoreanPos.KoreanParticle, 2, 1)
      )).mkString("/") ===
        "mN(Noun: 0, 2)/X(KoreanParticle: 2, 1)"
    )
  }

  test("extractPhrases should not deduplicate phrases") {
    val phrases = KoreanPhraseExtractor.extractPhrases(
      tokenize("성탄절 쇼핑 성탄절 쇼핑 성탄절 쇼핑 성탄절 쇼핑"), filterSpam = false
    )
    assert(phrases.map{ p: KoreanPhrase => p.text } === phrases.map{ p: KoreanPhrase => p.text }.distinct)
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
    assert(KoreanPhraseExtractor.extractPhrases(tokenize(spamText), filterSpam = false).size > 5)
    assert(
      KoreanPhraseExtractor.extractPhrases(tokenize(spamText), filterSpam = true).mkString(", ") ===
          "레알(Noun: 0, 2), 저거(Noun: 6, 2)")
  }

  test("extractPhrases should detect numbers with special chars") {
    assertExtraction("트위터 25.2% 상승.",
      "트위터(Noun: 0, 3), 트위터 25.2%(Noun: 0, 9), 트위터 25.2% 상승(Noun: 0, 12), 25.2%(Noun: 4, 5), 상승(Noun: 10, 2)")

    assertExtraction("짜장면 3400원.", "짜장면(Noun: 0, 3), 짜장면 3400원(Noun: 0, 9), 3400원(Noun: 4, 5)")

    assertExtraction("떡볶이 3,444,231원 + 400원.",
      "떡볶이(Noun: 0, 3), 떡볶이 3,444,231원(Noun: 0, 14), 400원(Noun: 17, 4), 3,444,231원(Noun: 4, 10)")

    assertExtraction("트위터 $200으로 상승",
      "트위터(Noun: 0, 3), 트위터 $200(Noun: 0, 8), 상승(Noun: 11, 2), $200(Noun: 4, 4)")

    assertExtraction("1,200.34원. 1,200.34엔. 1,200.34옌. 1,200.34위안.",
      "1,200.34원(Noun: 0, 9), 1,200.34엔(Noun: 11, 9), 1,200.34옌(Noun: 22, 9), 1,200.34위안(Noun: 33, 10)")

    assertExtraction("200달러 3위 3000유로",
      "200달러(Noun: 0, 5), 200달러 3위(Noun: 0, 8), 200달러 3위 3000유로(Noun: 0, 15), 3000유로(Noun: 9, 6)")
  }

  def assertExtraction(s: String, expected: String): Unit = {
    val tokens = tokenize(s)
    assert(KoreanPhraseExtractor.extractPhrases(tokens).mkString(", ") ===
        expected)
  }
}
