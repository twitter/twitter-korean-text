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

package com.twitter.penguin.korean;

import java.util.List;

import scala.collection.JavaConversions;
import scala.collection.Seq;

import com.twitter.penguin.korean.TwitterKoreanProcessor.KoreanSegment;
import com.twitter.penguin.korean.TwitterKoreanProcessor.KoreanSegmentWithText;
import com.twitter.penguin.korean.stemmer.KoreanStemmer;
import com.twitter.penguin.korean.tokenizer.KoreanTokenizer.KoreanToken;

/**
 * Java wrapper for TwitterKoreanProcessor using Builder pattern
 */
public class TwitterKoreanProcessorJava {
  private boolean stemmerEnabled;
  private boolean normalizerEnabled;
  private boolean keepSpaceEnabled;

  private TwitterKoreanProcessorJava(boolean normalizerEnabled, boolean stemmerEnabled, boolean keepSpaceEnabled) {
    // Use the builder to instantiate this
    this.stemmerEnabled = stemmerEnabled;
    this.normalizerEnabled = normalizerEnabled;
    this.keepSpaceEnabled = keepSpaceEnabled;
  }

  /**
   * Normalize Korean text
   * 그랰ㅋㅋㅋㅋㅋㅋ -> 그래ㅋㅋ
   *
   * @param text Input text.
   * @return Normalized text.
   */
  public CharSequence normalize(CharSequence text) {
    return TwitterKoreanProcessor.normalize(text);
  }

  /**
   * Stem Korean Verbs and Adjectives
   *
   * @param text Input text.
   * @return StemmedTextWithTokens(text, tokens)
   */
  public KoreanStemmer.StemmedTextWithTokens stem(CharSequence text) {
    return TwitterKoreanProcessor.stem(text);
  }

  /**
   * Tokenize with the builder options.
   *
   * @param text Input text.
   * @return A list of Korean Tokens
   */
  public List<KoreanToken> tokenize(CharSequence text) {
    Seq<KoreanToken> tokenized = TwitterKoreanProcessor.tokenize(
        text, normalizerEnabled, stemmerEnabled, keepSpaceEnabled
    );
    return JavaConversions.seqAsJavaList(tokenized);
  }

  /**
   * Tokenize with the builder options into a String Iterable.
   *
   * @param text Input text.
   * @return A list of token strings.
   */
  public List<String> tokenizeToStrings(CharSequence text) {
    Seq<String> tokenized = TwitterKoreanProcessor.tokenizeToStrings(
        text, normalizerEnabled, stemmerEnabled, keepSpaceEnabled
    );
    return JavaConversions.seqAsJavaList(tokenized);
  }

  /**
   * Tokenize into KoreanSegments, which includes the indices
   *
   * @param text Input text.
   * @return A list of KoreanSegments.
   */
  public List<KoreanSegment> tokenizeWithIndex(CharSequence text) {
    return JavaConversions.seqAsJavaList(
        TwitterKoreanProcessor.tokenizeWithIndex(text)
    );
  }

  /**
   * Tokenize into KoreanSegmentWithText, which includes
   * the stemmed text and the KoreanSegments
   *
   * @param text Input text.
   * @return KoreanSegmentWithText(text, KoreanSegments)
   */
  public KoreanSegmentWithText tokenizeWithIndexWithStemmer(CharSequence text) {
    return TwitterKoreanProcessor.tokenizeWithIndexWithStemmer(text);
  }

  /**
   * Extract phrases from Korean input text
   *
   * @param text Input text.
   * @return List of phrase CharSequences.
   */
  public List<CharSequence> extractPhrases(CharSequence text) {
    return JavaConversions.seqAsJavaList(
        TwitterKoreanProcessor.extractPhrases(text)
    );
  }

  /**
   * Builder for TwitterKoreanProcessorJava
   */
  public static final class Builder {
    private boolean normalizerEnabled = true;
    private boolean stemmerEnabled = true;
    private boolean keepSpaceEnabled = false;

    public Builder disableNormalizer() {
      normalizerEnabled = false;
      return this;
    }

    public Builder disableStemmer() {
      stemmerEnabled = false;
      return this;
    }

    public Builder enableKeepSpace() {
      keepSpaceEnabled = true;
      return this;
    }

    public TwitterKoreanProcessorJava build() {
      return new TwitterKoreanProcessorJava(normalizerEnabled, stemmerEnabled, keepSpaceEnabled);
    }
  }
}
