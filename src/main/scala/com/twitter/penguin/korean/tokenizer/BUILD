scala_library(name = 'tokenizer',
  provides = scala_artifact(
   org = 'com.twitter.penguin.korean',
   name = 'tokenizer',
   repo = pants('build-support/twitter/ivy:internal'),
  ),
  dependencies = [
    pants('3rdparty:twitter-text'),
    pants('src/scala/com/twitter/penguin/korean/util'),
  ],
  sources = globs('*.scala'),
)
