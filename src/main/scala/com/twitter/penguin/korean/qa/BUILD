scala_library(name = 'qa',
  provides = scala_artifact(
    org = 'com.twitter.penguin.korean',
    name = 'qa',
    repo = pants('build-support/twitter/ivy:internal'),
  ),
  dependencies = [
    pants('src/scala/com/twitter/penguin/korean'),
  ],
  sources = globs('*.scala'),
)

jvm_binary(
  name = 'batch_tokenize_tweets',
  main = 'com.twitter.penguin.korean.qa.BatchTokenizeTweets',
  dependencies = [
    pants(':qa'),
  ]
)

jvm_binary(
  name = 'batch_get_unknown_nouns',
  main = 'com.twitter.penguin.korean.qa.BatchGetUnknownNouns',
  dependencies = [
    pants(':qa'),
  ]
)

jvm_binary(
  name = 'korean_tokenizer_sandbox',
  main = 'com.twitter.penguin.korean.qa.KoreanTokenizerSandbox',
  dependencies = [
    pants(':qa'),
  ]
)
