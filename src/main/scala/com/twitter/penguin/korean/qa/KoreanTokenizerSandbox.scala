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
    val s = "멋지게 사는 싱글 친구들의 sns글을 보며 부러워하다, 난 어차피 결혼 안했어도 능력이 딸려서 저렇게 멋지게 살진 못했겠구나 하는 깨닳음을 얻었다. 어차피 이러나 저러나 망한 인생이라니!! 오 차라리 마음이 편함ㅋㅋ"

    println(
      tokenize(s, stemming = false).mkString(" ")
    )
  }
}