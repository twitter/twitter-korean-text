

## twitter-korean-text [![Build Status](https://secure.travis-ci.org/twitter/twitter-korean-text.png?branch=master)](http://travis-ci.org/twitter/twitter-korean-text)

Scala library to process Korean text. twitter-korean-text currently provides Korean normalization and tokenization. 

### Maven
To include this in your Maven-based JVM project, add the following lines to your pom.xml:

```xml
  <dependency>
    <groupId>com.twitter.penguin</groupId>
    <artifactId>korean-text</artifactId>
    <version>1.0-SNAPSHOT</version>
  <dependency>
```

## Get the source

Clone the git repo and build using maven.
```bash
git clone https://github.com/twitter/twitter-korean-text.git
cd twitter-korean-text
mvn compile
```

Open 'pom.xml' from your favorite IDE.

## Usage

You can find these [examples](examples) in examples folder.

from Scala
```scala
import com.twitter.penguin.korean.tokenizer.KoreanTokenizer
import com.twitter.penguin.korean.TwitterKoreanProcessor

object ScalaTwitterKoreanTextExample {
  def main(args: Array[String]) {
    // Tokenize into List<String>
    val parsed: Seq[String] = TwitterKoreanProcessor.tokenizeToNormalizedStrings("한국어를 처리하는 예시입니닼ㅋㅋㅋㅋㅋ")
    println(parsed) // ArraySeq(한국어, 를, 처리, 하는, 예시, 입, 니다, ㅋㅋ)

    // Tokenize with Part-of-Speech information
    val parsedPos: Seq[KoreanTokenizer.KoreanToken] = TwitterKoreanProcessor.tokenizeWithNormalization("한국어를 처리하는 예시입니닼ㅋㅋㅋㅋㅋ")
    println(parsedPos) // ArraySeq(한국어Noun, 를Josa, 처리Noun, 하는Verb, 예시Noun, 입Verb, 니다Eomi, ㅋㅋKoreanParticle)
  }
}
```

from Java
```java
import java.util.List;

import scala.collection.JavaConversions;
import scala.collection.Seq;

import com.twitter.penguin.korean.TwitterKoreanProcessor;
import com.twitter.penguin.korean.tokenizer.KoreanTokenizer;

public class JavaTwitterKoreanTextExample {
  public static void main(String[] args) {
    // Tokenize into List<String>
    Seq<String> parsed = TwitterKoreanProcessor.tokenizeToNormalizedStrings("한국어를 처리하는 예시입니닼ㅋㅋㅋㅋㅋ");
    List<String> javaParsed = JavaConversions.seqAsJavaList(parsed);
    System.out.println(javaParsed);  // [한국어, 를, 처리, 하는, 예시, 입, 니다, ㅋㅋ]

    // Tokenize with Part-of-Speech information
    Seq<KoreanTokenizer.KoreanToken> parsedPos = TwitterKoreanProcessor.tokenizeWithNormalization("한국어를 처리하는 예시입니닼ㅋㅋㅋㅋㅋ");
    List<KoreanTokenizer.KoreanToken> javaParsedPos = JavaConversions.seqAsJavaList(parsedPos);
    System.out.println(javaParsedPos);  // [한국어Noun, 를Josa, 처리Noun, 하는Verb, 예시Noun, 입Verb, 니다Eomi, ㅋㅋKoreanParticle]
  }
}
```


### Basics

TwitterKoreanProcessor.scala is the central object that provides interface for all the features.

## Running Tests

`mvn test` will run our unit tests

## Tools

We provide tools for quality assurance and test resources. They can be found under [src/main/scala/com/twitter/penguin/korean/qa](src/main/scala/com/twitter/penguin/korean/qa) and [src/main/scala/com/twitter/penguin/korean/tools](src/main/scala/com/twitter/penguin/korean/tools).
 

## Author(s)

* Hohyon "Will" Ryu: https://github.com/softbass https://twitter.com/NLPenguin

## License

Copyright 2014 Twitter, Inc.

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
