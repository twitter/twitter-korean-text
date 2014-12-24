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

package com.twitter.penguin.korean.tokenizer

import com.twitter.penguin.korean.TestBase
import com.twitter.penguin.korean.tokenizer.KoreanChunker._
import com.twitter.penguin.korean.tokenizer.KoreanTokenizer.KoreanToken
import com.twitter.penguin.korean.util.KoreanPos._

class KoreanChunkerTest extends TestBase {

  test("getChunks should correctly split a string into Korean-sensitive chunks") {
    assert(
      getChunks("안녕? iphone6안녕? 세상아?")
        === "안녕 ? iphone 6 안녕 ? 세상아 ?".split(" ").toSeq
    )

    assert(
      getChunks("This is an 한국어가 섞인 English tweet.")
        === "This is an 한국어가 섞인 English tweet .".split(" ").toSeq
    )

    assert(
      getChunks("이 日本것은 日本語Eng")
        === "이 日本 것은 日本語 Eng".split(" ").toSeq
    )

    assert(
      getChunks("무효이며")
        === Seq("무효이며")
    )

    assert(
      getChunks("#해쉬태그 이라는 것 #hash @hello 123 이런이런 #여자최애캐_5명으로_취향을_드러내자")
        === "#해쉬태그 이라는 것 #hash @hello 123 이런이런 #여자최애캐_5명으로_취향을_드러내자".split(" ").toSeq
    )
  }

  test("getChunks should correctly split a string into Korean-sensitive chunks with spaces") {
    assert(
      getChunks("안녕? iphone6안녕? 세상아?", keepSpace = true)
        === Seq("안녕", "?", " ", "iphone", "6", "안녕", "?", " ", "세상아", "?")
    )

    assert(
      getChunks("This is an 한국어가 섞인 English tweet.", keepSpace = true)
        === Seq("This", " ", "is", " ", "an", " ", "한국어가", " ", "섞인", " ", "English", " ", "tweet", ".")
    )

    assert(
      getChunks("이 日本것은 日本語Eng", keepSpace = true)
        === Seq("이", " ", "日本", "것은", " ", "日本語", "Eng")
    )

    assert(
      getChunks("무효이며", keepSpace = true)
        === Seq("무효이며")
    )

    assert(
      getChunks("#해쉬태그 이라는 것 #hash @hello 123 이런이런 #여자최애캐_5명으로_취향을_드러내자", keepSpace = true)
        === Seq("#해쉬태그", " ", "이라는", " ", "것", " ", "#hash", " ", "@hello", " ",
        "123", " ", "이런이런", " ", "#여자최애캐_5명으로_취향을_드러내자")
    )
  }


  test("getChunkTokens should correctly find chunks with correct POS tags") {
    assert(
      chunk("한국어와 English와 1234와 pic.twitter.com " +
        "http://news.kukinews.com/article/view.asp?" +
        "page=1&gCode=soc&arcid=0008599913&code=41121111 " +
        "hohyonryu@twitter.com 갤럭시 S5").mkString(" ")
        ===
        "한국어와Korean EnglishAlpha 와Korean 1234Number 와Korean " +
          "pic.twitter.comURL " +
          "http://news.kukinews.com/article/view.asp?" +
          "page=1&gCode=soc&arcid=0008599913&code=41121111URL " +
          "hohyonryu@twitter.comEmail 갤럭시Korean SAlpha 5Number"
    )

    assert(
      chunk("우와!!! 완전ㅋㅋㅋㅋ")
        === Seq(
        KoreanToken("우와", Korean), KoreanToken("!!!", Punctuation),
        KoreanToken("완전", Korean), KoreanToken("ㅋㅋㅋㅋ", KoreanParticle))
    )

    assert(
      chunk("@nlpenguin @edeng #korean_tokenizer_rocks 우하하")
        === Seq(KoreanToken("@nlpenguin", ScreenName), KoreanToken("@edeng", ScreenName),
        KoreanToken("#korean_tokenizer_rocks", Hashtag), KoreanToken("우하하", Korean))
    )
  }
}