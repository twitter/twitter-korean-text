/*
 * Twitter Korean Text - Scala library to process Korean text
 *
 * Copyright 2016 Twitter, Inc.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import scala.collection.Seq;

import org.junit.Test;

import com.twitter.penguin.korean.tokenizer.KoreanTokenizer;
import com.twitter.penguin.korean.tokenizer.Sentence;

import static org.junit.Assert.assertEquals;

public class TwitterKoreanProcessorJavaTest {
  @Test
  public void testNormalize() throws Exception {
    assertEquals("힘들겠습니다 그래요ㅋㅋ", TwitterKoreanProcessorJava.normalize("힘들겟씀다 그래욬ㅋㅋㅋ"));
  }

  @Test
  public void testTokenize() throws Exception {
    String text = "착한강아지상을 받은 루루";
    assertEquals(
        "List(착한(Adjective: 0, 2), 강아지(Noun: 2, 3), 상(Suffix: 5, 1), 을(Josa: 6, 1), " +
            " (Space: 7, 1), 받은(Verb: 8, 2),  (Space: 10, 1), 루루(Noun: 11, 2))",
        TwitterKoreanProcessorJava.tokenize(text).toString()
    );
  }

  @Test
  public void testStem() throws Exception {
    Seq<KoreanTokenizer.KoreanToken> tokens = TwitterKoreanProcessorJava.tokenize("아름다운 강산을 귀여워서 먹었다.");
    Seq<KoreanTokenizer.KoreanToken> stemmed = TwitterKoreanProcessorJava.stem(tokens);

    assertEquals(
        "[아름답다(Adjective: 0, 4), 강산(Noun: 5, 2), 을(Josa: 7, 1), 귀엽다(Adjective: 9, 4), " +
            "먹다(Verb: 14, 3), .(Punctuation: 17, 1)]",
        TwitterKoreanProcessorJava.tokensToJavaKoreanTokenList(stemmed).toString());

    assertEquals("[아름답다, 강산, 을, 귀엽다, 먹다, .]",
        TwitterKoreanProcessorJava.tokensToJavaStringList(stemmed).toString());
  }

  @Test
  public void testTokensToJavaStringList() throws Exception {
    String text = "착한강아지상을 받은 루루";
    Seq<KoreanTokenizer.KoreanToken> tokens = TwitterKoreanProcessorJava.tokenize(text);
    assertEquals(
        "[착한, 강아지, 상, 을,  , 받은,  , 루루]",
        TwitterKoreanProcessorJava.tokensToJavaStringList(tokens, true).toString()
    );

    assertEquals(
        "[착한, 강아지, 상, 을, 받은, 루루]",
        TwitterKoreanProcessorJava.tokensToJavaStringList(tokens, false).toString()
    );
  }

  @Test
  public void testAddToDictionary() {
    Seq<KoreanTokenizer.KoreanToken> tokens = TwitterKoreanProcessorJava.tokenize("우햐나어가녀아뎌");
    assertEquals("[우햐나어가녀아뎌]", TwitterKoreanProcessorJava.tokensToJavaStringList(tokens).toString());

    ArrayList<String> words = new ArrayList<>();
    words.add("우햐나");
    words.add("어가녀");
    words.add("아뎌");
    TwitterKoreanProcessorJava.addNounsToDictionary(words);

    tokens = TwitterKoreanProcessorJava.tokenize("우햐나어가녀아뎌");

    assertEquals("[우햐나, 어가녀, 아뎌]", TwitterKoreanProcessorJava.tokensToJavaStringList(tokens).toString());
  }

  @Test
  public void testTokensToJavaKoreanTokenList() throws Exception {
    String text =  "착한강아지상을 받은 루루";
    Seq<KoreanTokenizer.KoreanToken> tokens = TwitterKoreanProcessorJava.tokenize(text);
    assertEquals(
        "[착한(Adjective: 0, 2), 강아지(Noun: 2, 3), 상(Suffix: 5, 1), 을(Josa: 6, 1), " +
            " (Space: 7, 1), 받은(Verb: 8, 2),  (Space: 10, 1), 루루(Noun: 11, 2)]",
        TwitterKoreanProcessorJava.tokensToJavaKoreanTokenList(tokens, true).toString()
    );

    assertEquals(
        "[착한(Adjective: 0, 2), 강아지(Noun: 2, 3), 상(Suffix: 5, 1), 을(Josa: 6, 1), " +
            "받은(Verb: 8, 2), 루루(Noun: 11, 2)]",
        TwitterKoreanProcessorJava.tokensToJavaKoreanTokenList(tokens, false).toString()
    );
  }

  @Test
  public void testPhraseExtractor() {
    String text = "아름다운 트위터를 만들어 보자. 시발 #욕하지_말자";
    Seq<KoreanTokenizer.KoreanToken> tokens = TwitterKoreanProcessorJava.tokenize(text);

    assertEquals(
        "[아름다운 트위터(Noun: 0, 8), 트위터(Noun: 5, 3), #욕하지_말자(Hashtag: 21, 7)]",
        TwitterKoreanProcessorJava.extractPhrases(tokens, true, true).toString()
    );
    assertEquals(
        "[아름다운 트위터(Noun: 0, 8), 트위터(Noun: 5, 3)]",
        TwitterKoreanProcessorJava.extractPhrases(tokens, true, false).toString()
    );
    assertEquals(
        "[아름다운 트위터(Noun: 0, 8), 시발(Noun: 18, 2), 트위터(Noun: 5, 3), #욕하지_말자(Hashtag: 21, 7)]",
        TwitterKoreanProcessorJava.extractPhrases(tokens, false, true).toString()
    );
    assertEquals(
        "[아름다운 트위터(Noun: 0, 8), 시발(Noun: 18, 2), 트위터(Noun: 5, 3)]",
        TwitterKoreanProcessorJava.extractPhrases(tokens, false, false).toString()
    );
  }

  @Test
  public void testSentenceSplitter() {
    String text = "가을이다! 남자는 가을을 탄다...... 그렇지? 루루야! 버버리코트 사러 가자!!!!";
    List<Sentence> tokens = TwitterKoreanProcessorJava.splitSentences(text);

    assertEquals(
        "[가을이다!(0,5), 남자는 가을을 탄다......(6,22), 그렇지?(23,27), 루루야!(28,32), 버버리코트 사러 가자!!!!(33,48)]",
        tokens.toString()
    );
  }

  @Test
  public void testDetokenizer() {
    List<String> words = Arrays.asList("늘", "평온", "하게", "누워", "있", "는",  "루루");

    assertEquals(
        "늘 평온하게 누워있는 루루",
        TwitterKoreanProcessorJava.detokenize(words)
    );
  }
}