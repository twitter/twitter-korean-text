package com.twitter.penguin.korean.stemmer

import com.twitter.penguin.korean.tokenizer.KoreanTokenizer.KoreanToken
import com.twitter.penguin.korean.util.KoreanDictionaryProvider._
import com.twitter.penguin.korean.util.KoreanPos._

/**
 * Stems Adjectives and Verbs: 새로운 스테밍을 추가했었다. -> 새롭다 + 스테밍 + 을 + 추가하다
 */
object KoreanStemmer {
  private val Endings = Set(Eomi, PreEomi)
  private val Predicates = Set(Verb, Adjective)

  private val EndingsForNouns = Set("하다", "되다", "없다")

  /**
   * Removes Ending tokens recovering the root form of predicates
   *
   * @param tokens A sequence of tokens
   * @return A sequence of collapsed Korean tokens
   */
  def stem(tokens: Seq[KoreanToken]): Seq[KoreanToken] = {
    if (!tokens.exists(t => t.pos == Verb || t.pos == Adjective)) {
      return tokens
    }

    val stemmed = tokens.foldLeft(List[KoreanToken]()) {
      case (l: List[KoreanToken], token: KoreanToken) if l.nonEmpty && Endings.contains(token.pos) =>
        if (Predicates.contains(l.head.pos)) {
          val prevToken = l.head
          KoreanToken(
            prevToken.text,
            prevToken.pos, prevToken.offset, prevToken.length + token.length, prevToken.unknown
          ) :: l.tail
        } else {
          l
        }
      case (l: List[KoreanToken], token: KoreanToken) if Predicates.contains(token.pos) =>
        val text = predicateStems(token.pos)(token.text)
        KoreanToken(
          text,
          token.pos, token.offset, token.length, token.unknown
        ) :: l
      case (l: List[KoreanToken], token: KoreanToken) => token :: l
    }.reverse

    def validNounHeading(token: KoreanToken): Boolean = {
      val heading = token.text.take(token.text.length - 2)

      val validLength = token.text.length > 2
      val validPos = token.pos == Verb
      val validEndings = EndingsForNouns.contains(token.text.takeRight(2))
      val validNouns = koreanDictionary(Noun).contains(heading)

      validLength && validPos && validEndings && validNouns
    }

    stemmed.flatMap {
      case token if validNounHeading(token) =>
        val heading = token.text.take(token.text.length - 2)
        val ending = token.text.takeRight(2)

        Seq(
          KoreanToken(heading, Noun, token.offset, heading.length),
          KoreanToken(ending, token.pos, token.offset + heading.length, token.length - heading.length)
        )
      case token => Seq(token)
    }
  }
}
