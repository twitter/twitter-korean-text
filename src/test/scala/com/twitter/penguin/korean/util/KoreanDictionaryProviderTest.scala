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
import KoreanDictionaryProvider._
class KoreanDictionaryProviderTest extends TestBase {

  test("addWordsToDictionary should add words to dictionary") {
    val nonExsistentWord = "없는명사다"

    assert(!koreanDictionary(KoreanPos.Noun).contains(nonExsistentWord))

    addWordsToDictionary(KoreanPos.Noun, Seq(nonExsistentWord))

    assert(koreanDictionary(KoreanPos.Noun).contains(nonExsistentWord))
  }
}

