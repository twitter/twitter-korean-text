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

package org.openkoreantext.processor.tokenizer

import com.twitter.Regex
import org.openkoreantext.processor.TestBase
import org.openkoreantext.processor.tokenizer.KoreanChunker._
import org.openkoreantext.processor.util.KoreanPos._

class KoreanChunkerTest extends TestBase {
  
  test("findAllPatterns should correctly find all patterns") {
    val testStr = "한국어와 English와 1234와 pic.twitter.com " +
        "http://news.kukinews.com/article/view.asp?" +
        "page=1&gCode=soc&arcid=0008599913&code=41121111 " +
        "#Hashtag $hello_kr " +
        "hohyonryu@twitter.com @nlpenguin 갤럭시 S5 ㄱㄱ"
    
    assert(
      findAllPatterns(POS_PATTERNS(URL).matcher(testStr), URL).mkString("/")
        === "ChunkMatch(35,125, http://news.kukinews.com/article/view.asp?page=1&gCode=soc&arcid=0008599913&code=41121111,URL)/" +
            "ChunkMatch(19,35, pic.twitter.com,URL)"
    )
    
    assert(
      findAllPatterns(POS_PATTERNS(Email).matcher(testStr), Email).mkString("/")
        === "ChunkMatch(145,166,hohyonryu@twitter.com,Email)"
    )
    
    assert(
      findAllPatterns(POS_PATTERNS(ScreenName).matcher(testStr), ScreenName).mkString("/")
        === "ChunkMatch(166,177, @nlpenguin,ScreenName)"
    )
    
    assert(
      findAllPatterns(POS_PATTERNS(Hashtag).matcher(testStr), Hashtag).mkString("/")
        === "ChunkMatch(125,134, #Hashtag,Hashtag)"
    )
    
    assert(
      findAllPatterns(POS_PATTERNS(CashTag).matcher(testStr), CashTag).mkString("/")
        === "ChunkMatch(134,144, $hello_kr,CashTag)"
    )
    
    assert(
      findAllPatterns(POS_PATTERNS(Korean).matcher(testStr), Korean).mkString("/")
        === "ChunkMatch(178,181,갤럭시,Korean)/" +
            "ChunkMatch(18,19,와,Korean)/" +
            "ChunkMatch(12,13,와,Korean)/" +
            "ChunkMatch(0,4,한국어와,Korean)"
    )
    
    assert(
      findAllPatterns(POS_PATTERNS(KoreanParticle).matcher(testStr), KoreanParticle).mkString("/")
        === "ChunkMatch(185,187,ㄱㄱ,KoreanParticle)"
    )
    
    assert(
      findAllPatterns(POS_PATTERNS(Number).matcher(testStr), Number).mkString("/")
        === "ChunkMatch(183,184,5,Number)/" +
            "ChunkMatch(117,125,41121111,Number)/" +
            "ChunkMatch(101,111,0008599913,Number)/" +
            "ChunkMatch(83,84,1,Number)/" +
            "ChunkMatch(14,18,1234,Number)"
    )
    
    assert(
      findAllPatterns(POS_PATTERNS(Alpha).matcher(testStr), Alpha).mkString("/")
        === "ChunkMatch(182,183,S,Alpha)/" +
            "ChunkMatch(168,177,nlpenguin,Alpha)/" +
            "ChunkMatch(163,166,com,Alpha)/" +
            "ChunkMatch(155,162,twitter,Alpha)/" +
            "ChunkMatch(145,154,hohyonryu,Alpha)/" +
            "ChunkMatch(142,144,kr,Alpha)/" +
            "ChunkMatch(136,141,hello,Alpha)/" +
            "ChunkMatch(127,134,Hashtag,Alpha)/" +
            "ChunkMatch(112,116,code,Alpha)/" +
            "ChunkMatch(95,100,arcid,Alpha)/" +
            "ChunkMatch(91,94,soc,Alpha)/" +
            "ChunkMatch(85,90,gCode,Alpha)/" +
            "ChunkMatch(78,82,page,Alpha)/" +
            "ChunkMatch(74,77,asp,Alpha)/" +
            "ChunkMatch(69,73,view,Alpha)/" +
            "ChunkMatch(61,68,article,Alpha)/" +
            "ChunkMatch(57,60,com,Alpha)/" +
            "ChunkMatch(48,56,kukinews,Alpha)/" +
            "ChunkMatch(43,47,news,Alpha)/" +
            "ChunkMatch(36,40,http,Alpha)/" +
            "ChunkMatch(32,35,com,Alpha)/" +
            "ChunkMatch(24,31,twitter,Alpha)/" +
            "ChunkMatch(20,23,pic,Alpha)/" +
            "ChunkMatch(5,12,English,Alpha)"
    )
    
    assert(
      findAllPatterns(POS_PATTERNS(Punctuation).matcher(testStr), Punctuation).mkString("/")
        === "ChunkMatch(167,168,@,Punctuation)/" +
            "ChunkMatch(162,163,.,Punctuation)/" +
            "ChunkMatch(154,155,@,Punctuation)/" +
            "ChunkMatch(141,142,_,Punctuation)/" +
            "ChunkMatch(135,136,$,Punctuation)/" +
            "ChunkMatch(126,127,#,Punctuation)/" +
            "ChunkMatch(116,117,=,Punctuation)/" +
            "ChunkMatch(111,112,&,Punctuation)/" +
            "ChunkMatch(100,101,=,Punctuation)/" +
            "ChunkMatch(94,95,&,Punctuation)/" +
            "ChunkMatch(90,91,=,Punctuation)/" +
            "ChunkMatch(84,85,&,Punctuation)/" +
            "ChunkMatch(82,83,=,Punctuation)/" +
            "ChunkMatch(77,78,?,Punctuation)/" +
            "ChunkMatch(73,74,.,Punctuation)/" +
            "ChunkMatch(68,69,/,Punctuation)/" +
            "ChunkMatch(60,61,/,Punctuation)/" +
            "ChunkMatch(56,57,.,Punctuation)/" +
            "ChunkMatch(47,48,.,Punctuation)/" +
            "ChunkMatch(40,43,://,Punctuation)/" +
            "ChunkMatch(31,32,.,Punctuation)/" +
            "ChunkMatch(23,24,.,Punctuation)"
    )
  }

  test("getChunks should correctly split a string into Korean-sensitive chunks") {
    assert(
      getChunks("안녕? iphone6안녕? 세상아?").mkString("/")
        === "안녕/?/ /iphone/6/안녕/?/ /세상아/?"
    )

    assert(
      getChunks("This is an 한국어가 섞인 English tweet.").mkString("/")
        === "This/ /is/ /an/ /한국어가/ /섞인/ /English/ /tweet/."
    )

    assert(
      getChunks("이 日本것은 日本語Eng").mkString("/")
        === "이/ /日本/것은/ /日本語/Eng"
    )

    assert(
      getChunks("무효이며").mkString("/")
        === "무효이며"
    )

    assert(
      getChunks("#해쉬태그 이라는 것 #hash @hello 123 이런이런 #여자최애캐_5명으로_취향을_드러내자").mkString("/")
        === "#해쉬태그/ /이라는/ /것/ /#hash/ /@hello/ /123/ /이런이런/ /#여자최애캐_5명으로_취향을_드러내자"
    )
  }
  
  test("getChunksByPos should correctly extract chunks with a POS tag") {
    assert(
      getChunksByPos("openkoreantext.org에서 API를 테스트 할 수 있습니다.", URL).mkString("/")
        === "openkoreantext.org(URL: 0, 18)"
    )
    
    assert(
      getChunksByPos("메일 주소 mechanickim@openkoreantext.org로 문의주시거나", Email).mkString("/")
        === "mechanickim@openkoreantext.org(Email: 6, 30)"
    )
    
    assert(
      getChunksByPos("open-korean-text 프로젝트 마스터 @nlpenguin님께 메일주시면 됩니다. :-)", ScreenName).mkString("/")
        === "@nlpenguin(ScreenName: 26, 10)"
    )
    
    assert(
      getChunksByPos("해시태그는 이렇게 생겼습니다. #나는_해적왕이_될_사나이다", Hashtag).mkString("/")
        === "#나는_해적왕이_될_사나이다(Hashtag: 17, 15)"
    )
    
    assert(
      getChunksByPos("캐쉬태그는 이렇게 생겼습니다. $hello_kr", CashTag).mkString("/")
        === "$hello_kr(CashTag: 17, 9)"
    )
    
    assert(
      getChunksByPos("Black action solier 출두요~!", Korean).mkString("/")
        === "출두요(Korean: 20, 3)"
    )
    
    assert(
      getChunksByPos("Black action solier 출두요~! ㅋㅋ", KoreanParticle).mkString("/")
        === "ㅋㅋ(KoreanParticle: 26, 2)"
    )
    
    assert(
      getChunksByPos("최근 발매된 게임 '13일의 금요일'은 43,000원에 스팀에서 판매중입니다.", Number).mkString("/")
        === "13일(Number: 11, 3)/43,000원(Number: 22, 7)"
    )
    
    assert(
      getChunksByPos("드래곤볼 Z", Alpha).mkString("/")
        === "Z(Alpha: 5, 1)"
    )
    
    assert(
      getChunksByPos("나의 일기장 안에 모든 말을 다 꺼내어 줄 순 없지만... 사랑한다는 말 이에요.", Punctuation).mkString("/")
        === "...(Punctuation: 29, 3)/.(Punctuation: 44, 1)"
    )
  }

  test("getChunks should correctly extract numbers") {
    assert(
      getChunks("300위안짜리 밥").mkString("/")
        === "300위안/짜리/ /밥"
    )

    assert(
      getChunks("200달러와 300유로").mkString("/")
        === "200달러/와/ /300유로"
    )

    assert(
      getChunks("$200이나 한다").mkString("/")
        === "$200/이나/ /한다"
    )

    assert(
      getChunks("300옌이었다.").mkString("/")
        === "300옌/이었다/."
    )

    assert(
      getChunks("3,453,123,123원 3억3천만원").mkString("/")
        === "3,453,123,123원/ /3억/3천만원"
    )

    assert(
      getChunks("6/4 지방 선거").mkString("/")
        === "6/4/ /지방/ /선거"
    )

    assert(
      getChunks("6.4 지방 선거").mkString("/")
        === "6.4/ /지방/ /선거"
    )

    assert(
      getChunks("6-4 지방 선거").mkString("/")
        === "6-4/ /지방/ /선거"
    )

    assert(
      getChunks("6.25 전쟁").mkString("/")
        === "6.25/ /전쟁"
    )

    assert(
      getChunks("1998년 5월 28일").mkString("/")
        === "1998년/ /5월/ /28일"
    )

    assert(
      getChunks("62:45의 결과").mkString("/")
        === "62:45/의/ /결과"
    )

    assert(
      getChunks("여러 칸  띄어쓰기,   하나의 Space묶음으로 처리됩니다.").mkString("/")
        === "여러/ /칸/  /띄어쓰기/,/   /하나의/ /Space/묶음으로/ /처리됩니다/."
    )
  }

  test("getChunkTokens should correctly find chunks with correct POS tags") {
    assert(
      chunk("한국어와 English와 1234와 pic.twitter.com " +
        "http://news.kukinews.com/article/view.asp?" +
        "page=1&gCode=soc&arcid=0008599913&code=41121111 " +
        "hohyonryu@twitter.com 갤럭시 S5").mkString("/")
        ===
        "한국어와(Korean: 0, 4)/ (Space: 4, 1)/English(Alpha: 5, 7)/와(Korean: 12, 1)/" +
          " (Space: 13, 1)/1234(Number: 14, 4)/와(Korean: 18, 1)/ (Space: 19, 1)/" +
          "pic.twitter.com(URL: 20, 15)/ (Space: 35, 1)/http://news.kukinews.com/" +
          "article/view.asp?page=1&gCode=soc&arcid=0008599913&code=41121111(URL: 36, 89)/" +
          " (Space: 125, 1)/hohyonryu@twitter.com(Email: 126, 21)/ (Space: 147, 1)/" +
          "갤럭시(Korean: 148, 3)/ (Space: 151, 1)/S(Alpha: 152, 1)/5(Number: 153, 1)"
    )

    assert(
      chunk("우와!!! 완전ㅋㅋㅋㅋ").mkString("/")
        === "우와(Korean: 0, 2)/!!!(Punctuation: 2, 3)/ (Space: 5, 1)/완전(Korean: 6, 2)/" +
        "ㅋㅋㅋㅋ(KoreanParticle: 8, 4)"
    )

    assert(
      chunk("@nlpenguin @edeng #korean_tokenizer_rocks 우하하").mkString("/")
        === "@nlpenguin(ScreenName: 0, 10)/ (Space: 10, 1)/@edeng(ScreenName: 11, 6)/" +
        " (Space: 17, 1)/#korean_tokenizer_rocks(Hashtag: 18, 23)/ (Space: 41, 1)/" +
        "우하하(Korean: 42, 3)"
    )
  }

  test("getChunkTokens should correctly detect Korean-specific punctuations.") {
    assert(
      chunk("중·고등학교에서…").mkString("/")
        === "중(Korean: 0, 1)/·(Punctuation: 1, 1)/고등학교에서(Korean: 2, 6)/…(Punctuation: 8, 1)"
    )
  }
}