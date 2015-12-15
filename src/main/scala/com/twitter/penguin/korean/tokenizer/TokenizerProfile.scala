package com.twitter.penguin.korean.tokenizer

import com.twitter.penguin.korean.util.KoreanPos._

object TokenizerProfile {
  val defaultProfile: TokenizerProfile = TokenizerProfile()
}

// Lower score is better
case class TokenizerProfile(
    tokenCount: Float = 0.18f,
    unknown: Float = 0.3f,
    wordCount: Float = 0.3f,
    freq: Float = 0.2f,
    unknownCoverage: Float = 0.5f,
    exactMatch: Float = 0.5f,
    allNoun: Float = 0.1f,
    unknownPosCount: Float = 10.0f,
    determinerPosCount: Float = -0.01f,
    exclamationPosCount: Float = 0.01f,
    initialPostPosition: Float = 0.2f,
    haVerb: Float = 0.3f,
    preferredPattern: Float = 0.6f,
    preferredPatterns: Seq[Seq[Any]] = Seq(Seq(Noun, Josa), Seq(ProperNoun, Josa)),
    spaceGuide: Set[Int] = Set[Int](),
    spaceGuidePenalty: Float = 3.0f
)
