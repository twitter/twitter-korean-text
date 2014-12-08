package com.twitter.penguin.korean;

import org.junit.Test;

import com.twitter.penguin.korean.stemmer.KoreanStemmer;

import static org.junit.Assert.assertEquals;

public class TwitterKoreanProcessorJavaTest {
  TwitterKoreanProcessorJava processor = new TwitterKoreanProcessorJava.Builder().build();
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

  @Test
  public void testNormalize() throws Exception {
    assertEquals("힘들겠습니다 그래요ㅋㅋ", processor.normalize("힘들겟씀다 그래욬ㅋㅋㅋ"));
  }

  @Test
  public void testStem() throws Exception {
    KoreanStemmer.StemmedTextWithTokens stemmed = processor.stem("아름다운 강산을 귀여워서 먹었다.");

    assertEquals("아름답다 강산을 귀엽다 먹다.", stemmed.text().toString());
    assertEquals("아름답다Adjective 강산Noun 을Josa 귀엽다Adjective 먹다Verb .Punctuation",
        stemmed.tokens().mkString(" ")
    );
  }

  @Test
  public void testTokenize() throws Exception {
    String text = "이런 생각을 하는 게 정말로 말이 되닠ㅋㅋㅋㅋㅋ";
    assertEquals(
        "[이렇다Adjective, 생각Noun, 을Josa, 하다Verb, 게Noun, 정말로Adverb, " +
            "말Noun, 이Josa, 되다Verb, ㅋㅋKoreanParticle]",
        processor.tokenize(text).toString()
    );
    assertEquals(
        "[이런Adjective, 생각Noun, 을Josa, 하는Verb, 게Noun, 정말로Adverb, " +
            "말Noun, 이Josa, 되니Verb, ㅋㅋKoreanParticle]",
        processorNormalization.tokenize(text).toString()
    );
    assertEquals(
        "[이렇다Adjective, 생각Noun, 을Josa, 하다Verb, 게Noun, 정말로Adverb, " +
            "말Noun, 이Josa, 되닠Noun*, ㅋㅋㅋㅋㅋKoreanParticle]",
        processorStemming.tokenize(text).toString()
    );
    assertEquals(
        "[이런Adjective, 생각Noun, 을Josa, 하는Verb, 게Noun, 정말로Adverb, " +
            "말Noun, 이Josa, 되닠Noun*, ㅋㅋㅋㅋㅋKoreanParticle]",
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
  public void testTokenizeWithIndex() throws Exception {
    String text = "아름다운 트위터를 만들어 보자.";
    Iterable<TwitterKoreanProcessor.KoreanSegment> segements = processor.tokenizeWithIndex(text);
    assertEquals(
        "[KoreanSegment(0,4,아름다운Adjective), KoreanSegment(5,3,트위터Noun), " +
            "KoreanSegment(8,1,를Josa), KoreanSegment(10,3,만들어Verb), " +
            "KoreanSegment(14,2,보자Verb), KoreanSegment(16,1,.Punctuation)]",
        segements.toString()
    );
  }

  @Test
  public void testTokenizeWithIndexWithStemmer() throws Exception {
    String text = "아름다운 트위터를 만들어 보자.";
    TwitterKoreanProcessor.KoreanSegmentWithText segements =
        processor.tokenizeWithIndexWithStemmer(text);

    assertEquals("아름답다 트위터를 만들다 보다.", segements.text().toString());
    assertEquals("List(KoreanSegment(0,4,아름답다Adjective), KoreanSegment(5,3,트위터Noun), " +
            "KoreanSegment(8,1,를Josa), KoreanSegment(10,3,만들다Verb), KoreanSegment(14,2,보다Verb), " +
            "KoreanSegment(16,1,.Punctuation))",
        segements.segments().toString()
    );

  }
}