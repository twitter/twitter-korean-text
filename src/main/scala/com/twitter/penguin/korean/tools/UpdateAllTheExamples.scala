package com.twitter.penguin.korean.tools

object UpdateAllTheExamples {
  def runTools(objects: Runnable*): Unit = {
    objects.foreach{o =>
      println("--- Running %s ---".format(o.getClass.getSimpleName.init))
      o.run
    }
    println("Finished running %s.".format(
      objects.map(_.getClass.getSimpleName).mkString(", ")
    ))
  }

  def main(args: Array[String]) {
    runTools(
      DeduplicateAndSortDictionaries,
      CreateConjugationExamples,
      CreateParsingExamples,
      CreatePhraseExtractionExamples
    )
  }
}
