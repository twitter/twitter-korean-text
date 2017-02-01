## open-korean-text-processor [![Coverage Status](https://coveralls.io/repos/github/openkoreantext/open-korean-text/badge.svg)](https://coveralls.io/github/openkoreantext/open-korean-text) [![Build Status](https://travis-ci.org/openkoreantext/open-korean-text.svg?branch=master)](https://travis-ci.org/openkoreantext/open-korean-text)

Open-source Korean Text Processor / 오픈소스 한국어 처리기 (Official Fork of twitter-korean-text)

Scala/Java library to process Korean text with a Java wrapper. open-korean-text currently provides Korean normalization and tokenization. Please join our community at [Google Forum](https://groups.google.com/forum/#!forum/open-korean-text). The intent of this text processor is not limited to short tweet texts.


스칼라로 쓰여진 한국어 처리기입니다. 현재 텍스트 정규화와 형태소 분석, 스테밍을 지원하고 있습니다. 짧은 트윗은 물론이고 긴 글도 처리할 수 있습니다. 개발에 참여하시고 싶은 분은 [Google Forum](https://groups.google.com/forum/#!forum/open-korean-text)에 가입해 주세요. 사용법을 알고자 하시는 초보부터 코드에 참여하고 싶으신 분들까지 모두 환영합니다.

open-korean-text의 목표는 빅데이터 등에서 간단한 한국어 처리를 통해 색인어를 추출하는 데에 있습니다. 완전한 수준의 형태소 분석을 지향하지는 않습니다.

open-korean-text는 normalization, tokenization, stemming, phrase extraction 이렇게 네가지 기능을 지원합니다.


**정규화 normalization (입니닼ㅋㅋ -> 입니다 ㅋㅋ, 샤릉해 -> 사랑해)**

* 한국어를 처리하는 예시입니닼ㅋㅋㅋㅋㅋ -> 한국어를 처리하는 예시입니다 ㅋㅋ

**토큰화 tokenization**

* 한국어를 처리하는 예시입니다 ㅋㅋ -> 한국어Noun, 를Josa, 처리Noun, 하는Verb, 예시Noun, 입Adjective, 니다Eomi ㅋㅋKoreanParticle

**어근화 stemming (입니다 -> 이다)**

* 한국어를 처리하는 예시입니다 ㅋㅋ -> 한국어Noun, 를Josa, 처리Noun, 하다Verb, 예시Noun, 이다Adjective, ㅋㅋKoreanParticle


**어구 추출 phrase extraction**

* 한국어를 처리하는 예시입니다 ㅋㅋ -> 한국어, 처리, 예시, 처리하는 예시

Introductory Presentation: [Google Slides](https://docs.google.com/presentation/d/10CZj8ry03oCk_Jqw879HFELzOLjJZ0EOi4KJbtRSIeU/)

## API
* [Scala Doc](https://openkoreantext.github.io/open-korean-text/scaladocs/org/openkoreantext/processor/index.html)

* [Maven Doc](https://openkoreantext.github.io/open-korean-text/index.html)

<!-- ## Try it here -->

<!-- Gunja Agrawal kindly created a test API webpage for this project: [http://gunjaagrawal.com/langhack/](http://gunjaagrawal.com/langhack/) -->

<!-- Gunja Agrawal님이 만들어주신 테스트 웹 페이지 입니다. -->
<!-- [http://gunjaagrawal.com/langhack/](http://gunjaagrawal.com/langhack/) -->

<!-- Opensourced here: [twitter-korean-tokenizer-api](https://github.com/gunjaag/twitter-korean-tokenizer-api) -->

## Maven
To include this in your Maven-based JVM project, add the following lines to your pom.xml:
/ Maven을 이용할 경우 pom.xml에 다음의 내용을 추가하시면 됩니다:

```xml
  <dependency>
    <groupId>org.openkoreantext</groupId>
    <artifactId>open-korean-text-processor</artifactId>
    <version>1.0</version>
  </dependency>
```

<!-- The maven site is available here http://twitter.github.io/open-korean-text/ and scaladocs are here http://twitter.github.io/open-korean-text/scaladocs/ -->

## Support for other languages.

### Wrappers

* [.net/C#](https://github.com/openkoreantext/open-korean-text-wrapper-csharp) by [modamoda](https://github.com/modamoda)

* [Node JS](https://github.com/openkoreantext/open-korean-text-wrapper-node-1) by [Ch0p](https://github.com/Ch0p)

* [Node JS](https://github.com/openkoreantext/open-korean-text-wrapper-node-2) by [Youngrok Kim](https://github.com/rokoroku)

* [Python](https://github.com/openkoreantext/open-korean-text-wrapper-python) by [Jaepil Jeong](https://github.com/jaepil)

* [Ruby for Java Version](https://github.com/openkoreantext/open-korean-text-wrapper-ruby-1) by [jun85664396](https://github.com/jun85664396)

* [Ruby for Scala Version](https://github.com/openkoreantext/open-korean-text-wrapper-ruby-2) by [Jaehyun Shin](https://github.com/keepcosmos)


### Portings

* [Python](https://github.com/openkoreantext/open-korean-text-python) by [Baeg-il Kim](https://github.com/cedar101)


### Packages

* [Python Korean NLP](https://github.com/konlpy/konlpy) by [KoNLPy](https://github.com/konlpy/konlpy)


### Elastic Search

* [tkt-elasticsearch](https://github.com/openkoreantext/open-korean-text-elastic-search) by [socurites](https://github.com/socurites)


## Get the source / 소스를 원하시는 경우

Clone the git repo and build using maven.
/ Git 전체를 클론하고 Maven을 이용하여 빌드합니다.

```bash
git clone https://github.com/openkoreantext/open-korean-text.git
cd open-korean-text
mvn compile
```

Open 'pom.xml' from your favorite IDE.

## Basic Usage / 사용 방법

You can find these [examples](examples) in examples folder.
/ [examples](examples) 폴더에 사용 방법 예제 파일이 있습니다.

* [Scala Example](examples/src/main/scala/ScalaOpenKoreanTextExample.scala)

* [Java Example](examples/src/main/java/JavaOpenKoreanTextProcessorExample.java)


## Running Tests

`mvn test` will run our unit tests
/ 모든 유닛 테스트를 실행하려면 `mvn test`를 이용해 주세요.


<!-- ## Tools -->

<!-- We provide tools for quality assurance and test resources. They can be found under [src/main/scala/com/twitter/penguin/korean/qa](src/main/scala/com/twitter/penguin/korean/qa) and [src/main/scala/com/twitter/penguin/korean/tools](src/main/scala/com/twitter/penguin/korean/tools). -->


## Contribution

Refer to the [general contribution guide](CONTRIBUTING.md). We will add this project-specific contribution guide later.

[설치 및 수정하는 방법 상세 안내](docs/contribution-guide.md)


## Performance / 처리 속도

Tested on Intel i7 2.3 Ghz

Initial loading time (초기 로딩 시간): 2~4 sec

Average time per parsing a chunk (평균 어절 처리 시간): 0.12 ms


**Tweets (Avg length ~50 chars)**

Tweets|100K|200K|300K|400K|500K|600K|700K|800K|900K|1M
---|---|---|---|---|---|---|---|---|---|---
Time in Seconds|57.59|112.09|165.05|218.11|270.54|328.52|381.09|439.71|492.94|542.12
Average per tweet: 0.54212 ms

**Benchmark test by [KoNLPy](http://konlpy.org/)**

![Benchmark test](http://konlpy.org/ko/v0.4.2/_images/time.png)

From [http://konlpy.org/ko/v0.4.2/morph/](http://konlpy.org/ko/v0.4.2/morph/)


## Author

* Will Hohyon Ryu (유호현): https://github.com/nlpenguin | https://twitter.com/NLPenguin

## Admin Staff

* Mingyu Kim (김민규): https://github.com/MechanicKim

## License

Copyright 2014 Twitter, Inc.

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
