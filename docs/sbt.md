SBT
===

-	build: `$ sbt package -verbose`
-	test: `$ sbt test`
-	run
	-	**사전 업데이트 등의 작업 후에 할 것** &mdash; `$ sbt "runMain com.twitter.penguin.korean.tools.UpdateAllTheExamples"`
	-	기타
		-	`$ sbt "runMain com.twitter.penguin.korean.qa.BatchGetUnknownNouns ./src/main/resources/com/twitter/penguin/korean/util/example_tweets.txt"`
		-	`$ sbt "runMain com.twitter.penguin.korean.qa.BatchGetUnknownNouns  ./src/main/resources/com/twitter/penguin/korean/util/example_tweets.txt"` Looking to contribute something? Here's how you can help.

Bugs reports
------------

A bug is a *demonstrable problem* that is caused by the code in the repository. Good bug reports are extremely helpful - thank you!

Guidelines for bug reports:

1.	**Use the GitHub issue search** &mdash; check if the issue has already been reported.
