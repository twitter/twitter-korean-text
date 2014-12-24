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

package com.twitter.penguin.korean.v1.qa

import java.util.logging.{Level, Logger}

import com.twitter.penguin.korean.v1.TwitterKoreanProcessor

import scala.io.Source

/**
 * Batch tokenize a file of Korean tweets.
 *
 * usage: ./pants goal run src/scala/com/twitter/penguin/korean/qa:batch_tokenize_tweets
 * --jvm-run-args="~/korean_201407_100"
 */
object BatchTokenizeTweets {

  case class ParseTime(time: Long, chunk: String)

  val LOG = Logger.getLogger(getClass.getSimpleName)
  val VERBOSE = true

  def main(args: Array[String]) {
    if (args.length != 1) {
      println("The first arg should be an input file of Korean tweets.")
      return
    }
    val parseTimesAll = Source.fromFile(args(0)).getLines().foldLeft(List[ParseTime]()) {
      case (l: List[ParseTime], line: String) if line.trim.length > 5 =>
        if (VERBOSE) println(line.trim)

        val t0 = System.currentTimeMillis()
        val parsed = TwitterKoreanProcessor.tokenize(line)
        val t1 = System.currentTimeMillis()

        if (VERBOSE) {
          println(parsed.map {
            case t if t.unknown => t.text.toString + t.pos + "*"
            case t => t.text + t.pos.toString
          }.mkString(" "))

          println()
        }
        ParseTime(t1 - t0, line.trim) :: l
      case (l: List[ParseTime], line: String) => l
    }

    val loadingTime = parseTimesAll.last

    LOG.log(Level.INFO, "The first one \"%s\" took %d ms including the loading time.".format(loadingTime.chunk, loadingTime.time))

    val parseTimes = parseTimesAll.init

    val averageTweetLength = parseTimes.map(_.chunk.length).sum.toDouble / parseTimes.size

    val averageTime = parseTimes.map(_.time).sum.toDouble / parseTimes.size
    val maxItem = parseTimes.maxBy(_.time)

    LOG.log(Level.INFO, ("Parsed %d items. \n" +
        "       Total time: %d s \n" +
        "       Average tweet length: %.2f chars \n" +
        "       Average time per tweet: %.2f ms \n" +
        "       Max time: %d ms, %s\n" +
        "       Parsed: %s"
        ).format(
          parseTimes.size,
          parseTimes.map(_.time).sum / 1000,
          averageTweetLength,
          averageTime,
          maxItem.time,
          maxItem.chunk,
          TwitterKoreanProcessor.tokenize(maxItem.chunk).map {
            case t if t.unknown => t.text.toString + t.pos + "*"
            case t => t.text + t.pos.toString
          }.mkString(" ")
        ))
  }
}
