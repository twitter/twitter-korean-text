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

import com.twitter.penguin.korean.util.KoreanPos._

object TokenizerProfile {
  val defaultProfile: TokenizerProfile = TokenizerProfile()
}

// Lower score is better
case class TokenizerProfile(
    tokenCount: Float = 0.18f,
    unknown: Float = 0.3f,
    wordCount: Float = 0.3f,
    freq: Float = 0.2f,
    unknownCoverage: Float = 0.5f,
    exactMatch: Float = 0.5f,
    allNoun: Float = 0.1f,
    unknownPosCount: Float = 10.0f,
    determinerPosCount: Float = -0.01f,
    exclamationPosCount: Float = 0.01f,
    initialPostPosition: Float = 0.2f,
    haVerb: Float = 0.3f,
    preferredPattern: Float = 0.6f,
    preferredPatterns: Seq[Seq[Any]] = Seq(Seq(Noun, Josa), Seq(ProperNoun, Josa)),
    spaceGuide: Set[Int] = Set[Int](),
    spaceGuidePenalty: Float = 3.0f
)
