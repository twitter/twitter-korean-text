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
