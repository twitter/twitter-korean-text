/*
 * Twitter Korean Text - Scala library to process Korean text
 *
 * Copyright 2015 Twitter, Inc.
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
import com.twitter.penguin.korean.tokenizer.KoreanDetokenizer._

class KoreanDetokenizerTest extends TestBase {
  test("detokenizer should correctly detokenize the input text") {
    assert(
      detokenize(List("연세", "대학교", "보건", "대학원","에","오신","것","을","환영","합니다", "!"))
          === "연세대학교 보건 대학원에 오신것을 환영합니다!"
    )

    assert(
      detokenize(List("와", "!!!", "iPhone", "6+", "가",",", "드디어","나왔다", "!"))
          === "와!!! iPhone 6+ 가, 드디어 나왔다!"
    )

    assert(
      detokenize(List("뭐", "완벽", "하진", "않", "지만", "그럭저럭", "쓸", "만", "하군", "..."))
          === "뭐 완벽하진 않지만 그럭저럭 쓸 만하군..."
    )
  }

  test("detokenizer should correctly detokenize the edge cases") {
    assert(
      detokenize(List(""))
          === ""
    )

    assert(
      detokenize(List())
          === ""
    )

    assert(
      detokenize(List("완벽"))
          === "완벽"
    )

    assert(
      detokenize(List("이"))
          === "이"
    )

    assert(
      detokenize(List("이", "제품을", "사용하겠습니다"))
          === "이 제품을 사용하겠습니다"
    )
  }
}