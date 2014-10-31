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

import scala.collection.JavaConversions;
import scala.collection.Seq;

import com.twitter.penguin.korean.TwitterKoreanProcessor;
import com.twitter.penguin.korean.tokenizer.KoreanTokenizer;

public class JavaTwitterKoreanTextExample {
  public static void main(String[] args) {
    // Tokenize into List<String>
    Seq<String> parsed = TwitterKoreanProcessor.tokenizeToNormalizedStrings("한국어를 처리하는 예시입니닼ㅋㅋㅋㅋㅋ");
    List<String> javaParsed = JavaConversions.seqAsJavaList(parsed);
    System.out.println(javaParsed);  // [한국어, 를, 처리, 하는, 예시, 입, 니다, ㅋㅋ]

    // Tokenize with Part-of-Speech information
    Seq<KoreanTokenizer.KoreanToken> parsedPos = TwitterKoreanProcessor.tokenizeWithNormalization("한국어를 처리하는 예시입니닼ㅋㅋㅋㅋㅋ");
    List<KoreanTokenizer.KoreanToken> javaParsedPos = JavaConversions.seqAsJavaList(parsedPos);
    System.out.println(javaParsedPos);  // [한국어Noun, 를Josa, 처리Noun, 하는Verb, 예시Noun, 입Verb, 니다Eomi, ㅋㅋKoreanParticle]
  }
}
