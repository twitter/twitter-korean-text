/*
 * Twitter Korean Text - Scala library to process Korean text
 *
 * Copyright 2014 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
 * Program arguments example = "~/korean_201407_100"
 */
object BatchGetUnknownNouns {
  private val LOG = Logger.getLogger(getClass.getSimpleName)
  private val VERBOSE = true

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
