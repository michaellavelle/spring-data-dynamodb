## How can I contribute?

* create issues for bugs you find
* create pull requests to fix bigs, add features, clean up code, etc.
* improve the documentation or the wiki

## What can I work on?

We do not yet support the full set of Spring-Data yet:
[docs.spring.io/spring-data/commons/docs/current/reference/html/](http://docs.spring.io/spring-data/commons/docs/current/reference/html/)

The issues page is another good place to look for ways to contribute.

## Compatibility

The library is heavily based on Spring. Therefore this library should work in any environment that is supported by the underlying Spring Framework version itself.

At the time of writing this is JDK7 (binary compatibility) but at compile time JDK8 is required - therefore supporting also `Optional` etc.

## Code Style

A dedicated and holistic code style is not yet defined.
Changes to existing classes should follow the style that is found in that specific class.

In gernal, the Google Code style should be followed.

## Merging

We use a rebase / cherry-pick strategy to merging code. This is to maintain a legible git history on the master branch. This has a few implications:

* it is best if you rebase your branches onto master to fix merge conflicts instead of merging
* your commits may be squashed, reordered, reworded, or edited when merged
* your pull request will be marked closed instead of merged but will be linked to the closing commit
* your branch will not remain tracked by this repository

## Working with the Code

### General
Testing is always important and the code coverage by unit tests should not decrease by new or adopted code.

### New features
DynamoDB comes with a lot of features. Therefore new supported features should always come with an Integration Test.

That means that it should have an `...IT.java` test class that uses the local DynamoDB to demonstrate how the feature is used.
This serves as proove that it is working (test case succeeds) and also as documentation and 'how-to'.