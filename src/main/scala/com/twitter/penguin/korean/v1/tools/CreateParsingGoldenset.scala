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

package com.twitter.penguin.korean.v1.tools

import java.io.FileOutputStream
import java.util.zip.GZIPOutputStream

import com.twitter.penguin.korean.v1.TwitterKoreanProcessor
import TwitterKoreanProcessor._
import com.twitter.penguin.korean.thriftscala._
import com.twitter.penguin.korean.v1.util.KoreanDictionaryProvider._
import org.apache.thrift.protocol.TBinaryProtocol
import org.apache.thrift.transport.TIOStreamTransport

/**
 * Create Korean Parsing goldenset from the goldenset resource that contains goldenset chunks.
 * The first argument is a gzipped output file.
 */
object CreateParsingGoldenset {
  def main(args: Array[String]) {
    if (args.length != 1) {
      throw new IllegalArgumentException("Please specify an output file")
    }

    System.err.println("Reading the goldenset..")

    val parsed = readFileByLineFromResources("goldenset.txt").flatMap {
      case line if line.length > 0 =>
        val chunk = line.trim
        val parsed = tokenizeWithNormalization(chunk)
        Some(ParseItem(chunk, parsed.map(p => KoreanTokenThrift(p.text, p.pos.id, p.unknown))))
      case line => None
    }.toSeq


    val outputFile: String = args(0)

    System.err.println("Writing the new goldenset to " + outputFile)

    val out = new GZIPOutputStream(new FileOutputStream(outputFile))
    val binaryOut = new TBinaryProtocol(new TIOStreamTransport(out))
    ParsingGoldenset(parsed).write(binaryOut)
    out.close()

    System.err.println("Testing the new goldenset " + outputFile)

    val input = readGzipTBininaryFromFile(outputFile)
    val loaded = ParsingGoldenset.decode(input).goldenset

    assert(loaded.equals(parsed))

    System.err.println("Updated goldenset in " + outputFile)
  }
}
