scala_library(name = 'tools',
  provides = scala_artifact(
    org = 'com.twitter.penguin.korean',
    name = 'tools',
    repo = pants('build-support/twitter/ivy:internal'),
  ),
  dependencies = [
    pants('3rdparty:thrift'),
    pants('3rdparty/jvm/com/twitter/penguin:korean-goldenset'),
    pants('src/scala/com/twitter/penguin/korean'),
    pants('src/thrift/com/twitter/penguin/korean:thrift-scala'),
  ],
  sources = globs('*.scala'),
)

jvm_binary(
  name = 'clean_up_dictionaries',
  main = 'com.twitter.penguin.korean.tools.CleanUpDictionaries',
  dependencies = [
    pants(':tools'),
  ]
)

jvm_binary(
  name = 'create_parsing_goldenset',
  main = 'com.twitter.penguin.korean.tools.CreateParsingGoldenset',
  dependencies = [
    pants(':tools'),
  ]
)

jvm_binary(
  name = 'create_conjugation_goldenset',
  main = 'com.twitter.penguin.korean.tools.CreateConjugationGoldenset',
  dependencies = [
    pants(':tools'),
  ]
)
