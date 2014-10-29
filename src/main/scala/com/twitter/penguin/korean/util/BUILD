scala_library(name = 'util',
  provides = scala_artifact(
   org = 'com.twitter.penguin.korean',
   name = 'util',
   repo = pants('build-support/twitter/ivy:internal'),
  ),
  dependencies = [
    pants('3rdparty:thrift'),
    pants('3rdparty/jvm/org/apache/lucene:lucene-analyzers-common'),
    pants('3rdparty/jvm/com/twitter/penguin:korean-dictionary'),
  ],
  sources = globs('*.scala'),
)
