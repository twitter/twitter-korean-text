package com.twitter.penguin.korean.util

import com.twitter.penguin.korean.tokenizer.KoreanTokenizer.KoreanToken
import com.twitter.penguin.korean.util.Hangul._
import com.twitter.penguin.korean.util.KoreanDictionaryProvider._
import com.twitter.penguin.korean.util.KoreanPos._

/**
 * Helper methods for Korean nouns and josas.
 */
object KoreanSubstantive {

  val JOSA_HEAD_FOR_CODA: Set[Char] = Set('은', '이', '을', '과', '아')

  val JOSA_HEAD_FOR_NO_CODA: Set[Char] = Set('는', '가', '를', '와', '야', '여')


  protected[korean] def isJosaAttachable(prevChar: Char, headChar: Char): Boolean = {
    (hasCoda(prevChar) && !JOSA_HEAD_FOR_NO_CODA.contains(headChar)) ||
        (!hasCoda(prevChar) && !JOSA_HEAD_FOR_CODA.contains(headChar))
  }

  protected[korean] def isName(chunk: CharSequence): Boolean = {
    if (chunk.length() != 3) return false
    nameDictionay('family_name).contains(chunk.charAt(0).toString) &&
        nameDictionay('given_name).contains(chunk.subSequence(1, 3).toString)
  }

  val NUMBER_CHARS = "일이삼사오육칠팔구천백십해경조억만".map(_.toInt).toSet
  val NUMBER_LAST_CHARS = "일이삼사오육칠팔구천백십해경조억만원배분초".map(_.toInt).toSet

  protected[korean] def isKoreanNumber(chunk: CharSequence): Boolean =
    (0 to chunk.length() - 1).foldLeft(true) {
      case (output, i) if i < chunk.length() - 1 => output && NUMBER_CHARS.contains(chunk.charAt(i).toInt)
      case (output, i) => output && NUMBER_LAST_CHARS.contains(chunk.charAt(i).toInt)
    }


  /**
   * Collapse all the one-char nouns into one unknown noun
   *
   * @param posNodes sequence of KoreanTokens
   * @return sequence of collapsed KoreanTokens
   */
  protected[korean] def collapseNouns(posNodes: Seq[KoreanToken]): Seq[KoreanToken] = {
    val (nodes, collapsing) = posNodes.foldLeft((List[KoreanToken](), false)) {
      case ((pl: List[KoreanToken], collapsing: Boolean), p: KoreanToken)
        if p.pos == Noun && p.text.length == 1 && collapsing =>
        (KoreanToken(pl.head.text + p.text, Noun, unknown = true) :: pl.tail, true)
      case ((pl: List[KoreanToken], collapsing: Boolean), p: KoreanToken)
        if p.pos == Noun && p.text.length == 1 && !collapsing =>
        (p :: pl, true)
      case ((pl: List[KoreanToken], collapsing: Boolean), p: KoreanToken) =>
        (p :: pl, false)
    }
    nodes.reverse.toSeq
  }

}
