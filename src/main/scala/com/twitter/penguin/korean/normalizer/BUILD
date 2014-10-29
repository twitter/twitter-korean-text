scala_library(name = 'normalizer',
  provides = scala_artifact(
   org = 'com.twitter.penguin.korean',
   name = 'normalizer',
   repo = pants('build-support/twitter/ivy:internal'),
  ),
  dependencies = [
    pants('src/scala/com/twitter/penguin/korean/util'),
  ],
  sources = globs('*.scala'),
)
