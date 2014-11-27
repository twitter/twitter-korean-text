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

import com.twitter.penguin.korean.thriftscala.{ConjugationGoldenset, ConjugationItem}
import com.twitter.penguin.korean.v1.util.KoreanConjugation._
import com.twitter.penguin.korean.v1.util.KoreanDictionaryProvider._
import org.apache.thrift.protocol.TBinaryProtocol
import org.apache.thrift.transport.TIOStreamTransport

import scala.collection.JavaConversions._

/**
 * Create Korean predicate expansion goldenset from adjective and verb resources.
 * The first argument is the test resource directory.
 *
 * usage: ./pants goal run src/scala/com/twitter/penguin/korean/tools:create_conjugation_goldenset
 * --jvm-run-args="/Users/hohyonryu/workspace/penguin-binaries/tests/com/twitter/penguin/korean"
 */
object CreateConjugationGoldenset {
  def main(args: Array[String]) {
    if (args.length != 1) {
      throw new IllegalArgumentException("Please specify an output resource directory..")
    }

    System.err.println("Reading the verbs and adjectives..")

    def updateConjugateGoldenset(file: String, isAdj: Boolean, outputFileName: String) {
      System.err.println("Writing the expansion goldenset in " + outputFileName)

      val outputPath = args(0) + "/" + outputFileName
      val out = new GZIPOutputStream(new FileOutputStream(outputPath))

      val binaryOut = new TBinaryProtocol(new TIOStreamTransport(out));

      val words = readWordsAsSeq(file)
      val goldenset = words.map(word =>
        ConjugationItem(word, conjugatePredicates(Set(word), isAdj).map {
          case charArray: Array[Char] => new String(charArray)
        }.toSet)
      )

      ConjugationGoldenset(goldenset).write(binaryOut)

      out.close()

      System.err.println("Testing the expansion goldenset in " + outputPath)

      val input = readGzipTBininaryFromFile(outputPath)
      val loaded = ConjugationGoldenset.decode(input).goldenset

      assert(loaded.equals(goldenset))
    }

    updateConjugateGoldenset("adjective/adjective.txt", isAdj = true, "adj_conjugate.gz")
    updateConjugateGoldenset("verb/verb.txt", isAdj = false, "verb_conjugate.gz")
  }
}
