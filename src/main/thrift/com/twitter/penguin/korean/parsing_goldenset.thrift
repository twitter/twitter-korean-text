namespace java com.twitter.penguin.korean.thriftjava
#@namespace scala com.twitter.penguin.korean.thriftscala

struct KoreanTokenThrift {
  1: string text
  2: i32 pos
  3: bool unknown
}

/**
 * Serialization framework for Korean parsing goldenset.
 */
struct ParseItem {
  1: string chunk
  2: list<KoreanTokenThrift> parse
}

struct ParsingGoldenset {
  1: list<ParseItem> goldenset
}
