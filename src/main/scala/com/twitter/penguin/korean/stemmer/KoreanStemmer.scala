package com.twitter.penguin.korean.stemmer

import com.twitter.penguin.korean.tokenizer.KoreanTokenizer
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

  case class StemmedTextWithTokens(text: CharSequence, tokens: Seq[KoreanToken])

  /**
   * Removes Ending tokens recovering the root form of predicates
   *
   * @param tokens A sequence of tokens
   * @return A list of Korean tokens
   */
  def stemPredicatesCore(tokens: Seq[KoreanToken]): Seq[Seq[KoreanToken]] = {
    val stemmed = tokens.map {
      case token: KoreanToken if Endings.contains(token.pos) => None
      case token: KoreanToken if Predicates.contains(token.pos) =>
        Some(
          KoreanToken(
            predicateStems(token.pos)(token.text),

            token.pos, token.unknown
          )
        )
      case token => Some(token)
    }

    def validNounHeading(token: KoreanToken): Boolean = {
      val heading = token.text.take(token.text.length - 2)
      
      val validLength = token.text.length > 2
      val validPos = token.pos == Verb
      val validEndings = EndingsForNouns.contains(token.text.takeRight(2))
      val validNouns = koreanDictionary(Noun).contains(heading)

      validLength && validPos && validEndings && validNouns 
    }
    
    stemmed.map {
      case Some(token) if validNounHeading(token) =>
        val heading = token.text.take(token.text.length - 2)
        val ending = token.text.takeRight(2)

        Seq(
          KoreanToken(heading, Noun),
          KoreanToken(ending, token.pos)
        )
      case Some(token) => Seq(token)
      case None => Seq()
    }
  }

  /**
   * Removes Ending tokens recovering the root form of predicates
   *
   * @param tokens A sequence of tokens
   * @return A list of Korean tokens
   */
  def stemPredicates(tokens: Seq[KoreanToken]): Seq[KoreanToken] = {
    stemPredicatesCore(tokens).flatMap(c => c)
  }

  /**
   * Stem tokens
   *
   * @param text Input text
   * @return StemmedTextWithTokens
   */
  def stem(text: CharSequence): StemmedTextWithTokens = {
    val tokenized = KoreanTokenizer.tokenize(text)

    val stemmed = stemPredicatesCore(tokenized)
    val mapped = tokenized.zip(stemmed)

    val s = text.toString
    // transform the original text to the stemmed text
    val (sb, i) = mapped.foldLeft(new StringBuilder(), 0) {
      case ((sb: StringBuilder, i: Int), (token: KoreanToken, stemmedTokens: Seq[KoreanToken])) =>
        val segStart = s.indexOf(token.text, i)
        sb.append(s.substring(i, segStart))
        sb.append(stemmedTokens.map(_.text).mkString(""))
        (sb, segStart + token.text.length)
    }
    sb.append(s.substring(i, text.length))

    StemmedTextWithTokens(sb, stemmed.flatten)
  }
}
