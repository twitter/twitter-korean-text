package com.twitter.penguin.korean;

import org.junit.Test;

import com.twitter.penguin.korean.stemmer.KoreanStemmer;

import static org.junit.Assert.assertEquals;

public class TwitterKoreanProcessorJavaTest {
  TwitterKoreanProcessorJava processor = new TwitterKoreanProcessorJava.Builder().build();
  TwitterKoreanProcessorJava processorWithSpace = new TwitterKoreanProcessorJava.Builder().enableKeepSpace().build();

  TwitterKoreanProcessorJava processorNormalization = new TwitterKoreanProcessorJava.Builder()
      .disableStemmer()
      .build();
  TwitterKoreanProcessorJava processorStemming = new TwitterKoreanProcessorJava.Builder()
      .disableNormalizer()
      .build();
  TwitterKoreanProcessorJava processorNeither = new TwitterKoreanProcessorJava.Builder()
      .disableStemmer()
      .disableNormalizer()
      .build();

  TwitterKoreanProcessorJava processorWithSpamFilter = new TwitterKoreanProcessorJava.Builder()
      .enablePhraseExtractorSpamFilter()
      .build();

  TwitterKoreanProcessorJava processorWithoutHashtags = new TwitterKoreanProcessorJava.Builder()
      .disablePhraseExtractorHashtags()
      .build();

  @Test
  public void testNormalize() throws Exception {
    assertEquals("힘들겠습니다 그래요ㅋㅋ", processor.normalize("힘들겟씀다 그래욬ㅋㅋㅋ"));
  }

  @Test
  public void testStem() throws Exception {
    CharSequence stemmed = processor.stem("아름다운 강산을 귀여워서 먹었다.");

    assertEquals("아름답다 강산을 귀엽다 먹다.", stemmed.toString());
  }

  @Test
  public void testTokenize() throws Exception {
    String text = "이런 생각을 하는 게 정말로 말이 되닠ㅋㅋㅋㅋㅋ";
    assertEquals(
        "[이렇다(Adjective: 0, 2), 생각(Noun: 3, 2), 을(Josa: 5, 1), 하다(Verb: 7, 2), " +
            "게(Noun: 10, 1), 정말로(Adverb: 12, 3), 말(Noun: 16, 1), 이(Josa: 17, 1), " +
            "되다(Verb: 19, 2), ㅋㅋ(KoreanParticle: 21, 2)]",
        processor.tokenize(text).toString()
    );
    assertEquals(
        "[이렇다(Adjective: 0, 2),  (Space: 2, 1), 생각(Noun: 3, 2), 을(Josa: 5, 1),  (Space: 6, 1), " +
            "하다(Verb: 7, 2),  (Space: 9, 1), 게(Noun: 10, 1),  (Space: 11, 1), " +
            "정말로(Adverb: 12, 3),  (Space: 15, 1), 말(Noun: 16, 1), 이(Josa: 17, 1),  (Space: 18, 1), " +
            "되다(Verb: 19, 2), ㅋㅋ(KoreanParticle: 21, 2)]",
        processorWithSpace.tokenize(text).toString()
    );
    assertEquals(
        "[이런(Adjective: 0, 2), 생각(Noun: 3, 2), 을(Josa: 5, 1), 하는(Verb: 7, 2), 게(Noun: 10, 1), " +
            "정말로(Adverb: 12, 3), 말(Noun: 16, 1), 이(Josa: 17, 1), 되니(Verb: 19, 2), " +
            "ㅋㅋ(KoreanParticle: 21, 2)]",
        processorNormalization.tokenize(text).toString()
    );
    assertEquals(
        "[이렇다(Adjective: 0, 2), 생각(Noun: 3, 2), 을(Josa: 5, 1), 하다(Verb: 7, 2), " +
            "게(Noun: 10, 1), 정말로(Adverb: 12, 3), 말(Noun: 16, 1), 이(Josa: 17, 1), " +
            "되닠*(Noun: 19, 2), ㅋㅋㅋㅋㅋ(KoreanParticle: 21, 5)]",
        processorStemming.tokenize(text).toString()
    );
    assertEquals(
        "[이런(Adjective: 0, 2), 생각(Noun: 3, 2), 을(Josa: 5, 1), 하는(Verb: 7, 2), " +
            "게(Noun: 10, 1), 정말로(Adverb: 12, 3), 말(Noun: 16, 1), 이(Josa: 17, 1), " +
            "되닠*(Noun: 19, 2), ㅋㅋㅋㅋㅋ(KoreanParticle: 21, 5)]",
        processorNeither.tokenize(text).toString()
    );
  }

  @Test
  public void testTokenizeToStrings() throws Exception {
    String text = "이런 생각을 하는 게 정말로 말이 되닠ㅋㅋㅋㅋㅋ";
    assertEquals(
        "[이렇다, 생각, 을, 하다, 게, 정말로, 말, 이, 되다, ㅋㅋ]",
        processor.tokenizeToStrings(text).toString()
    );
    assertEquals(
        "[이렇다,  , 생각, 을,  , 하다,  , 게,  , 정말로,  , 말, 이,  , 되다, ㅋㅋ]",
        processorWithSpace.tokenizeToStrings(text).toString()
    );
    assertEquals(
        "[이런, 생각, 을, 하는, 게, 정말로, 말, 이, 되니, ㅋㅋ]",
        processorNormalization.tokenizeToStrings(text).toString()
    );
    assertEquals(
        "[이렇다, 생각, 을, 하다, 게, 정말로, 말, 이, 되닠, ㅋㅋㅋㅋㅋ]",
        processorStemming.tokenizeToStrings(text).toString()
    );
    assertEquals(
        "[이런, 생각, 을, 하는, 게, 정말로, 말, 이, 되닠, ㅋㅋㅋㅋㅋ]",
        processorNeither.tokenizeToStrings(text).toString()
    );
  }

  @Test
  public void testPhraseExtractor() {
    String text = "아름다운 트위터를 만들어 보자. 시발 #욕하지_말자";

    assertEquals(
        "[아름다운 트위터(Noun: 0, 8), 시발(Noun: 18, 2), 트위터(Noun: 5, 3), #욕하지_말자(Hashtag: 21, 7)]",
        processor.extractPhrases(text).toString()
    );
    assertEquals(
        "[아름다운 트위터(Noun: 0, 8), 트위터(Noun: 5, 3), #욕하지_말자(Hashtag: 21, 7)]",
        processorWithSpamFilter.extractPhrases(text).toString()
    );
    assertEquals(
        "[아름다운 트위터(Noun: 0, 8), 시발(Noun: 18, 2), 트위터(Noun: 5, 3)]",
        processorWithoutHashtags.extractPhrases(text).toString()
    );
  }
}