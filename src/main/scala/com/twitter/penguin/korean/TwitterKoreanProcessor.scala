package com.twitter.penguin.korean

import com.twitter.penguin.korean.normalizer.KoreanNormalizer
import com.twitter.penguin.korean.tokenizer.KoreanTokenizer
import com.twitter.penguin.korean.tokenizer.KoreanTokenizer._

/**
 * TwitterKoreanTokenizer provides error and slang tolerant Korean tokenization.
 */
object TwitterKoreanProcessor {

  /**
   * Korean Segment includes each token's start offset, length, and the token itself.
   *
   * @param start Offset of the token
   * @param length Length of the token
   * @param token Token
   */
  case class KoreanSegment(start: Int, length: Int, token: KoreanToken)

  /**
   * Normalize Korean text. Uses KoreanNormalizer.normalize().
   *
   * @param text Input text
   * @return Normalized Korean text
   */
  def normalize(text: CharSequence): CharSequence = KoreanNormalizer.normalize(text)

  /**
   * Tokenize text into a sequence of token strings.
   *
   * @param text Input text, which can include any characters or spaces.
   * @return A sequence of token strings.
   */
  def tokenizeToStrings(text: CharSequence): Seq[String] = {
    tokenize(text).map(_.text.toString)
  }

  /**
   * Tokenize text into a sequence of normalized token strings.
   *
   * @param text Input text, which can include any characters or spaces.
   * @return A sequence of normalized token strings.
   */
  def tokenizeToNormalizedStrings(text: CharSequence): Seq[String] = {
    tokenizeWithNormalization(text).map(_.text.toString)
  }

  /**
   * Tokenize text into a sequence of KoreanTokens, which includes part-of-speech information and
   * whether a token is an out-of-vocabulary term.
   *
   * @param text Input text.
   * @return A sequence of KoreanTokens.
   */
  def tokenize(text: CharSequence): Seq[KoreanToken] = {
    KoreanTokenizer.tokenize(text)
  }

  /**
   * Apply normalization before tokenization.
   *
   * @param text Input text.
   * @return A sequence of normalized KoreanTokens.
   */
  def tokenizeWithNormalization(text: CharSequence): Seq[KoreanToken] = {
    tokenize(normalize(text))
  }

  /**
   * Tokenize text into a sequence of KoreanSegments, which includes start offset, the length,
   * and the full information of each token.
   *
   * This is useful for Lucene integration.
   *
   * @param text Input text.
   * @return A sequence of KoreanSegments.
   */
  def tokenizeWithIndex(text: CharSequence): Seq[KoreanSegment] = {
    val tokens: Seq[KoreanToken] = tokenize(text)

    val s: String = text.toString
    // Match text with the tokenization results to get offset and length of each token
    val (output, i) = tokens.foldLeft(List[KoreanSegment](), 0) {
      case ((l: List[KoreanSegment], i: Int), token: KoreanToken) =>
        val segStart = s.indexOf(token.text, i)
        (KoreanSegment(segStart, token.text.length, token) :: l, segStart + token.text.length)
    }
    output.reverse
  }
}