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

package com.twitter.penguin.korean.util

import com.twitter.penguin.korean.tokenizer.KoreanTokenizer.KoreanToken
import com.twitter.penguin.korean.util.KoreanPos._
import com.twitter.penguin.korean.util.KoreanSubstantive._
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class KoreanSubstantiveTest extends FunSuite {

  test("isJosaAttachable") {
    //애플은
    assert(isJosaAttachable('플', '은'))
    assert(isJosaAttachable('플', '이'))
    assert(isJosaAttachable('플', '을'))
    assert(isJosaAttachable('플', '과'))
    assert(isJosaAttachable('플', '아'))

    //애플가
    assert(!isJosaAttachable('플', '는'))
    assert(!isJosaAttachable('플', '가'))
    assert(!isJosaAttachable('플', '를'))
    assert(!isJosaAttachable('플', '와'))
    assert(!isJosaAttachable('플', '야'))
    assert(!isJosaAttachable('플', '여'))
    assert(!isJosaAttachable('플', '라'))

    //에프은
    assert(!isJosaAttachable('프', '은'))
    assert(!isJosaAttachable('프', '이'))
    assert(!isJosaAttachable('프', '을'))
    assert(!isJosaAttachable('프', '과'))
    assert(!isJosaAttachable('프', '아'))

    //에프가
    assert(isJosaAttachable('프', '는'))
    assert(isJosaAttachable('프', '가'))
    assert(isJosaAttachable('프', '를'))
    assert(isJosaAttachable('프', '와'))
    assert(isJosaAttachable('프', '야'))
    assert(isJosaAttachable('프', '여'))
    assert(isJosaAttachable('프', '라'))
  }

  test("isName should return false if input length is not 3") {
    assert(!isName("김"))
    assert(!isName("관진"))
    assert(!isName("유호현진"))
  }

  test("isName should correctly identify 3-char person names") {
    assert(isName("유호현"))
    assert(isName("김혜진"))
    assert(!isName("개루루"))

    assert(isName("이상헌"))
    assert(isName("박수형"))

    assert(isName("이은별"))
    assert(isName("최종은"))

    assert(isName("박근혜"))
    assert(isName("손석희"))
    assert(isName("강철중"))

    assert(!isName("사측의"))
    assert(!isName("사다리"))
    assert(!isName("철지난"))
    assert(!isName("수용액"))
    assert(!isName("눈맞춰"))
  }

  test("isKoreanNumber should return true if the text is a Korean number") {
    assert(isKoreanNumber("천이백만이십오"))
    assert(isKoreanNumber("이십"))
    assert(isKoreanNumber("오"))
    assert(isKoreanNumber("삼"))
  }

  test("isKoreanNumber should return false if the text is not a Korean number") {
    assert(!isKoreanNumber("영삼"))
    assert(!isKoreanNumber("이정"))
    assert(!isKoreanNumber("조삼모사"))
  }

  test("collapseNouns should collapse single-length nouns correctly") {
    assert(
      collapseNouns(Seq(KoreanToken("마", Noun), KoreanToken("코", Noun), KoreanToken("토", Noun)))
        === Seq(KoreanToken("마코토", Noun, unknown = true))
    )

    assert(
      collapseNouns(Seq(KoreanToken("마", Noun), KoreanToken("코", Noun),
        KoreanToken("토", Noun), KoreanToken("를", Josa)))
        === Seq(KoreanToken("마코토", Noun, unknown = true), KoreanToken("를", Josa))
    )

    assert(
      collapseNouns(Seq(KoreanToken("개", NounPrefix), KoreanToken("마", Noun),
        KoreanToken("코", Noun), KoreanToken("토", Noun)))
        === Seq(KoreanToken("개", NounPrefix), KoreanToken("마코토", Noun, unknown = true))
    )

    assert(
      collapseNouns(Seq(KoreanToken("마", Noun), KoreanToken("코", Noun),
        KoreanToken("토", Noun), KoreanToken("사람", Noun)))
        === Seq(KoreanToken("마코토", Noun, unknown = true), KoreanToken("사람", Noun))
    )

    assert(
      collapseNouns(Seq(KoreanToken("마", Noun), KoreanToken("코", Noun),
        KoreanToken("사람", Noun), KoreanToken("토", Noun)))
        === Seq(KoreanToken("마코", Noun, unknown = true), KoreanToken("사람", Noun), KoreanToken("토", Noun))
    )
  }

}

