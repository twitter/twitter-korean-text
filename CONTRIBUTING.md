# Contributing Guidelines

Looking to contribute something? Here's how you can help.

## Bugs reports

A bug is a _demonstrable problem_ that is caused by the code in the
repository. Good bug reports are extremely helpful - thank you!

Guidelines for bug reports:

1. **Use the GitHub issue search** &mdash; check if the issue has already been
   reported.

2. **Check if the issue has been fixed** &mdash; try to reproduce it using the
   latest `master` or development branch in the repository.

3. **Isolate the problem** &mdash; ideally create a reduced test
   case and a live example.

4. Please try to be as detailed as possible in your report. Include specific
   information about the environment - operating system and version, browser
   and version and steps required to reproduce the issue.


## Feature requests & contribution enquiries

Feature requests are welcome. But take a moment to find out whether your idea
fits with the scope and aims of the project. It's up to *you* to make a strong
case for the inclusion of your feature. Please provide as much detail and
context as possible.

Contribution enquiries should take place before any significant pull request,
otherwise you risk spending a lot of time working on something that we might
have good reasons for rejecting.

## Making changes to the code

1. Clone the repo
```
git clone https://github.com/twitter/twitter-korean-text.git
```
2. Change the code
3. Run tests
```
mvn test
```
4. You are almost certain to have broken a test, probably TwitterKoreanProcessorTest. You will see the difference of behavior in the console. Check if the difference improves the tokenizer. (Please copy and store the difference. It would be useful to have it in the review process.)
```
Goldenset Match Error: 올라와도 (올라Noun 와도Josa) -> (올라와Verb 도Eomi)
Goldenset Match Error: 동일조건변경허락 (동일조건변경허락Noun) -> (동일Noun 조건Noun 변경Noun 허락Noun)
Goldenset Match Error: 기획조정실장 (기획조정실장Noun) -> (기획Noun 조정Noun 실장Noun)
Goldenset Match Error: 안올라 (안Noun 올라Noun) -> (안Noun 올라Verb)
```
5. Run [src/main/scala/com/twitter/penguin/korean/tools/CreateParsingGoldenset.scala](src/main/scala/com/twitter/penguin/korean/tools/CreateParsingGoldenset.scala) to update the golden set. You can run it via maven or your IDE. I would recommend using an IDE.

## Pull requests

Good pull requests - patches, improvements, new features - are a fantastic
help. They should remain focused in scope and avoid containing unrelated
commits.

Make sure to adhere to the coding conventions used throughout the codebase
(indentation, accurate comments, etc.) and any other requirements (such as test
coverage).

Please follow this process; it's the best way to get your work included in the
project:

1. Create a new topic branch to contain your feature, change, or fix:

2. Commit your changes in logical chunks. Provide clear and explanatory commit
   messages. Use git's [interactive rebase](https://help.github.com/articles/interactive-rebase)
   feature to tidy up your commits before making them public.

3. Locally merge (or rebase) the upstream development branch into your topic branch:

4. Push your topic branch up to your fork:

5. [Open a Pull Request](http://help.github.com/send-pull-requests/) with a
   clear title and description.

## License

By contributing your code,

You agree to license your contribution under the terms of the Apache Public License 2.0
https://github.com/twitter/twitter-korean-text/blob/master/LICENSE
