/*
 * Twitter Korean Text - Scala library to process Korean text
 *
 * Copyright 2015 Twitter, Inc.
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
package com.twitter.penguin.korean;

public class KoreanTokenJava {
  String text;
  KoreanPosJava pos;
  int offset;
  int length;
  boolean unknown;

  public KoreanTokenJava(String text, KoreanPosJava pos, int offset, int length, boolean unknown) {
    this.text = text;
    this.pos = pos;
    this.offset = offset;
    this.length = length;
    this.unknown = unknown;
  }

  public String getText() {
    return text;
  }

  public KoreanPosJava getPos() {
    return pos;
  }

  public int getOffset() {
    return offset;
  }

  public int getLength() {
    return length;
  }

  public boolean isUnknown() {
    return unknown;
  }

  @Override
  public String toString() {
    String unknownStar = "";
    if (unknown){
      unknownStar = "*";
    }
    return String.format("%s%s(%s: %d, %d)", text, unknownStar, pos.toString(), offset, length);
  }
}
