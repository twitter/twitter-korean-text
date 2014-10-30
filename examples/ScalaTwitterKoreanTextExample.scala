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