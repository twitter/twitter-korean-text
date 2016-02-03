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
import com.twitter.penguin.korean.tokenizer.KoreanTokenizer.KoreanToken
import com.twitter.penguin.korean.tokenizer._
import com.twitter.penguin.korean.util.{KoreanDictionaryProvider, KoreanPos}

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
   * Tokenize text into a sequence of KoreanTokens, which includes part-of-speech information and
   * whether a token is an out-of-vocabulary term.
   *
   * @param text input text
   * @return A sequence of KoreanTokens.
   */
  def tokenize(text: CharSequence): Seq[KoreanToken] = KoreanTokenizer.tokenize(text)

  /**
   * Tokenize text (with a custom profile) into a sequence of KoreanTokens,
   * which includes part-of-speech information and whether a token is an out-of-vocabulary term.
   *
   * @param text input text
   * @return A sequence of KoreanTokens.
   */
  def tokenize(
      text: CharSequence,
      profile: TokenizerProfile
  ): Seq[KoreanToken] = {
    KoreanTokenizer.tokenize(text, profile)
  }

  /**
   * Add user-defined word list to the noun dictionary. Spaced words are not allowed.
   *
   * @param words Sequence of words to add.
   */
  def addNounsToDictionary(words: Seq[String]) {
    KoreanDictionaryProvider.addWordsToDictionary(KoreanPos.Noun, words)
  }

  /**
   * Wrapper for Korean stemmer
   *
   * @param tokens Korean tokens
   * @return A sequence of stemmed tokens
   */
  def stem(tokens: Seq[KoreanToken]): Seq[KoreanToken] = KoreanStemmer.stem(tokens)

  /**
   * Tokenize text into a sequence of token strings. This excludes spaces.
   *
   * @param tokens Korean tokens
   * @return A sequence of token strings.
   */
  def tokensToStrings(tokens: Seq[KoreanToken]): Seq[String] = {
    tokens.filterNot(t => t.pos == KoreanPos.Space).map(_.text.toString)
  }

  /**
   * Split input text into sentences.
   *
   * @param text input text
   * @return A sequence of sentences.
   */
  def splitSentences(text: CharSequence): Seq[Sentence] = {
    KoreanSentenceSplitter.split(text)
  }

  /**
   * Extract noun-phrases from Korean text
   *
   * @param tokens         Korean tokens
   * @param filterSpam     true if spam/slang terms to be filtered out (default: false)
   * @param enableHashtags true if #hashtags to be included (default: true)
   * @return A sequence of extracted phrases
   */
  def extractPhrases(tokens: Seq[KoreanToken],
      filterSpam: Boolean = false,
      enableHashtags: Boolean = true): Seq[KoreanPhrase] = {
    KoreanPhraseExtractor.extractPhrases(tokens, filterSpam, enableHashtags)
  }

  /**
   * Detokenize the input list of words.
   *
   * @param tokens List of words.
   * @return Detokenized string.
   */
  def detokenize(tokens: Iterable[String]): String = {
    KoreanDetokenizer.detokenize(tokens)
  }
}