junit_tests(name = 'korean',
  dependencies = [
    pants('3rdparty:junit'),
    pants('3rdparty:scalatest'),
    pants('3rdparty:thrift'),
    pants('3rdparty/jvm/com/twitter/penguin:korean-goldenset'),
    pants('src/scala/com/twitter/penguin/korean'),
    pants('src/thrift/com/twitter/penguin/korean:thrift-scala'),
  ],
  sources = globs('*.scala')
)

dependencies(name = 'all',
  dependencies = [
    pants(':korean'),
    pants('tests/scala/com/twitter/penguin/korean/normalizer'),
    pants('tests/scala/com/twitter/penguin/korean/tokenizer'),
    pants('tests/scala/com/twitter/penguin/korean/util'),
  ],
)
