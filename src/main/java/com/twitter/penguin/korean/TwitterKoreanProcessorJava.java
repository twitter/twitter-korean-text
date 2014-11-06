package com.twitter.penguin.korean;

import java.util.List;

import scala.collection.JavaConversions;
import scala.collection.Seq;

import com.twitter.penguin.korean.stemmer.KoreanStemmer;
import com.twitter.penguin.korean.tokenizer.KoreanTokenizer.KoreanToken;

/**
 * Java wrapper for TwitterKoreanProcessor using Builder pattern
 */
public class TwitterKoreanProcessorJava {
  private boolean stemmerEnabled;
  private boolean normalizerEnabled;

  private TwitterKoreanProcessorJava(boolean normalizerEnabled, boolean stemmerEnabled) {
    // Use the builder to instantiate this
    this.stemmerEnabled = stemmerEnabled;
    this.normalizerEnabled = normalizerEnabled;
  }

  /**
   * Normalize Korean text
   * 그랰ㅋㅋㅋㅋㅋㅋ -> 그래ㅋㅋ
   *
   * @param text Input text
   * @return Normalized text.
   */
  public CharSequence normalize(CharSequence text) {
    return TwitterKoreanProcessor.normalize(text);
  }

  /**
   * Stem Korean Verbs and Adjectives
   *
   * @param text
   * @return
   */
  public KoreanStemmer.StemmedTextWithTokens stem(CharSequence text) {
    return TwitterKoreanProcessor.stem(text);
  }

  /**
   *
   * @param text
   * @return
   */
  public List<KoreanToken> tokenize(CharSequence text) {
    Seq<KoreanToken> tokenized = TwitterKoreanProcessor.tokenize(
        text, normalizerEnabled, stemmerEnabled
    );
    return JavaConversions.seqAsJavaList(tokenized);
  }

  /**
   *
   * @param text
   * @return
   */
  public List<TwitterKoreanProcessor.KoreanSegment> tokenizeWithIndex(CharSequence text) {
    return JavaConversions.seqAsJavaList(
        TwitterKoreanProcessor.tokenizeWithIndex(text)
    );
  }

  /**
   *
   * @param text
   * @return
   */
  public TwitterKoreanProcessor.KoreanSegmentWithText tokenizeWithIndexWithStemmer(CharSequence text) {
    return TwitterKoreanProcessor.tokenizeWithIndexWithStemmer(text);
  }

  /**
   *
   */
  public static final class Builder{
    private boolean normalizerEnabled = true;
    private boolean stemmerEnabled = true;

    public Builder disableNormalizer() {
      normalizerEnabled = false;
      return this;
    }
    public Builder disableStemmer() {
      stemmerEnabled = false;
      return this;
    }

    public TwitterKoreanProcessorJava build(){
      return new TwitterKoreanProcessorJava(normalizerEnabled, stemmerEnabled);
    }
  }
}
