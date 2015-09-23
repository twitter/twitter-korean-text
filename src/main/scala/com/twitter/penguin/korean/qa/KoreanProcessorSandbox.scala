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

package com.twitter.penguin.korean.qa

import com.twitter.penguin.korean.TwitterKoreanProcessor._

/**
 * A simple testing tool to try Korean tokenization.
 */
object KoreanProcessorSandbox {
  def main(args: Array[String]) {
    val s = "새로운 스테밍을 추가했었다. 트위터 주식 25% 상승. 주식 400원. 400원. $TWTR $200 20% 상승"

    val tokens = tokenize(s)

    println(
      tokens.mkString(" ")
    )

    println(
      stem(tokens).mkString(" ")
    )

    println(
      extractPhrases(tokens)
    )
  }
}