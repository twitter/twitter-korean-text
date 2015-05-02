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

import java.util.List;

import com.twitter.penguin.korean.TwitterKoreanProcessorJava;
import com.twitter.penguin.korean.phrase_extractor.KoreanPhraseExtractor;
import com.twitter.penguin.korean.tokenizer.KoreanTokenizer;

public class JavaTwitterKoreanTextExample {
  public static void main(String[] args) {
    // Tokenize with normalization + stemmer
    TwitterKoreanProcessorJava processor = new TwitterKoreanProcessorJava.Builder().build();

    List<String> parsedStrings = processor.tokensToJavaStringList("한국어를 처리하는 예시입니닼ㅋㅋㅋㅋㅋ");
    System.out.println(parsedStrings);
    // output: [한국어, 를, 처리, 하다, 예시, 이다, ㅋㅋ]

    List<KoreanTokenizer.KoreanToken> parsed = processor
        .tokenize("한국어를 처리하는 예시입니닼ㅋㅋㅋㅋㅋ");
    System.out.println(parsed);
    // output: [한국어(Noun: 0, 3), 를(Josa: 3, 1), 처리(Noun: 5, 2), 하다(Verb: 7, 2), 예시(Noun: 10, 2), 이다(Adjective: 12, 3), ㅋㅋ(KoreanParticle: 15, 2)]


    // Tokenize without stemmer
    processor = new TwitterKoreanProcessorJava.Builder()
            .disableStemmer()
            .build();

    parsedStrings = processor.tokensToJavaStringList("한국어를 처리하는 예시입니닼ㅋㅋㅋㅋㅋ");
    System.out.println(parsedStrings);
    // output: [한국어, 를, 처리, 하는, 예시, 입니, 다, ㅋㅋ]

    parsed = processor.tokenize("한국어를 처리하는 예시입니닼ㅋㅋㅋㅋㅋ");
    System.out.println(parsed);
    // output: [한국어(Noun: 0, 3), 를(Josa: 3, 1), 처리(Noun: 5, 2), 하는(Verb: 7, 2), 예시(Noun: 10, 2), 입니(Adjective: 12, 2), 다(Eomi: 14, 1), ㅋㅋ(KoreanParticle: 15, 2)]


    // Tokenize with neither normalization nor stemmer
    processor = new TwitterKoreanProcessorJava.Builder()
        .disableNormalizer()
        .disableStemmer()
        .build();

    parsedStrings = processor.tokensToJavaStringList("한국어를 처리하는 예시입니닼ㅋㅋㅋㅋㅋ");
    System.out.println(parsedStrings);
    // output: [한국어, 를, 처리, 하는, 예시, 입니, 닼, ㅋㅋㅋㅋㅋ]

    parsed = processor.tokenize("한국어를 처리하는 예시입니닼ㅋㅋㅋㅋㅋ");
    System.out.println(parsed);
    // output: [한국어(Noun: 0, 3), 를(Josa: 3, 1), 처리(Noun: 5, 2), 하는(Verb: 7, 2), 예시(Noun: 10, 2), 입니(Adjective: 12, 2), 닼*(Noun: 14, 1), ㅋㅋㅋㅋㅋ(KoreanParticle: 15, 5)]

    List<KoreanPhraseExtractor.KoreanPhrase> phrases = processor
        .extractPhrases("한국어를 처리하는 예시입니닼ㅋㅋㅋㅋㅋ 시발");
    System.out.println(phrases);
    // output: [한국어(Noun: 0, 3), 처리(Noun: 5, 2), 처리하는 예시(Noun: 5, 7), 예시(Noun: 10, 2), 시발(Noun: 18, 2)]

    processor = new TwitterKoreanProcessorJava.Builder()
        .disableNormalizer()
        .disableStemmer()
        .enablePhraseExtractorSpamFilter()
        .build();

    phrases = processor.extractPhrases("한국어를 처리하는 예시입니닼ㅋㅋㅋㅋㅋ 시발");
    System.out.println(phrases);
    // output: [한국어(Noun: 0, 3), 처리(Noun: 5, 2), 처리하는 예시(Noun: 5, 7), 예시(Noun: 10, 2)]
  }
}
