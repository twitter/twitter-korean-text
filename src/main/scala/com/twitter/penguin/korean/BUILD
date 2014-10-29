scala_library(name = 'korean',
  provides = scala_artifact(
    org = 'com.twitter.penguin',
    name = 'korean',
    repo = pants('build-support/twitter/ivy:internal'),
  ),
  dependencies = [
    pants('src/scala/com/twitter/penguin/korean/tokenizer'),
    pants('src/scala/com/twitter/penguin/korean/normalizer'),
  ],
  sources = globs('*.scala'),
)
