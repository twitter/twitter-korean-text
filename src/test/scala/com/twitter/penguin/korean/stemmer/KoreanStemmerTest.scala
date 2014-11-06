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

package com.twitter.penguin.korean.stemmer

import com.twitter.penguin.korean.stemmer.KoreanStemmer.StemmedTextWithTokens
import com.twitter.penguin.korean.tokenizer.KoreanTokenizer
import com.twitter.penguin.korean.tokenizer.KoreanTokenizer.KoreanToken
import com.twitter.penguin.korean.util.KoreanPos._
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class KoreanStemmerTest extends FunSuite {

  val sampleText1 = "새로운 스테밍을 추가했었다."
  val sampleStems1 = Seq(
    KoreanToken("새롭다", Adjective),
    KoreanToken("스테밍", Noun),
    KoreanToken("을", Josa),
    KoreanToken("추가하다", Verb),
    KoreanToken(".", Punctuation)
  )

  val sampleText2 = "그런 사람 없습니다.."
  val sampleStems2 = Seq(KoreanToken("그렇다", Adjective),
    KoreanToken("사람", Noun),
    KoreanToken("없다", Adjective),
    KoreanToken("..", Punctuation)
  )


  test("stemPredicates should stem predicates from Korean tokens") {
    assert(
      KoreanStemmer.stemPredicates(
        KoreanTokenizer.tokenize(sampleText1)
      ).flatten === sampleStems1
    )

    assert(
      KoreanStemmer.stemPredicates(
        KoreanTokenizer.tokenize(sampleText2)
      ).flatten === sampleStems2
    )
  }

  test("stem should transform the original text along with the tokens") {
    assert(
      KoreanStemmer.stem(sampleText1)
        === StemmedTextWithTokens((new StringBuilder).append("새롭다 스테밍을 추가하다."), sampleStems1)
    )
    assert(
      KoreanStemmer.stem(sampleText2)
        === StemmedTextWithTokens((new StringBuilder).append("그렇다 사람 없다.."), sampleStems2)
    )
  }

  test("stem should stem adjectives correctly") {
    val words = Map(
      "예뻐도" -> "예쁘다Adjective",
      "예뻐서" -> "예쁘다Adjective",
      "예뻤다" -> "예쁘다Adjective",
      "예뻤었겠지" -> "예쁘다Adjective",
      "예뻤지" -> "예쁘다Adjective",
      "예쁘겠지" -> "예쁘다Adjective",
      "예쁘긴" -> "예쁘다Adjective",
      "예쁘지만" -> "예쁘다Adjective",
      "예쁜 것 같다" -> "예쁘다Adjective 것Noun 같다Adjective",
      "예쁜건 아니잖아" -> "예쁘다Adjective 알다Verb", // todo: this should be 아니다Adjective
      "예쁠 수 있을까" -> "예쁘다Adjective 수Noun 있다Adjective",
      "예쁠" -> "예쁘다Adjective",
      "예쁠수있을까" -> "예쁘다Adjective",
      "예쁜" -> "예쁘다Adjective"
    )
    words.foreach {
      case (word, stem) =>
        val stemmed = KoreanStemmer.stem(word)
        assert(stem === stemmed.tokens.mkString(" "))
    }
  }

  test("stem should stem verbs correctly") {
    val words = Map(
      "먹어도" -> "먹다Verb",
      "먹어서" -> "먹다Verb",
      "먹었다" -> "먹다Verb",
      "먹었었겠지" -> "먹다Verb",
      "먹었지" -> "먹다Verb",
      "먹겠지" -> "먹다Verb",
      "먹긴" -> "먹다Verb",
      "먹지만" -> "먹다Verb",
      "먹은 것 같다" -> "먹다Verb 것Noun 같다Adjective",
      "먹은건 아니잖아" -> "먹다Verb 알다Verb", // todo: this should be 아니다Adjective
      "먹을 수 있을까" -> "먹다Verb 수Noun 있다Adjective",
      "먹을" -> "먹다Verb",
      "먹을수있을까" -> "먹다Verb",
      "먹은" -> "먹다Verb"
    )
    words.foreach {
      case (word, stem) =>
        val stemmed = KoreanStemmer.stem(word)
        assert(stem === stemmed.tokens.mkString(" "))
    }
  }
}