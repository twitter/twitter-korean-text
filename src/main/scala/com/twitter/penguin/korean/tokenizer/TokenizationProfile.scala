package com.twitter.penguin.korean.tokenizer

import com.twitter.penguin.korean.util.KoreanPos._

object TokenizationProfile {
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

