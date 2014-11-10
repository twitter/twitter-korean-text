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

import com.twitter.penguin.korean.normalizer.KoreanNormalizer
import com.twitter.penguin.korean.stemmer.KoreanStemmer
import com.twitter.penguin.korean.stemmer.KoreanStemmer.StemmedTextWithTokens
import com.twitter.penguin.korean.tokenizer.KoreanTokenizer
import com.twitter.penguin.korean.tokenizer.KoreanTokenizer.KoreanToken

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
   * Wraps a text and the sequence of KoreanSegments
   *
   * @param text Input text.
   * @param segments Sequence of Korean Segments.
   */
  case class KoreanSegmentWithText(text: CharSequence, segments: Seq[KoreanSegment])

  /**
   * Normalize Korean text. Uses KoreanNormalizer.normalize().
   *
   * @param text Input text
   * @return Normalized Korean text
   */
  def normalize(text: CharSequence): CharSequence = KoreanNormalizer.normalize(text)

  /**
   * Wrapper for Korean stemmer
   *
   * @param text Input text
   * @return A sequence of stemmed tokens
   */
  def stem(text: CharSequence): StemmedTextWithTokens = {
    KoreanStemmer.stem(text)
  }

  /**
   * Tokenize text into a sequence of token strings.
   *
   * @param text input text
   * @param normalize option to enable the normalizer
   * @param stem option to enable the stemmer
   * @return A sequence of token strings.
   */
  def tokenizeToStrings(text: CharSequence, normalize: Boolean = true, stem: Boolean = true): Seq[String] = {
    tokenize(text, normalize, stem).map(_.text.toString)
  }

  /**
   * Tokenize text into a sequence of KoreanSegments, which includes start offset, the length,
   * and the full information of each token.
   *
   * This is useful for Lucene integration. Normalization is not supported for this feature.
   * For stemming support, use tokenizeWithIndexWithStemmer
   *
   * @param text Input text.
   * @return A sequence of KoreanSegments.
   */
  def tokenizeWithIndex(text: CharSequence): Seq[KoreanSegment] = {
    val tokens: Seq[KoreanToken] = tokenize(text, normalizization = false, stemming = false)
    getKoreanSegments(text, tokens)
  }

  /**
   * Tokenize text into a KoreanSegmentWithText,
   * which includes stemmed input text and a sequence stemmed tokens
   *
   * This is useful for Lucene integration. Normalization is not supported for this feature.
   *
   * @param text Input text.
   * @return KoreanSegmentWithText
   */
  def tokenizeWithIndexWithStemmer(text: CharSequence): KoreanSegmentWithText = {
    val stemmed: StemmedTextWithTokens = KoreanStemmer.stem(text)
    KoreanSegmentWithText(stemmed.text, getKoreanSegments(stemmed.text, stemmed.tokens))
  }

  /**
   * Tokenize text into a sequence of KoreanTokens, which includes part-of-speech information and
   * whether a token is an out-of-vocabulary term.
   *
   * @param text input text
   * @param normalizization option to enable the normalizer
   * @param stemming option to enable the stemmer
   * @return A sequence of KoreanTokens.
   */
  def tokenize(text: CharSequence, normalizization: Boolean = true, stemming: Boolean = true): Seq[KoreanToken] = {
    val normalized = if (normalizization) KoreanNormalizer.normalize(text) else text
    val tokenized = KoreanTokenizer.tokenize(normalized)
    if (stemming) KoreanStemmer.stemPredicates(tokenized).flatten else tokenized
  }

  /**
   * Align text with tokens to get indices (useful for Lucene integration)
   * @param text Input CharSequence
   * @param tokens A sequence of Tokens
   * @return  A sequence of KoreanSegments
   */
  private def getKoreanSegments(text: CharSequence, tokens: Seq[KoreanToken]): Seq[KoreanSegment] = {
    val s = text.toString
    // Match text with the tokenization results to get offset and length of each token
    val (output, i) = tokens.foldLeft(List[KoreanSegment](), 0) {
      case ((l: List[KoreanSegment], i: Int), token: KoreanToken) =>
        val segStart = s.indexOf(token.text, i)
        (KoreanSegment(segStart, token.text.length, token) :: l, segStart + token.text.length)
    }
    output.reverse
  }
}