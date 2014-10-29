junit_tests(name = 'util',
  dependencies = [
    pants('3rdparty:junit'),
    pants('3rdparty:scalatest'),
    pants('3rdparty:thrift'),
    pants('3rdparty/jvm/com/twitter/penguin:korean-goldenset'),
    pants('src/scala/com/twitter/penguin/korean/tokenizer'),
    pants('src/thrift/com/twitter/penguin/korean:thrift-scala'),
  ],
  sources = globs('*.scala')
)
