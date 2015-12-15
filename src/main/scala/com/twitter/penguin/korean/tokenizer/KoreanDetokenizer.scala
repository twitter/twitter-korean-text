package com.twitter.penguin.korean.tokenizer

import com.twitter.penguin.korean.tokenizer.KoreanTokenizer.KoreanToken
import com.twitter.penguin.korean.util.KoreanPos

/**
  * Created by hohyonryu on 12/14/15.
  */
object KoreanDetokenizer {
  val SuffixPos = Set(KoreanPos.Josa, KoreanPos.Eomi, KoreanPos.PreEomi, KoreanPos.Suffix, KoreanPos.Punctuation)
  val PrefixPos = Set(KoreanPos.NounPrefix, KoreanPos.VerbPrefix)

  def detokenize(input: Iterable[String]) = {
    val (spaceGuide, index) = input.foldLeft((Set[Int](), 0)) {
      case ((output: Set[Int], i: Int), word: String) =>
        (output + (i + word.length), i + word.length)
    }

    val tokenized = KoreanTokenizer.tokenize(input.mkString(""), TokenizerProfile(spaceGuide = spaceGuide))

    val (output, prefix) = tokenized.foldLeft((List[String](), false)) {
      case ((output: List[String], prefix: Boolean), token: KoreanToken) =>
        if (prefix || SuffixPos.contains(token.pos)) {
          val attached = output.lastOption.getOrElse("") + token.text
          (output.init :+ attached, false)
        } else if (PrefixPos.contains(token.pos)) {
          (output :+ token.text, true)
        } else {
          (output :+ token.text, false)
        }
    }

    output.mkString(" ")
  }
}
