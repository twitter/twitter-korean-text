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
object KoreanTokenizerSandbox {
  def main(args: Array[String]) {
    val s = "거기에있는것은자기마음이원하는행복한시간입니다굿나잇잘자고좋은꿈행복한꿈꾸세요"

    println(
      tokenize(s).mkString(" ")
    )
  }
}