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

class KoreanSentenceSplitterTest extends TestBase {

  test("split should correctly split a string into sentences") {
    assert(
      KoreanSentenceSplitter.split("안녕? iphone6안녕? 세상아?").mkString("/")
        === "안녕?(0,3)/iphone6안녕?(4,14)/세상아?(15,19)"
    )

    assert(
      KoreanSentenceSplitter.split("그런데, 누가 그러는데, 루루가 있대. 그렇대? 그렇지! 아리고 이럴수가!!!!! 그래...").mkString("/")
        === "그런데, 누가 그러는데, 루루가 있대.(0,21)/그렇대?(22,26)/그렇지!(27,31)/아리고 이럴수가!!!!!(32,45)/그래...(46,51)"
    )

    assert(
      KoreanSentenceSplitter.split("이게 말이 돼?! 으하하하 ㅋㅋㅋㅋㅋㅋㅋ…    ").mkString("/")
        === "이게 말이 돼?!(0,9)/으하하하 ㅋㅋㅋㅋㅋㅋㅋ…(10,23)"
    )
  }

}