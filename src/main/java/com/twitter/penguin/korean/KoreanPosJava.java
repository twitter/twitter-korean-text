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
package com.twitter.penguin.korean;

/**
 * These enum class doesn't follow Java's POS capitalization convention intentionally
 * to match with Scala's com.twitter.penguin.korean.util.KoreanPos enumeration.
 */
public enum KoreanPosJava {
  // Word leved POS
  Noun, Verb, Adjective,
  Adverb, Determiner, Exclamation,
  Josa, Eomi, PreEomi, Conjunction,
  NounPrefix, VerbPrefix, Suffix, Unknown,

  // Chunk level POS
  Korean, Foreign, Number, KoreanParticle, Alpha,
  Punctuation, Hashtag, ScreenName,
  Email, URL, CashTag,

  // Functional POS
  Space, Others,

  ProperNoun;
}
