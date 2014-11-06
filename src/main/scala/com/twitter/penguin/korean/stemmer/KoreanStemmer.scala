package com.twitter.penguin.korean.stemmer

import com.twitter.penguin.korean.TwitterKoreanProcessor.KoreanSegment
import com.twitter.penguin.korean.tokenizer.KoreanTokenizer
import com.twitter.penguin.korean.tokenizer.KoreanTokenizer.KoreanToken
import com.twitter.penguin.korean.util.{KoreanDictionaryProvider, KoreanPos}

/**
 * Stems Adjectives and Verbs: 새로운 스테밍을 추가했었다. -> 새롭다 + 스테밍 + 을 + 추가하다
 */
object KoreanStemmer {
  val Endings = Set(KoreanPos.Eomi, KoreanPos.PreEomi)
  val Predicates = Set(KoreanPos.Verb, KoreanPos.Adjective)

  case class StemmedTextWithTokens(text: CharSequence, tokens: Seq[KoreanToken])

  def stemPredicates(tokens: Seq[KoreanToken]): Seq[Option[KoreanToken]] = {
    tokens.map {
      case token: KoreanToken if Endings.contains(token.pos) => None
      case token: KoreanToken if Predicates.contains(token.pos) =>
        Some(
          KoreanToken(
            KoreanDictionaryProvider.predicateStems(token.pos)(token.text),
            token.pos, token.unknown
          )
        )
      case token => Some(token)
    }
  }
  
  def stem(text: CharSequence): StemmedTextWithTokens = {
    val tokenized = KoreanTokenizer.tokenize(text)
    val stemmed = stemPredicates(tokenized)

    val mapped = tokenized.zip(stemmed)

    val s = text.toString
    // transform the original text to the stemmed text
    val (sb, i) = mapped.foldLeft(new StringBuilder(), 0) {
      case ((sb: StringBuilder, i: Int), (token: KoreanToken, stemmedToken: Option[KoreanToken])) =>
        val segStart = s.indexOf(token.text, i)
        sb.append(s.substring(i, segStart))
        if (stemmedToken.isDefined) {
          sb.append(stemmedToken.get.text)
        }
        (sb, segStart + token.text.length)
    }
    sb.append(s.substring(i, text.length))

    StemmedTextWithTokens(sb, stemmed.flatten)
  } 
}
