import com.twitter.penguin.korean.TwitterKoreanProcessor
import com.twitter.penguin.korean.tokenizer.KoreanTokenizer

object ScalaTwitterKoreanTextExample {
  def main(args: Array[String]) {
    // Tokenize into List<String>
    val parsed: Seq[String] = TwitterKoreanProcessor.tokenizeToStrings("한국어를 처리하는 예시입니닼ㅋㅋㅋㅋㅋ")
    println(parsed)
    // ArraySeq(한국어, 를, 처리, 하다, 예시, 이다, ㅋㅋ)

    // Tokenize with Part-of-Speech information
    val parsedPos: Seq[KoreanTokenizer.KoreanToken] = TwitterKoreanProcessor.tokenize("한국어를 처리하는 예시입니닼ㅋㅋㅋㅋㅋ")
    println(parsedPos)
    // ArraySeq(한국어Noun, 를Josa, 처리Noun, 하다Verb, 예시Noun, 이다Adjective, ㅋㅋKoreanParticle)

    // Tokenize without normalization and stemming
    val parsedPosParsingOnly: Seq[KoreanTokenizer.KoreanToken] = TwitterKoreanProcessor
      .tokenize("한국어를 처리하는 예시입니닼ㅋㅋㅋㅋㅋ", normalizization = false, stemming = false)
    println(parsedPosParsingOnly)
    // ArraySeq(한국어Noun, 를Josa, 처리Noun, 하는Verb, 예시Noun, 입Noun, 니Josa, 닼Noun*, ㅋㅋㅋㅋㅋKoreanParticle)
  }
}
