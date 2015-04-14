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
import com.twitter.penguin.korean.phrase_extractor.KoreanPhraseExtractor
import com.twitter.penguin.korean.phrase_extractor.KoreanPhraseExtractor.KoreanPhrase
import com.twitter.penguin.korean.stemmer.KoreanStemmer
import com.twitter.penguin.korean.tokenizer.KoreanTokenizer
import com.twitter.penguin.korean.tokenizer.KoreanTokenizer.KoreanToken

/**
 * TwitterKoreanTokenizer provides error and slang tolerant Korean tokenization.
 */
object TwitterKoreanProcessor {
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
  def stem(text: CharSequence): CharSequence = KoreanStemmer.stem(text)


  /**
   * Tokenize text into a sequence of token strings.
   *
   * @param text input text
   * @param normalize option to enable the normalizer
   * @param stem option to enable the stemmer
   * @return A sequence of token strings.
   */
  def tokenizeToStrings(text: CharSequence, normalize: Boolean = true, stem: Boolean = true, keepSpace: Boolean = false): Seq[String] = {
    tokenize(text, normalize, stem, keepSpace).map(_.text.toString)
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
  def tokenize(text: CharSequence,
               normalizization: Boolean = true,
               stemming: Boolean = true,
               keepSpace: Boolean = false): Seq[KoreanToken] = {
    val normalized = if (normalizization) KoreanNormalizer.normalize(text) else text
    val tokenized = KoreanTokenizer.tokenize(normalized, keepSpace)
    if (stemming) KoreanStemmer.stemPredicates(tokenized) else tokenized
  }

  /**
   * Extract noun-phrases from Korean text
   *
   * @param text input text.
   * @param filterSpam Whether to filter spam/slang terms
   * @return A sequence of extracted phrases
   */
  def extractPhrases(text: CharSequence,
                     filterSpam: Boolean = false,
                     enableHashtags: Boolean = true): Seq[KoreanPhrase] = {
    KoreanPhraseExtractor.extractPhrases(text, filterSpam, enableHashtags)
  }

}