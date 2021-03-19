---
title: "JUnit Lambda - The Prototype"
tags: [junit-5, libraries, testing]
date: 2015-11-23
slug: junit-lambda-prototype
description: "JUnit Lambda will eventually bring us JUnit 5. This is a discussion of the recent prototype, its features, core principles and compatibility considerations."
searchKeywords: "junit lambda"
featuredImage: junit-lambda
repo: junit-lambda-demo
---

Did you hear about [*JUnit Lambda*](http://junit.org/junit-lambda.html)?
I hope so because those guys are shaping the future of testing on the JVM.
Too exaggerated?
Maybe, but not by much.
This is the next version of our beloved JUnit, by far the [most used Java library](http://blog.takipi.com/we-analyzed-60678-libraries-on-github-here-are-the-top-100/), in the making.

I experimented with the brand new prototype and present my findings here.
The project is currently gathering feedback so this is our chance to weigh in.

<admonition type="note">

As expected, things changed considerably between prototype and released version, and this article **does not apply** to JUnit 5 - it only still exists for historical reasons.
I ended up writing a lot about the released JUnit 5 version, though - from setup to basics to architecture and extensions - so [check it out](tag:junit-5)!

</admonition>

## Background

*JUnit Lambda* is a project by a bunch of Java test enthusiast, including a JUnit core committer.

> The goal is to create an up-to-date foundation for developer-side testing on the JVM.
This includes focusing on Java 8 and above, as well as enabling many different styles of testing.
>
> [JUnit Lambda Project Site](http://junit.org/junit-lambda.html)

They [collected funds](https://www.indiegogo.com/projects/junit-lambda/#/) from July to October, [kicked off](https://github.com/junit-team/junit-lambda/wiki/Kickoff-Meeting) their full-time work on this in a meeting from 20 to 22nd of October and [released their prototype](https://github.com/junit-team/junit-lambda/wiki/Prototype) last Wednesday (18th of November).

> If you want to provide input in the interim, please use the project's [issue tracker](https://github.com/junit-team/junit-lambda/issues) or send us comments via [Twitter](https://twitter.com/junitlambda).
>
> [JUnit Lambda Wiki](https://github.com/junit-team/junit-lambda/wiki/Prototype)

The project is collecting feedback until November, 30th and will then start working on an alpha version.
This also implies that the current version is not even alpha.
Keep that in mind when forming your opinion.

## Features

### Setup, Test, Teardown

The most important piece of JUnit's API is the `@Test` annotation and nothing changes here: Only the methods annotated with it will be considered as tests.

The tried and tested annotations to set up and tear down tests stay virtually unchanged but have new names:

-   `@Before` and `@After`, which run before and after *each* test method, are now called `@BeforeEach` and `@AfterEach`
-   `@BeforeClass` and `@AfterClass`, which run before the *first* and after the *last* test from a class, are now called `@BeforeAll` and `@AfterAll`

I like the new names.
They are more intention revealing and thus easier to understand - especially for beginners.

Then there is the new `@Name`, which can be used to give more human readable names to test classes and methods.
An example from [the documentation](https://github.com/junit-team/junit-lambda/wiki/Prototype-Writing-Test-Cases#custom-names):

```java
@Name("A special test case")
class CanHaveAnyNameTest {

  @Test
  @Name("A nice name, isn't it?")
  void testWithANiceName() {}

}
```

My test method names follow the pattern *unitOfWork\_stateUnderTest\_expectedBehavior* as [presented by Roy Osherove](http://osherove.com/blog/2005/4/3/naming-standards-for-unit-tests.html) and I'm not planning on repeating any of that anywhere else.
My uneducated guess is that most developers who care about their test method names think similarly and those who don't won't use it anyways.
So from my point of view, this does not add much value.

### Assertions

If `@Before...`, `@After...`, and `@Test` are a test suite's skeleton, assertions are its heart.
The prototype undertakes a careful evolution here.

Assertion messages now come last and can be created lazily.
That `assertTrue` and `assertFalse` can directly evaluate a `BooleanSupplier` is a nice tweak.

```java
@Test
void interestingAssertions() {
	String mango = "Mango";

	// message comes last
	assertEquals("Mango", mango, "Y U no equal?!");

	// message can be created lazily
	assertEquals("Mango", mango,
			() -> "Expensive string, creation deferred until needed.");

	// for 'assert[True|False]' it is possible
	// to directly test a supplier that exists somewhere in the code
	BooleanSupplier existingBooleanSupplier = () -> true;
	assertTrue(existingBooleanSupplier);
}
```

More interesting is the added feature of capturing exceptions...

```java
@Test
void exceptionAssertions() {
	IOException exception = expectThrows(
			IOException.class,
			() -> { throw new IOException("Something bad happened"); });
	assertTrue(exception.getMessage().contains("Something bad"));
}
```

... and that assertions can be grouped to test them all at once.

```java
@Test
void groupedAssertions() {
	assertAll("Multiplication",
			() -> assertEquals(15, 3 * 5, "3 x 5 = 15"),
			// this fails on purpose to see what the message looks like
			() -> assertEquals(15, 5 + 3, "5 x 3 = 15")
	);
}
```

Note how the group can have a name (in this case "Multiplication") and that the contained assertions are given as lambdas to delay execution.

When it comes to assertions in general, I always valued JUnit's extensibility.
This allowed me to ignore the built-in assertions and the steganographic masterpiece that is Hamcrest in favor of [AssertJ](http://joel-costigliola.github.io/assertj/).
Hence I don't have any real opinions on this, except that the changes seem to improve things slightly.

### Visibility

You might already have spotted it: Test classes and methods do not have to be public anymore.
I think that's great news!
One useless keyword less.

While package visibility suffices to be run, private methods will still be ignored.
This is a very sensible decision, in line with how visibility is commonly understood end employed.

### Lifecycles

JUnit 4 always creates a new instance of the test class for every single test method.
This minimizes the chances of individual tests subtly interacting with and unwantedly depending on each other.

The prototype contains a new annotation `@TestInstance`, which specifies the lifecycle of the test class instances.
They can either be created per test method (default behavior) or once for all of the tests:

```java
@TestInstance(Lifecycle.PER_CLASS)
class _2_PerClassLifecycle {

	/** There are two test methods, so the value is 2. */
	private static final int EXPECTED_TEST_METHOD_COUNT = 2;

	/** Is incremented by every test method
		AND THE STATE IS KEPT ACROSS METHODS! */
	private int executedTestMethodCount = Integer.MIN_VALUE;

	// Note that the following @[Before|After]All methods are _not_ static!
	// They don't have to be because this test class has a lifecycle PER_CLASS.

	@BeforeAll
	void initializeCounter() {
		executedTestMethodCount = 0;
	}

	@AfterAll
	void assertAllMethodsExecuted() {
		assertEquals(EXPECTED_TEST_METHOD_COUNT, executedTestMethodCount);
	}

	@Test
	void oneMethod() { executedTestMethodCount++; }

	@Test
	void otherMethod() { executedTestMethodCount++; }

}
```

I think this is a typical case of a feature that is harmful in 99% of the cases but indispensable in the other 1%.
I am honestly afraid of what a test suite might look like that had inexperienced developers sprinkle such inter-test-dependencies across it.
But having such devs do whatever they want without checks and balances like pair programming and code reviews is a problem in itself so having a per-class lifecycle will not make that much worse.

What do you think?
Ship it or scrap it?

### Inner Classes

Some people use inner classes in their test suites.
I do it to [inherit interface tests](https://github.com/nipafx/LibFX/blob/3ec42447a99cbac33642cef35d0e522f7b595435/src/test/java/org/codefx/libfx/collection/tree/stream/StackTreePathTest.java), others to [keep their test classes small](http://www.petrikainulainen.net/programming/testing/writing-clean-tests-small-is-beautiful/).
To have them run in JUnit 4 you have to either use [JUnit's `@Suite`](http://junit.sourceforge.net/javadoc/org/junit/runners/Suite.html) or [NitorCreations' more elegant `NestedRunner`](https://github.com/NitorCreations/CoreComponents/tree/master/junit-runners#nestedrunner).
Still you have to do *something*.

With *JUnit Lambda* this is not necessary anymore!
In the following example, all of the printing methods are executed:

```java
class _4_InnerClasses {

	@Nested
	class InnerClass {

		@Test
		void someTestMethod() { print("Greetings!"); }

	}

	@Nested
	static class StaticClass {

		@Test
		void someTestMethod() { print("Greetings!"); }

	}

	class UnannotatedInnerClass {

		@Test
		void someTestMethod() { throw new AssertionError(); }

	}

	static class UnannotatedStaticClass {

		@Test
		void someTestMethod() { print("Greetings!"); }

	}

}
```

The new annotation `@Nested` guides JUnit and the reader to understand the tests as part of a larger suite.
An [example form the prototype's codebase](https://github.com/junit-team/junit-lambda/blob/2a7773eb773ac821a15444759bfda211af8ee006/sample-project/src/test/java/com/example/TestingAStack.java) demonstrates this well.

In the current version, it is also required to trigger the execution of tests in non-static inner classes but [that seems to be coincidental](https://twitter.com/sam_brannen/status/668043376214847488).
And while the documentation discourages the use on static classes, I guess because it interacts badly with a per-class lifecycle, this does not lead to an exception.

### Assumptions

[Assumptions](http://junit.org/apidocs/org/junit/Assume.html) got a nice addition utilizing the power of lambda expressions:

```java
@Test
void assumeThat_trueAndFalse() {
	assumingThat(true, () -> executedTestMethodCount++);
	assumingThat(false, () -> {
		String message = "If you can see this, 'assumeFalse(true)' passed, "
				+ "which it obviously shouldn't.";
		throw new AssertionError(message);
	});
}
```

I think I never once used assumptions, so what can I say?
Looks nice.
:)

### Custom Annotations

When *JUnit Lambda* checks a class or method (or anything else really) for annotations, it also looks at the annotations' annotations and so forth.
It will then treat any annotation it finds during that search as if it were directly on the examined class or method.
This is the hamstrung but common way to simulate inheritance of annotations, which [Java does not support directly](http://stackoverflow.com/q/1624084/2525313 "Why is [it] not possible to extend annotations in Java?
- StackOverflow").

We can use this to easily create custom annotations:

```java
/**
 * We define a custom annotation that:
 * - stands in for '@Test' so that the method gets executed
 * - gives it a default name
 * - has the tag "integration" so we can filter by that,
 *   e.g. when running tests from the command line
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Test
@Name("Evil integration test! 👹")
@Tag("integration")
public @interface IntegrationTest { }
```

We can then use it like this:

```java
@IntegrationTest
void runsWithCustomAnnotation() {
	// this is run even though 'IntegrationTest' is not defined by JUnit
}
```

This is really neat!
A simple feature with great implications.

### Conditions

Now it gets really interesting!
*JUnit Lambda* introduces the concept of *conditions*, which allows the creation of custom annotations that decide whether a test should be skipped or not.
The decision whether to run a specific test method is made as follows:

1. the runner looks for any annotation on the test method that is itself annotated with `@Conditional(Class<?
extends Condition> condition)`
2. it creates an instance of `condition` and a `TestExecutionContext`, which contains a bunch of information for the current test
3. it calls the condition's `evaluate`-method with the context
4. depending in the call's return value it decides whether the test is run or not

One condition that comes with JUnit is `@Disabled`, which replaces `@Ignore`.
Its `DisabledCondition` simply checks whether the annotation is present on the method or on the class and creates a matching message.

Let's do something more interesting and much more useful: We'll create an annotation that skips tests if it's Friday afternoon.
For all those tricky ones that threaten your weekend.

Let's start with the date check:

```java
static boolean itsFridayAfternoon() {
	LocalDateTime now = LocalDateTime.now();
	return now.getDayOfWeek() == DayOfWeek.FRIDAY
			&& 13 <= now.getHour() && now.getHour() <= 18;
}
```

Now we create the `Condition` implementation that evaluates a test:

```java
class NotFridayCondition implements Condition {

	@Override
	public Result evaluate(TestExecutionContext testExecutionContext) {
		return itsFridayAfternoon()
				? Result.failure("It's Friday afternoon!")
				: Result.success("Just a regular day...");
	}

}
```

We can see that we don't need the `TestExecutionContext` at all and simply check whether it is Friday afternoon or not.
If it is, the result is a failure (unlucky naming) so the test will be skipped.

We can now hand this class to `@Conditional` and define our annotation:

```java
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Conditional({NotFridayCondition.class})
public @interface DontRunOnFridayAfternoon { }
```

And to get rid of that pesky test and ride off into the Friday afternoon sunset:

```java
@Test
@DontRunOnFridayAfternoon
void neverRunsOnFridayAfternoon() {
	assertFalse(itsFridayAfternoon());
}
```

Very nice.
Great feature!

### Injection

Last but not least, there is great support for injecting instances into tests.

This is done by simply declaring the required instances as test method parameters.
For each of the parameters, JUnit will then look for a `MethodParameterResolver` that supports its type.
Such resolvers are either shipped with JUnit or listed in the new `@ExtendWith` annotation on the test class.

If JUnit finds an applicable resolver, it uses it to create an instance of the parameter.
Otherwise the test fails.

A simple example is `@TestName` which, if used on a string, injects the test's name:

```java
@Test
void injectsTestName(@TestName String testName) {
	// '@TestName' comes with JUnit.
	assertEquals("injectsTestName", testName);
}
```

Creating a resolver is easy.
Let's say there is a `Server` class, which we need to preconfigure for different tests.
Doing that is as simple as writing this class:

```java
public class ServerParameterResolver implements MethodParameterResolver {

	@Override
	public boolean supports(Parameter parameter) {
		// support all parameters of type 'Server'
		return parameter.getType().equals(Server.class);
	}

	@Override
	public Object resolve(Parameter parameter, TestExecutionContext context)
			throws ParameterResolutionException {
		return new Server("https://nipafx.dev");
	}
}
```

Assuming the test class is annotated with `@ExtendWith( { ServerParameterResolver.class } )`, any parameter of type `Server` is initialized by the resolver:

```java
@Test
void injectsServer(Server server) {
	int statusCode = server.sendRequest("gimme!");
	assertEquals(200, statusCode);
}
```

Let's look at a slightly more complicated example.

Say we want to provide a resolver for strings that should contain E-Mail addresses.
Now, analog to above we could write a `StringParameterResolver` but it would be used for all strings which is not what we want.
We need a way to identify those strings that should contain addresses.
For that we introduce an annotation...

```java
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface EMailParameter { }
```

... which we use to limit the supported parameters.

```java
public class Resolver implements MethodParameterResolver {

	@Override
	public boolean supports(Parameter parameter) {
		// support strings annotated with '@EmailParameter'
		return parameter.getType().equals(String.class)
				&& isAnnotated(parameter, EMailParameter.class);
	}

	@Override
	public Object resolve(Parameter parameter, TestExecutionContext context)
			throws ParameterResolutionException {
		return "nicolai@nipafx.dev";
	}
}
```

Makes sense, right?
Now we can use it like `@TestName`:

```java
@Test
void injectsEMailAddress(@EMailParameter String eMail) {
	assertTrue(eMail.contains("@"));
}
```

Did I say that I like this feature?
I do.
In fact I think it's awesome!
I'm sure third-party test libraries can make great use of this.

But I wonder whether `supports` should also receive a `TestExecutionContext` for more fine-grained decision-making.

## Misc

### Extensibility

The project lists a couple of [core principles](https://github.com/junit-team/junit-lambda/wiki/Core-Principles), one of them is to "prefer extension points over features".
This is a great principle to have and I think especially the last features we discussed follow this very well.

The ability to create custom annotations, conditions and injections and that they are treated exactly as if they were shipped with the library is really cool.
I am sure this will lead to interesting innovations in third-party test libraries.

### Java Version

This is still a little unclear (at least to me).
While the API is lambda-enabled it seems to do that mostly without requiring types or features from Java 8.
It is [also being considered](https://github.com/junit-team/junit-lambda/wiki/Core-Principles#jdk-level) to avoid using Java 8 features inside JUnit so that the project can be compiled against older versions.
If so, it could be used in environments that did not or can not upgrade (e.g. Android).

### Compatibility With JUnit 4

The project dedicates a separate page to address this important topic.

> Instead, JUnit 5 provides a gentle migration path via a JUnit 4 test engine which allows existing tests based on JUnit 4 to be executed using the JUnit 5 infrastructure.
Since all classes and annotations specific to JUnit 5 reside under a new org.junit.gen5 base package, having both JUnit 4 and JUnit 5 in the classpath does not lead to any conflicts.
It is therefore safe to maintain existing JUnit 4 tests alongside JUnit 5 tests.
>
> [JUnit Lambda Wiki](https://github.com/junit-team/junit-lambda/wiki/Prototype-JUnit4-Run-And-Migrate)

## Reflection

We have seen the prototype's basic features:

-   setup, test, teardown: better naming
-   assertions: slight improvements
-   visibility: no more `public`!
-   lifecycles: a per-class lifecycle keeping state between tests
-   inner classes: direct support for tests in inner classes
-   assumptions: slight improvements
-   custom annotations: enables fully compatible custom annotations
-   conditions: enables fanciful skipping of tests
-   injection: support for injecting instances via test parameters

Especially the last items show the core principle of extensibility.
We also discussed the focus on migration compatibility, which will allow projects to execute tests using both JUnit 4 and JUnit 5.

With all of this you are prepared to make up your opinion about the details and (important step!) give feedback to the great people doing this for us:

-   [GitHub issue tracker](https://github.com/junit-team/junit-lambda/issues)
-   [Twitter](https://twitter.com/junitlambda)

By the way, if you'd follow [me on Twitter](https://twitter.com/nipafx), you would've known most of this since last Friday.

https://twitter.com/nipafx/status/667615700408750080

Just saying...
