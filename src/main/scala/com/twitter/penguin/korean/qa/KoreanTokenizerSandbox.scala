package com.twitter.penguin.korean.qa

import com.twitter.penguin.korean.TwitterKoreanProcessor._

/**
 * A simple testing tool to try Korean tokenization.
 *
 * usage: ./pants goal run src/scala/com/twitter/penguin/korean/qa:korean_tokenizer_sandbox
 *
 */
object KoreanTokenizerSandbox {
  def main(args: Array[String]) {
    val s = "거기에있는것은자기마음이원하는행복한시간입니다굿나잇잘자고좋은꿈행복한꿈꾸세요"

    println(
      tokenize(s).mkString(" ")
    )
  }
}