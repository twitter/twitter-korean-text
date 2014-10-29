package com.twitter.penguin.korean.qa

import java.util.logging.Logger

import com.twitter.penguin.korean.TwitterKoreanProcessor
import com.twitter.penguin.korean.tokenizer.KoreanChunker._
import com.twitter.penguin.korean.tokenizer.KoreanTokenizer._
import com.twitter.penguin.korean.util.KoreanPos

import scala.io.Source

/**
 * Find unknown nouns from a file of tweets.
 *
 * usage: ./pants goal run src/scala/com/twitter/penguin/korean/qa:batch_get_unknown_nouns
 * --jvm-run-args="~/korean_201407_100"
 */
object BatchGetUnknownNouns {
  val LOG = Logger.getLogger(getClass.getSimpleName)
  val VERBOSE = true

  case class ChunkWithTweet(chunk: String, tweet: String)

  def main(args: Array[String]) {
    if (args.length != 1) {
      println("The first arg should be an input file path of Korean tweets.")
      return
    }
    val chunksWithUnknowns = Source.fromFile(args(0)).getLines().foldLeft(List[ChunkWithTweet]()) {
      case (l: List[ChunkWithTweet], line: String) if line.trim.length > 5 =>
        chunk(line).flatMap {
          case t: KoreanToken if t.pos == KoreanPos.Korean && tokenize(t.text).exists(_.unknown) =>
            Some(ChunkWithTweet(t.text, line.trim))
          case t: KoreanToken => None
        }.toList ::: l
      case (l: List[ChunkWithTweet], line: String) => l
    }.toSet

    chunksWithUnknowns.toSeq.sortBy(_.chunk).foreach {
      chunkWithTweet: ChunkWithTweet =>
        println(chunkWithTweet.tweet)
        println(TwitterKoreanProcessor
            .tokenize(chunkWithTweet.tweet)
            .mkString(" "))

        println(chunkWithTweet.chunk + ": " +
            tokenize(chunkWithTweet.chunk).mkString(" "))
        println()
    }

  }
}
