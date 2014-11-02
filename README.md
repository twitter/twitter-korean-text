

## twitter-korean-text [![Build Status](https://secure.travis-ci.org/twitter/twitter-korean-text.png?branch=master)](http://travis-ci.org/twitter/twitter-korean-text)
트위터 한국어 분석기

Scala library to process Korean text. twitter-korean-text currently provides Korean normalization and tokenization. 

스칼라로 쓰여진 한국어 분석기입니다. 현재 텍스트 정규화와 형태소 분석을 지원하고 있습니다. 본 프로젝트는 아직 기초적인 단계에 있습니다. 참여하시고 싶은 분은 hohyonryu@twitter.com으로 연락 부탁드립니다. 

### Try here

Gunja Agrawal kindly created a test API webpage for this project: [http://gunjaagrawal.com/langhack/](http://gunjaagrawal.com/langhack/)


### Maven
To include this in your Maven-based JVM project, add the following lines to your pom.xml:

Maven을 이용할 경우 pom.xml에 다음의 내용을 추가하시면 됩니다:

```xml
  <dependency>
    <groupId>com.twitter.penguin</groupId>
    <artifactId>korean-text</artifactId>
    <version>1.0</version>
  <dependency>
```

The maven site is available here http://twitter.github.io/twitter-korean-text/ and scaladocs are here http://twitter.github.io/twitter-korean-text/scaladocs/

## Get the source 소스를 원하시는 경우

Clone the git repo and build using maven.

Git 전체를 클론하고 Maven을 이용하여 빌드합니다.

```bash
git clone https://github.com/twitter/twitter-korean-text.git
cd twitter-korean-text
mvn compile
```

Open 'pom.xml' from your favorite IDE.

## Usage 사용 방법

You can find these [examples](examples) in examples folder.

[examples](examples) 폴더에 사용 방법 예제 파일이 있습니다. 

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

[TwitterKoreanProcessor.scala](src/main/scala/com/twitter/penguin/korean/TwitterKoreanProcessor.scala) is the central object that provides interface for all the features.

[TwitterKoreanProcessor.scala](src/main/scala/com/twitter/penguin/korean/TwitterKoreanProcessor.scala)에 지원하는 모든 기능을 모아 두었습니다. 

## Running Tests

`mvn test` will run our unit tests

## Tools

We provide tools for quality assurance and test resources. They can be found under [src/main/scala/com/twitter/penguin/korean/qa](src/main/scala/com/twitter/penguin/korean/qa) and [src/main/scala/com/twitter/penguin/korean/tools](src/main/scala/com/twitter/penguin/korean/tools).
 
## Contribution

Refer to the [general contribution guide](CONTRIBUTING.md). We will add this project-specific contribution guide later.

## Author(s)

* Will Hohyon Ryu (유호현): https://github.com/nlpenguin | https://twitter.com/NLPenguin

## License

Copyright 2014 Twitter, Inc.

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
