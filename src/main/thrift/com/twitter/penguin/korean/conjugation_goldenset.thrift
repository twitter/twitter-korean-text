namespace java com.twitter.penguin.korean.thriftjava
#@namespace scala com.twitter.penguin.korean.thriftscala

/**
 * Serialization framework for Korean conjugation goldenset.
 */
struct ConjugationItem {
  1: string chunk
  2: set<string> conjugation
}

struct ConjugationGoldenset {
  1: list<ConjugationItem> goldenset
}