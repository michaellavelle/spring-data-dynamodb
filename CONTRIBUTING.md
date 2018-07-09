## How can I contribute?

* If you think this project is great, you would like to help, but you don't know how - you can become project's stargazer. By starring you're making project more popular. Visit [this link](https://blog.github.com/2012-08-06-notifications-stars) if you would like to learn more about how notifications and stars works on Github.
* create issues for bugs you find
* create pull requests to fix bigs, add features, clean up code, etc.
* improve the documentation or the wiki

## What can I work on?

We do not yet support the full set of Spring-Data yet:
[Spring Data Reference Documentation](http://docs.spring.io/spring-data/commons/docs/current/reference/html/)

The issues page is another good place to look for ways to contribute.

## Compatibility

The library is heavily based on Spring. Therefore this library should work in any environment that is supported by the underlying Spring Framework version itself.

At the time of writing this is JDK8 (binary compatibility).

## Code Style

Google's Java code style is followed available [here](https://github.com/google/styleguide).

The proper formatting is checked during compile time. To easily follow the style use **one** of those options:
1. Use [Eclipse Formatter](https://help.eclipse.org/neon/topic/org.eclipse.jdt.doc.user/reference/preferences/java/codestyle/ref-preferences-formatter.htm) with the `src/eclipse-java-google-style.xml` file.
1. Use [IntelliJ Formatter](https://blog.jetbrains.com/idea/2014/01/intellij-idea-13-importing-code-formatter-settings-from-eclipse/) with the `src/eclipse-java-google-style.xml` file.
1. Use `mvn formatter:format` to apply the style to the source files.

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
But an Integration Test should always be present _next to_ regular Unit Tests that follow common, good practise to test a single class without requiring existence of external dependencies.

