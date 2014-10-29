package com.twitter.penguin.korean.util

import com.twitter.penguin.korean.util.Hangul._
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class HangulTest extends FunSuite {

  test("decomposeKoreanChar should decompose full Korean chars correctly") {
    assert(decomposeHangul('간') ==('ㄱ', 'ㅏ', 'ㄴ'))
    assert(decomposeHangul('관') ==('ㄱ', 'ㅘ', 'ㄴ'))
    assert(decomposeHangul('꼃') ==('ㄲ', 'ㅕ', 'ㅀ'))
  }

  test("decomposeKoreanChar should decompose full no coda chars correctly") {
    assert(decomposeHangul('가') ==('ㄱ', 'ㅏ', ' '))
    assert(decomposeHangul('과') ==('ㄱ', 'ㅘ', ' '))
    assert(decomposeHangul('껴') ==('ㄲ', 'ㅕ', ' '))
  }

  test("decomposeKoreanChar should raise an exception if input is invalud") {
    intercept[IllegalArgumentException] {
      decomposeHangul('ㅋ')
    }
    intercept[IllegalArgumentException] {
      decomposeHangul('ㅏ')
    }
    intercept[IllegalArgumentException] {
      decomposeHangul('ㅀ')
    }
  }

  test("hasCoda should return true when a character has a coda") {
    assert(hasCoda('갈'))
    assert(hasCoda('갉'))
  }

  test("hasCoda should return false when a character does not have a coda") {
    assert(!hasCoda('가'))
    assert(!hasCoda('ㄱ'))
    assert(!hasCoda('ㅘ'))
    assert(!hasCoda(' '))
  }

  test("composeKoreanChar should compose a full Korean char from a triple of letters") {
    assert(composeHangul('ㄱ', 'ㅏ', 'ㄷ') === '갇')
    assert(composeHangul('ㄲ', 'ㅑ', 'ㅀ') === '꺓')
    assert(composeHangul('ㅊ', 'ㅘ', 'ㄴ') === '촨')
  }

  test("composeKoreanChar should compose a no-coda Korean char from a triple of letters") {
    assert(composeHangul('ㄱ', 'ㅏ', ' ') === '가')
    assert(composeHangul('ㄲ', 'ㅑ', ' ') === '꺄')
    assert(composeHangul('ㅊ', 'ㅘ', ' ') === '촤')
  }

  test("composeKoreanChar should raise an exception if input is invalid") {
    intercept[IllegalArgumentException] {
      composeHangul(' ', 'ㅏ', ' ')
    }
    intercept[IllegalArgumentException] {
      composeHangul('ㄲ', ' ', ' ')
    }
    intercept[IllegalArgumentException] {
      composeHangul(' ', ' ', 'ㄴ')
    }
  }
}

