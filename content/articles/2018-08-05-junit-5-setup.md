---
title: "JUnit 5 Setup in IntelliJ, Eclipse, Maven, and Gradle"
tags: [tools, junit-5, libraries, testing]
date: 2018-08-05
slug: junit-5-setup
description: "How to set up JUnit 5 so tests run in IntelliJ, Eclipse, Maven, Gradle or, if all else fails, via JUnit 4 or on the command line."
searchKeywords: "setup"
featuredImage: junit-5-setup
repo: junit-5-demo
---

JUnit 5 tool support has come a long way since its early days in 2017, so setting JUnit 5 up in your favorite IDE or build tool should be fairly straight-forward.
Here's how to do it in IntelliJ, Eclipse, Maven, Gradle, or, if everything else fails, on the command line.

## Writing Tests

To write tests, you need the Jupiter API artifact:

-   **Group ID**: org.junit.jupiter
-   **Artifact ID**: junit-jupiter-api
-   **Version**: 5.2.0
-   **Scope**: test

Including it in your project with your favorite build tool is all it takes to write tests.
So lets do that and quickly create [our first test](https://github.com/nipafx/demo-junit-5/blob/master/src/test/java/org/codefx/demo/junit5/HelloWorldTest.java):

```java
import org.junit.jupiter.api.Test;

class HelloWorldTest {

	@Test
	void helloJUnit5() {
		System.out.println("Hello, JUnit 5.");
	}

}
```

See ma, no `public`!
Cool, right?
I won't go into it here, though - check out the [post on basics](junit-5-basics) for more details.

## Running Tests

A new aspect of JUnit 5, and I'll go into more details when we discuss [its architecture](junit-5-architecture-jupiter), are *engines*.
An engine is in charge of executing all tests with a specific API.
For the Jupiter API, which we just added and used, that would be the Jupiter engine:

-   **Group ID**: org.junit.jupiter
-   **Artifact ID**: junit-jupiter-engine
-   **Version**: 5.2.0
-   **Scope**: test or testRuntime

Because the test API evolves over time, the engine must do the same and so it's best for the project to specify the exact engine version in its build configuration.

### Build Tool Support

Initially, the JUnit 5 team implemented a rudimentary Gradle plugin and Maven Surefire provider as proofs of concept.
In the meantime, both tools have implemented native support, so there's no need to use `junit-platform-gradle-plugin` or `junit-platform-surefire-provider` anymore - you can remove them.

#### Gradle

[Native JUnit 5 support](https://docs.gradle.org/current/userguide/java_testing.html#using_junit5) is available since [Gradle 4.6](https://docs.gradle.org/4.6/release-notes.html).
All you need to do is activate it in the `test` task:

```groovy
test {
	useJUnitPlatform()
}
```

As I explained, you need the engine at test run time, so the tests can actually be executed:

```groovy
testRuntime "org.junit.jupiter:junit-jupiter-engine:5.2.0"
```

For more details on the Gradle integration, check [its documentation](https://docs.gradle.org/current/userguide/java_plugin.html#sec:java_test).

#### Maven

Maven's surefire provider has [native support for JUnit 5](https://maven.apache.org/surefire/maven-surefire-plugin/examples/junit-platform.html) since version 2.22.0.
It picks up the test engine from your regular dependencies:

```xml
<dependency>
	<groupId>org.junit.jupiter</groupId>
	<artifactId>junit-jupiter-engine</artifactId>
	<version>5.2.0</version>
	<scope>test</scope>
</dependency>
```

### IDE Support

Eclipse and IntelliJ natively support JUnit 5, but for NetBeans [I couldn't even find an issue](https://netbeans.org/bugzilla/buglist.cgi?bug_status=UNCONFIRMED&bug_status=NEW&bug_status=STARTED&bug_status=REOPENED&bug_status=RESOLVED&bug_status=VERIFIED&bug_status=CLOSED&f0=OP&f1=OP&f10=OP&f11=OP&f12=alias&f13=short_desc&f14=status_whiteboard&f15=content&f16=CP&f17=CP&f2=product&f3=component&f4=alias&f5=short_desc&f6=status_whiteboard&f7=content&f8=CP&f9=CP&j1=OR&j11=OR&o12=substring&o13=substring&o14=substring&o15=matches&o2=substring&o3=substring&o4=substring&o5=substring&o6=substring&o7=matches&query_format=advanced&short_desc=junit%205&short_desc_type=allwordssubstr&v12=5&v13=5&v14=5&v15=%225%22&v2=junit&v3=junit&v4=junit&v5=junit&v6=junit&v7=%22junit%22).

#### IntelliJ IDEA

[IntelliJ IDEA supports JUnit 5](https://blog.jetbrains.com/idea/2016/08/using-junit-5-in-intellij-idea/) since [2016.2](https://blog.jetbrains.com/idea/2016/07/intellij-idea-2016-2-is-here/), but I strongly recommend to use at least [2017.3](https://blog.jetbrains.com/idea/2017/11/intellij-idea-2017-3-junit-support/).
Until then, IntelliJ used to come with its own version of the Jupiter engine, which leads to problems if your project does not depend on the matching API version.
Since 2017.3, IntelliJ selects the engine based on the API version you depend on.

#### Eclipse

Eclipse supports JUnit 5 since [Oxygen.1a (4.7.1a)](https://www.eclipse.org/eclipse/news/4.7.1a/#junit-5-support), but I didn't figure out how it picks up the engine.

### JUnit 4 Runner

If the support for your tool of choice does not suffice, you can try the detour via JUnit 4: A [test runner](http://www.codeaffine.com/2014/09/03/junit-nutshell-test-runners/) called [`JUnitPlatform`](http://junit.org/junit5/docs/current/api/org/junit/platform/runner/JUnitPlatform.html) can be used to run new tests as part of a JUnit 4 run.
You find it in its own artifact, which you have to add to your project (on top of JUnit 4 and the JUnit 5 Jupiter API and engine):

-   **Group ID**: org.junit.platform
-   **Artifact ID**: junit-platform-runner
-   **Version**: 1.2.0
-   **Scope**: test or testRuntime

To run all tests in a project, it is easiest to create a test suite for them:

```java
package org.codefx.demo.junit5;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.runner.SelectPackages;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
@SelectPackages({ "org.codefx.demo.junit5" })
public class TestWithJUnit5 { }
```

Note that the class has to be a regular JUnit 4 test class, i.e.
it has to adhere to the [common naming convention](http://stackoverflow.com/a/6178629/2525313) and must be public.
The `@SelectPackages`-annotation interprets packages as a hierarchy so it runs all tests in all packages prefixed with `org.codefx.demo.junit5`.
If you prefer, you can use the same runner directly on the JUnit 5 test classes; in that case they have to be public.

Now we're done!
Even if slightly outdated, your favorite IDE and build tool will happily run the classes annotated with `@RunWith(JUnitPlatform.class)` and hence the new JUnit 5 tests.

Due to the detour through JUnit 4, some features may not be supported, e.g. IDEs won't run individual test methods.
But if the other approaches do not work for you, this can be an acceptable and tool independent solution.

### Command Line For The Win!

In case all of this is too fancy for you, try the [console launcher](http://junit.org/junit5/docs/current/user-guide/#running-tests-console-launcher), which lets you run the tests directly from the command line.
The best way to use it is to download the [standalone JAR](https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/), which comes with all the required dependencies.

Ignoring your dependencies (e.g. from production code or on test libraries) you can then use it as follows:

```shell
# run all tests
$ java -jar junit-platform-console-standalone.jar
	--class-path ${path_to_compiled_test_classes}
	--scan-class-path
# run a specific test
$ java -jar junit-platform-console-standalone
	--class-path ${path_to_compiled_test_classes}
	--select-class ${fully_qualified_test_class_name}
```

To include dependencies, add them to the class path after `--class-path`.
If you're doing this in a Maven project, your command might look like this:

```shell
java -jar junit-platform-console-standalone
	--class-path target/test-classes:target/classes
	--scan-class-path
```

## Compatibility

As you might have noticed, JUnit 5 occupies new namespaces: `org.junit.jupiter`, `org.junit.platform`, and `org.junit.vintage` (which we didn't see yet).
I explain their meaning [in a post dedicated to JUnit's architecture](junit-5-architecture-jupiter) - for now this only means that there will be no conflicts when different JUnit versions are used in the same project.

Indeed, a project can contain and run tests from different versions without problems, which allows a slow migration to JUnit 5.
We will revisit this topic when we're exploring migration paths (stay tuned).

<pullquote>A project can contain and run tests from different JUnit versions</pullquote>

Assertion libraries like Hamcrest and AssertJ, which communicate with JUnit via exceptions, continues to work in the new version.
Check out the [complete version of `HelloWorldTest`](https://github.com/nipafx/demo-junit-5/blob/master/src/test/java/org/codefx/demo/junit5/HelloWorldTest.java) for an example using Mockito and AssertJ.

## Reflection

For our JUnit 5 setup we've included `junit-jupiter-api` and the matching `junit-jupiter-engine`, in our project, written a first minimal test case, and made sure it runs in various IDEs and build tools.

[The next post](junit-5-basics) explores the basics of how to write tests in JUnit 5.
