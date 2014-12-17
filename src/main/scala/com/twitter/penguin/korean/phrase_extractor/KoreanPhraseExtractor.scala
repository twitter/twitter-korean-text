package com.twitter.penguin.korean.phrase_extractor

import com.twitter.penguin.korean.TwitterKoreanProcessor
import com.twitter.penguin.korean.tokenizer.KoreanTokenizer
import com.twitter.penguin.korean.tokenizer.KoreanTokenizer.KoreanToken
import com.twitter.penguin.korean.util.KoreanDictionaryProvider._
import com.twitter.penguin.korean.util.KoreanSubstantive._
import com.twitter.penguin.korean.util.{Hangul, KoreanPos}
import com.twitter.penguin.korean.util.KoreanPos._

import scala.collection.mutable

/**
 * KoreanPhraseExtractor extracts suitable phrases for trending topics.
 *
 * 1. Collapse sequence of POSes to phrase candidates (초 + 거대 + 기업 + 의 -> 초거대기업 + 의)
 * 2. Find suitable phrases
 */
object KoreanPhraseExtractor {
  private val minCharsPerPhraseChunkWithoutSpaces = 3
  private val minPhrasesPerPhraseChunk = 2

  private val maxPhrasesPerPhraseChunk = 8

  private val ModifyingPredicateEndings: Set[Char] = Set('ㄹ', 'ㄴ')
  private val PhraseTokens = Set(Noun, Conjunction, Space)
  private val ConjunctionJosa = Set("와", "과", "의")
  type KoreanPhraseChunk = Seq[KoreanPhrase]

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
  private val COLLAPSING_RULES = Map(
    // Substantive
    "D0p*N1s0" -> Noun,
    "n*a+n*" -> Noun,
    "n+" -> Noun,

    // Predicate 초기뻐하다, 와주세요, 초기뻤었고, 추첨하다, 구경하기힘들다, 기뻐하는, 기쁜, 추첨해서, 좋아하다, 걸려있을
    "v*V1r*e0" -> Verb,
    "v*J1r*e0" -> Adjective
  )

  private val COLLAPSE_TRIE = KoreanPos.getTrie(COLLAPSING_RULES)

  private val PHRASE_HEAD_TAIL_POSES = Set(Noun, Alpha, Number)

  private def trimPhraseChunk(phrases: KoreanPhraseChunk): KoreanPhraseChunk = {
    def trimNonNouns: Seq[KoreanPhrase] = {
      phrases
          .dropWhile(t => !PHRASE_HEAD_TAIL_POSES.contains(t.pos))
          .reverse
          .dropWhile(t => !PHRASE_HEAD_TAIL_POSES.contains(t.pos))
          .reverse
    }

    def trimSpacesFromPhrase(phrases: Seq[KoreanPhrase]): Seq[KoreanPhrase] = {
      phrases.zipWithIndex.map {
        case (phrase, i) if phrases.length == 1 =>
          KoreanPhrase(phrase.tokens
              .dropWhile(_.pos == Space)
              .reverse.dropWhile(_.pos == Space).reverse, phrase.pos)
        case (phrase, i) if i == 0 =>
          KoreanPhrase(phrase.tokens.dropWhile(_.pos == Space), phrase.pos)
        case (phrase, i) if i == phrases.length - 1 =>
          KoreanPhrase(phrase.tokens.reverse.dropWhile(_.pos == Space).reverse, phrase.pos)
        case (phrase, i) => phrase
      }
    }

    trimSpacesFromPhrase(trimNonNouns)
  }

  private def trimPhrase(phrase: KoreanPhrase): KoreanPhrase = {
    KoreanPhrase(phrase.tokens.dropWhile(_.pos == Space)
        .reverse.dropWhile(_.pos == Space).reverse, phrase.pos)
  }


  private def isProperPhraseChunk(phraseChunk: KoreanPhraseChunk): Boolean = {
    def notEndingInNonPhraseSuffix: Boolean = {
      val lastToken = phraseChunk.last.tokens.last
      !(lastToken.pos == Suffix && lastToken.text == "적")
    }

    def isRightLength: Boolean = {
      val phraseChunkWithoutSpaces: Seq[KoreanPhrase] = phraseChunk.filter(_.pos != Space)
      phraseChunkWithoutSpaces.length <= maxPhrasesPerPhraseChunk &&
          (phraseChunkWithoutSpaces.length >= minPhrasesPerPhraseChunk ||
              phraseChunkWithoutSpaces.map(_.getTextLength).sum >= minCharsPerPhraseChunkWithoutSpaces)
    }

    isRightLength && notEndingInNonPhraseSuffix
  }

  case class KoreanPhrase(tokens: Seq[KoreanToken], pos: KoreanPos = Noun) {
    override def toString(): String = {
      this.tokens.map(_.text).mkString("") + pos
    }

    def getTextLength = {
      this.tokens.map(_.text.length).sum
    }
  }

  protected[korean] def collapsePos(tokens: Seq[KoreanToken]): Seq[KoreanPhrase] = {
    case class PhraseBuffer(phrases: List[KoreanPhrase], curTrie: List[KoreanPosTrie], ending: Option[KoreanPos])

    def getTries(token: KoreanToken, trie: List[KoreanPosTrie]): (KoreanPosTrie, List[KoreanPosTrie]) = {
      val curTrie = trie.filter(_.curPos == token.pos).head
      val nextTrie = curTrie.nextTrie.map {
        case nt: KoreanPosTrie if nt == selfNode => curTrie
        case nt: KoreanPosTrie => nt
      }
      (curTrie, nextTrie)
    }

    def getInit(phraseBuffer: PhraseBuffer): List[KoreanPhrase] = {
      if (phraseBuffer.phrases.isEmpty) {
        List()
      } else {
        phraseBuffer.phrases.init
      }
    }

    tokens.foldLeft(PhraseBuffer(List[KoreanPhrase](), COLLAPSE_TRIE, None)) {
      case (output, token) if output.curTrie.exists(_.curPos == token.pos) =>
        val (ct, nt) = getTries(token, output.curTrie)

        if (output.phrases.isEmpty) {
          PhraseBuffer(
            List(KoreanPhrase(List(token), ct.ending.getOrElse(Noun))),
            nt, ct.ending
          )
        } else if (output.curTrie == COLLAPSE_TRIE) {
          PhraseBuffer(
            List(KoreanPhrase(List(token), ct.ending.getOrElse(Noun))),
            nt, ct.ending
          )
        } else {
          PhraseBuffer(
            getInit(output) :+
                KoreanPhrase(output.phrases.last.tokens :+ token, ct.ending.getOrElse(Noun)),
            nt, ct.ending
          )
        }
      case (output, token) if COLLAPSE_TRIE.exists(_.curPos == token.pos) =>
        val (ct, nt) = getTries(token, COLLAPSE_TRIE)

        PhraseBuffer(
          output.phrases :+ KoreanPhrase(List(token), ct.ending.getOrElse(Noun)),
          nt, ct.ending
        )
      case (output, token) =>
        PhraseBuffer(
          output.phrases :+ KoreanPhrase(List(token), token.pos),
          output.curTrie, output.ending
        )
    }.phrases
  }

  protected def getCandidatePhraseChunks(phrases: KoreanPhraseChunk): Seq[KoreanPhraseChunk] = {
    def isPhraseCandidate(phrase: KoreanPhrase): Boolean = {
      val trimmed = trimPhrase(phrase)

      // 하는, 할인된, 할인될
      def isModifyingPredicate: Boolean =
        (trimmed.pos == Verb || trimmed.pos == Adjective) &&
            ModifyingPredicateEndings.contains(Hangul.decomposeHangul(trimmed.tokens.last.text.last).coda)

      // 과, 와, 의
      def isConjuctionJosa: Boolean =
        trimmed.pos == Josa && ConjunctionJosa.contains(trimmed.tokens.last.text)

      def isAlphaNumeric: Boolean =
        trimmed.pos == Alpha || trimmed.pos == Number

      isAlphaNumeric || PhraseTokens.contains(phrase.pos) || isModifyingPredicate || isConjuctionJosa
    }

    def collapseNounPhrases(phrases: KoreanPhraseChunk): KoreanPhraseChunk = {
      val (output, buffer) = phrases.foldLeft((Seq[KoreanPhrase](), Seq[KoreanPhrase]())) {
        case ((output, buffer), phrase) if phrase.pos == Noun => (output, buffer :+ phrase)
        case ((output, buffer), phrase) =>
          val tempPhrases = if (buffer.length > 0) {
            Seq(KoreanPhrase(buffer.flatMap(_.tokens)), phrase)
          } else {
            Seq(phrase)
          }
          (output ++ tempPhrases, Seq[KoreanPhrase]())
      }
      if (buffer.length > 0) output :+ KoreanPhrase(buffer.flatMap(_.tokens)) else output
    }

    def collapsePhrases(phrases: KoreanPhraseChunk): Seq[KoreanPhraseChunk] = {
      val (output, buffer) = phrases.foldLeft((Seq[KoreanPhraseChunk](), Seq[KoreanPhrase]())) {
        case ((output, buffer), phrase) if isPhraseCandidate(phrase) =>
          (output, buffer :+ phrase)
        case ((output, buffer), phrase) if buffer.length > 0 =>
          (output :+ buffer, Seq[KoreanPhrase]())
        case ((output, buffer), phrase) => (output, buffer)
      }
      if (buffer.length > 0) output :+ buffer else output
    }

    def getSingleTokenNouns: Seq[KoreanPhraseChunk] = {
      phrases.filter {
        phrase =>
          val trimmed = trimPhrase(phrase)
          phrase.pos == Noun &&
              (trimmed.getTextLength >= minCharsPerPhraseChunkWithoutSpaces ||
                  trimmed.tokens.length >= minPhrasesPerPhraseChunk)
      }.map(phrase => Seq(trimPhrase(phrase)))
    }

    val phraseCollapsed = collapsePhrases(
      collapseNounPhrases(phrases)
    )

    (phraseCollapsed.map(trimPhraseChunk) ++ getSingleTokenNouns).distinct
  }

  /**
   * Find suitable phrases
   *
   * @param tokens A sequence of tokens
   * @return A list of KoreanPhrase
   */
  def extractPhrases(tokens: Seq[KoreanToken]): Seq[KoreanPhrase] = {

    val collapsed = collapsePos(tokens)
    val candidates = getCandidatePhraseChunks(collapsed)

    val permutatedCandidates = candidates.flatMap {
      case phrases if phrases.length > minPhrasesPerPhraseChunk =>
        (0 to phrases.length - minPhrasesPerPhraseChunk).map {
          i => trimPhraseChunk(phrases.slice(i, phrases.length))
        }
      case phrases => Seq(phrases)
    }.filter { phraseChunk: KoreanPhraseChunk => isProperPhraseChunk(phraseChunk)
    }.distinct

    permutatedCandidates.map {
      phraseChunk: KoreanPhraseChunk => KoreanPhrase(trimPhraseChunk(phraseChunk).flatMap(_.tokens))
    }
  }


  /**
   * Extract phrarse from input CharSequence
   *
   * @param input Input CharSequence
   * @return Seq of phrase CharSequence
   */
  def extractPhrases(input: CharSequence): Seq[CharSequence] = {
    val tokens = TwitterKoreanProcessor.tokenize(
      input, stemming = false, keepSpace = true
    )
    extractPhrases(tokens).map {
      phrase => phrase.tokens.map(_.text).mkString("")
    }
  }

}
