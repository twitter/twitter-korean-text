/** Twitter Korean Text - Scala library to process Korean text
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

namespace java com.twitter.penguin.korean.v1.thriftjava
#@namespace scala com.twitter.penguin.korean.v1.thriftscala

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
