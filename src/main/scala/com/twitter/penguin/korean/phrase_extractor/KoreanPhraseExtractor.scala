package com.twitter.penguin.korean.phrase_extractor

import com.twitter.penguin.korean.tokenizer.KoreanTokenizer.KoreanToken
import com.twitter.penguin.korean.util.KoreanPos
import com.twitter.penguin.korean.util.KoreanPos._

/**
 * KoreanPhraseExtractor extracts suitable phrases for trending topics.
 *
 * 1. Collapse sequence of POSes to phrase candidates (초 + 거대 + 기업 + 의 -> 초거대기업 + 의)
 * 2. Find suitable phrases
 */
object KoreanPhraseExtractor {

  /**
   * 0 for optional, 1 for required
   * * for optional repeatable, + for required repeatable
   *
   * Substantive: 체언 (초거대기업의)
   * Predicate: 용언 (하였었습니다, 개예뻤었다)
   * Modifier: 수식언 (모르는 할수도있는 보이기도하는 예뻐 예쁜 완전 레알 초인간적인 잘 잘한)
   * Standalone: 독립언
   * Functional: 관계언 (조사)
   *
   * N Noun: 명사 (Nouns, Pronouns, Company Names, Proper Noun, Person Names, Numerals, Standalone, Dependent)
   * V Verb: 동사 (하, 먹, 자, 차)
   * J Adjective: 형용사 (예쁘다, 크다, 작다)
   * A Adverb: 부사 (잘, 매우, 빨리, 반드시, 과연)
   * D Determiner: 관형사 (새, 헌, 참, 첫, 이, 그, 저)
   * E Exclamation: 감탄사 (헐, ㅋㅋㅋ, 어머나, 얼씨구)
   *
   * C Conjunction: 접속사
   *
   * j SubstantiveJosa: 조사 (의, 에, 에서)
   * l AdverbialJosa: 부사격 조사 (~인, ~의, ~일)
   * e Eomi: 어말어미 (다, 요, 여, 하댘ㅋㅋ)
   * r PreEomi: 선어말어미 (었)
   *
   * p NounPrefix: 접두사 ('초'대박)
   * v VerbPrefix: 동사 접두어 ('쳐'먹어)
   * s Suffix: 접미사 (~적)
   */
  val CollapsingRules = Map(
    // Substantive
    "D0p*N1s0" -> Noun,
    // Predicate 초기뻐하다, 와주세요, 초기뻤었고, 추첨하다, 구경하기힘들다, 기뻐하는, 기쁜, 추첨해서, 좋아하다, 걸려있을
    "v*V1r*e0" -> Verb,
    "v*J1r*e0" -> Adjective,
    // Standalone
    "A1" -> Adverb,
    "j1" -> Josa,
    "C1" -> Conjunction,
    "E+" -> Exclamation,
    "o1" -> Others
  )

  val collapseTrie = KoreanPos.getTrie(CollapsingRules)

  case class KoreanPhrase(tokens: Seq[KoreanToken], pos: KoreanPos)

  def collapsePos(tokens: Seq[KoreanToken],
                  trie: List[KoreanPosTrie] = collapseTrie,
                  finalTokens: Seq[KoreanPhrase] = Seq(),
                  curTokens: Seq[KoreanToken] = Seq(),
                  ending: Option[KoreanPos] = None)
  : Seq[KoreanPhrase] = {
    if (tokens.length == 0) {
      return finalTokens
    }

    val h = tokens.head

    val newSeq = if (ending.isDefined) {
      collapsePos(
        tokens,
        finalTokens = finalTokens :+ KoreanPhrase(curTokens, ending.get),
        curTokens = Seq()
      )
    } else Seq()

    val output = trie.flatMap {
      case t: KoreanPosTrie if t.curPos == h.pos =>
        collapsePos(tokens.tail, t.nextTrie, finalTokens, curTokens :+ h, t.ending)
      case t: KoreanPosTrie if t.curPos == Others && OtherPoses.contains(h.pos) =>
        collapsePos(tokens.tail, finalTokens = finalTokens :+ KoreanPhrase(Seq(h), h.pos))
      case t: KoreanPosTrie => Seq()
    }

    newSeq ++ output
  }

  /**
   * Find suitable phrases
   *
   * @param tokens A sequence of tokens
   * @return A list of Korean tokens
   */
  //  def extractPhrases(tokens: Seq[KoreanToken]): Seq[Seq[KoreanToken]] = {
  //
  //  }
}
