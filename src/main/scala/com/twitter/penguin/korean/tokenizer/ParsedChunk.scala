package com.twitter.penguin.korean.tokenizer

import com.twitter.penguin.korean.tokenizer.KoreanTokenizer.KoreanToken
import com.twitter.penguin.korean.util.KoreanDictionaryProvider._
import com.twitter.penguin.korean.util.KoreanPos._


object ParsedChunk {
  val suffixes = Set(Suffix, Eomi, Josa, PreEomi)

  val preferredBeforeHaVerb = Set(Noun, ProperNoun, VerbPrefix)

  val defaultProfile: TokenizationProfile = TokenizationProfile()
}


// Lower score is better
case class TokenizationProfile(
    tokenCount: Float = 0.18f,
    unknown: Float = 0.3f,
    wordCount: Float = 0.3f,
    unknownCoverage: Float = 0.5f,
    freq: Float = 0.2f,
    unknownPosCount: Float = 10.0f,
    exactMatch: Float = 0.5f,
    allNoun: Float = 0.1f,
    preferredPattern: Float = 0.6f,
    determiner: Float = -0.01f,
    exclamation: Float = 0.01f,
    initialPostPosition: Float = 0.2f, // supress initial suffix when tied
    haVerb: Float = 0.3f,
    preferredPatterns: Seq[Seq[KoreanPos]] = Seq(Seq(Noun, Josa), Seq(ProperNoun, Josa)))


/**
  * A candidate parse for a chunk.
  *
  * @param posNodes Sequence of KoreanTokens.
  * @param words Number of words in this candidate parse.
  */
case class ParsedChunk(posNodes: Seq[KoreanToken], words: Int,
    profile: TokenizationProfile = TokenizationProfile.defaultProfile) {

  import ParsedChunk._

  lazy val score = countTokens * profile.tokenCount +
      countUnknowns * profile.unknown +
      words * profile.wordCount +
      getUnknownCoverage * profile.unknownCoverage +
      getFreqScore * profile.freq +
      countPos(Unknown) * profile.unknownPosCount +
      isExactMatch * profile.exactMatch +
      isAllNouns * profile.allNoun +
      isPreferredPattern * profile.preferredPattern +
      countPos(Determiner) * profile.determiner +
      countPos(Exclamation) * profile.exclamation +
      isInitialPostPosition * profile.initialPostPosition +
      isNounHa * profile.haVerb

  lazy val countUnknowns = this.posNodes.count { p: KoreanToken => p.unknown }
  lazy val countTokens = this.posNodes.size
  lazy val isInitialPostPosition = if (suffixes.contains(this.posNodes.head.pos)) 1 else 0
  lazy val isExactMatch = if (this.posNodes.size == 1) 0 else 1

  lazy val isAllNouns = if (this.posNodes.exists(t => t.pos != Noun && t.pos != ProperNoun)) 1
  else 0

  lazy val isPreferredPattern = if (
    posNodes.size == 2 && profile.preferredPatterns.contains(posNodes.map(_.pos))
  ) 0
  else 1

  lazy val isNounHa = if (this.posNodes.size >= 2
      && preferredBeforeHaVerb.contains(this.posNodes.head.pos)
      && this.posNodes(1).pos == Verb
      && this.posNodes(1).text.startsWith("하")) 0
  else 1

  lazy val posTieBreaker = this.posNodes.map(_.pos.id).sum

  lazy val getUnknownCoverage = this.posNodes.foldLeft(0) {
    case (sum, p: KoreanToken) => if (p.unknown) sum + p.text.length else sum
  }

  lazy val getFreqScore = this.posNodes.foldLeft(0f) {
    case (output: Float, p: KoreanToken) if p.pos == Noun || p.pos == ProperNoun =>
      output + (1f - koreanEntityFreq.getOrElse(p.text, 0f))
    case (output: Float, p: KoreanToken) => output + 1.0f
  } / this.posNodes.size

  def ++(that: ParsedChunk) = {
    ParsedChunk(this.posNodes ++ that.posNodes, this.words + that.words, profile)
  }

  def countPos(pos: KoreanPos) = this.posNodes.count { p: KoreanToken => p.pos == pos }
}