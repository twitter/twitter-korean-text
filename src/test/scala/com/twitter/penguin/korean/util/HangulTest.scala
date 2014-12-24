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

import com.twitter.penguin.korean.TestBase
import com.twitter.penguin.korean.util.Hangul._

class HangulTest extends TestBase {

  test("decomposeKoreanChar should decompose full Korean chars correctly") {
    assert(decomposeHangul('간') == HangulChar('ㄱ', 'ㅏ', 'ㄴ'))
    assert(decomposeHangul('관') == HangulChar('ㄱ', 'ㅘ', 'ㄴ'))
    assert(decomposeHangul('꼃') == HangulChar('ㄲ', 'ㅕ', 'ㅀ'))
  }

  test("decomposeKoreanChar should decompose full no coda chars correctly") {
    assert(decomposeHangul('가') == HangulChar('ㄱ', 'ㅏ', ' '))
    assert(decomposeHangul('과') == HangulChar('ㄱ', 'ㅘ', ' '))
    assert(decomposeHangul('껴') == HangulChar('ㄲ', 'ㅕ', ' '))
  }

  test("decomposeKoreanChar should raise an exception if input is invalud") {
    intercept[IllegalArgumentException] {
      decomposeHangul('ㅋ')
    }
    intercept[IllegalArgumentException] {
      decomposeHangul('ㅏ')
    }
    intercept[IllegalArgumentException] {
      decomposeHangul('ㅀ')
    }
  }

  test("hasCoda should return true when a character has a coda") {
    assert(hasCoda('갈'))
    assert(hasCoda('갉'))
  }

  test("hasCoda should return false when a character does not have a coda") {
    assert(!hasCoda('가'))
    assert(!hasCoda('ㄱ'))
    assert(!hasCoda('ㅘ'))
    assert(!hasCoda(' '))
  }

  test("composeKoreanChar should compose a full Korean char from a triple of letters") {
    assert(composeHangul('ㄱ', 'ㅏ', 'ㄷ') === '갇')
    assert(composeHangul('ㄲ', 'ㅑ', 'ㅀ') === '꺓')
    assert(composeHangul('ㅊ', 'ㅘ', 'ㄴ') === '촨')
  }

  test("composeKoreanChar should compose a no-coda Korean char from a triple of letters") {
    assert(composeHangul('ㄱ', 'ㅏ', ' ') === '가')
    assert(composeHangul('ㄲ', 'ㅑ', ' ') === '꺄')
    assert(composeHangul('ㅊ', 'ㅘ', ' ') === '촤')
  }

  test("composeKoreanChar should raise an exception if input is invalid") {
    intercept[IllegalArgumentException] {
      composeHangul(' ', 'ㅏ', ' ')
    }
    intercept[IllegalArgumentException] {
      composeHangul('ㄲ', ' ', ' ')
    }
    intercept[IllegalArgumentException] {
      composeHangul(' ', ' ', 'ㄴ')
    }
  }
}

