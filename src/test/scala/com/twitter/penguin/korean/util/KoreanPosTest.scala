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
import com.twitter.penguin.korean.util.KoreanPos._

class KoreanPosTest extends TestBase {
  test("buildTrie should build Trie correctly for initial optionals with final non-optionals") {
    // 0 -> 1
    assert(
      buildTrie("p0N1", Noun) === List(
        KoreanPosTrie(NounPrefix, List(
          KoreanPosTrie(Noun, List(), ending = Some(Noun))
        ), ending = None),
        KoreanPosTrie(Noun, List(), ending = Some(Noun))
      )
    )
    // * -> +
    assert(
      buildTrie("p*N+", Noun) === List(
        KoreanPosTrie(NounPrefix, List(
          selfNode,
          KoreanPosTrie(Noun, List(selfNode), ending = Some(Noun))
        ), ending = None),
        KoreanPosTrie(Noun, List(selfNode), ending = Some(Noun))
      )
    )
  }
  test("buildTrie should build Trie correctly for initial optionals with multiple non-optionals") {
    // 0 -> 0 -> 1
    assert(
      buildTrie("p0N0s1", Noun) === List(
        KoreanPosTrie(NounPrefix, List(
          KoreanPosTrie(Noun, List(
            KoreanPosTrie(Suffix, List(), ending = Some(Noun))
          ), ending = None),
          KoreanPosTrie(Suffix, List(), ending = Some(Noun))
        ), ending = None),
        KoreanPosTrie(Noun, List(
          KoreanPosTrie(Suffix, List(), ending = Some(Noun))
        ), ending = None),
        KoreanPosTrie(Suffix, List(), ending = Some(Noun))
      )
    )
  }
  test("buildTrie should build Trie correctly for initial non-optionals with final non-optionals") {
    // 1 -> +
    assert(
      buildTrie("p1N+", Noun) === List(
        KoreanPosTrie(NounPrefix, List(
          KoreanPosTrie(Noun, List(
            selfNode
          ), ending = Some(Noun))
        ), ending = None)
      )
    )
    // + -> 1
    assert(
      buildTrie("N+s1", Noun) === List(
        KoreanPosTrie(Noun, List(
          selfNode,
          KoreanPosTrie(Suffix, List(), ending = Some(Noun))
        ), ending = None)
      )
    )
  }

  test("buildTrie should build Trie correctly for initial non-optionals with final optionals") {
    // 1 -> *
    assert(
      buildTrie("p1N*", Noun) === List(
        KoreanPosTrie(NounPrefix, List(
          KoreanPosTrie(Noun, List(
            selfNode
          ), ending = Some(Noun))
        ), ending = Some(Noun))
      )
    )
    // + -> 0
    assert(
      buildTrie("N+s0", Noun) === List(
        KoreanPosTrie(Noun, List(
          selfNode,
          KoreanPosTrie(Suffix, List(), ending = Some(Noun))
        ), ending = Some(Noun))
      )
    )
  }
  test("buildTrie should build Trie correctly for initial non-optionals with multiple non-optionals") {
    // + -> + -> 0
    assert(
      buildTrie("A+V+A0", Verb) === List(
        KoreanPosTrie(Adverb, List(
          selfNode,
          KoreanPosTrie(Verb, List(
            selfNode,
            KoreanPosTrie(Adverb, List(), ending = Some(Verb))
          ), ending = Some(Verb))
        ), ending = None)
      )
    )
  }

}
