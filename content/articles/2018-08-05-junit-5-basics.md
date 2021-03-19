---
title: "JUnit 5 Basics: `@Test`, Lifecycle, Assertions, Assumptions, And More"
tags: [java-basics, junit-5, libraries, testing]
date: 2018-08-05
slug: junit-5-basics
description: "The Basics of JUnit 5: How to use `@Test`, `@BeforeAll`, `@BeforeEach`, `@AfterEach`, `@AfterAll`, assertions, and assumptions. How to disable, name, and tag tests."
intro: "Get to know the basics of JUnit 5: `@Test`, lifecycle methods, assertions, and assumptions; how to disable, name, and tag tests; as well as previews on nesting, parameterization, and test interfaces. Let's write some tests!"
searchKeywords: "JUnit 5"
featuredImage: junit-5-basics
repo: junit-5-demo
---

After [setting JUnit 5 up](junit-5-setup), we can write some basic tests, getting to know the test lifecycle, assertions, and assumptions as well as some more advanced features like test interfaces, disabled, tagged, nested, and parameterized tests.

## Philosophy

The [new architecture](junit-5-architecture-jupiter), which is not terribly important at this moment, is aimed at extensibility.
It is possible that some day very alien (at least to us run-of-the-mill Java devs) testing techniques will be possible with JUnit 5.

But for now the basics are very similar to version 4.
JUnit 5's surface undergoes a deliberately incremental improvement and developers should feel right at home.
At least I do and I think you will, too:

<pullquote>The basics are very similar to JUnit 4</pullquote>

```java
class Lifecycle {

	@BeforeAll
	static void initializeExternalResources() {
		System.out.println("Initializing external resources...");
	}

	@BeforeEach
	void initializeMockObjects() {
		System.out.println("Initializing mock objects...");
	}

	@Test
	void someTest() {
		System.out.println("Running some test...");
		assertTrue(true);
	}

	@Test
	void otherTest() {
		assumeTrue(true);

		System.out.println("Running another test...");
		assertNotEquals(1, 42, "Why would these be the same?");
	}

	@Test
	@Disabled
	void disabledTest() {
		System.exit(1);
	}

	@AfterEach
	void tearDown() {
		System.out.println("Tearing down...");
	}

	@AfterAll
	static void freeExternalResources() {
		System.out.println("Freeing external resources...");
	}

}
```

See?
No big surprises.

## The Basics Of JUnit 5

We'll now go through the details of what we just saw (visibility, test lifecycle, and assertions) and discuss some related features (assumptions and test instances).

### Visibility

The most obvious change is that test classes and methods do not have to be public anymore.
Package visibility suffices but private does not.
I think this is a sensible choice and in line with how we intuit the different visibility modifiers.

<pullquote>Package visibility suffices</pullquote>

Great!
I'd say, less letters to type but you haven't been doing that manually anyways, right?
Still less boilerplate to ignore while scrolling through a test class.

### Test Lifecycle

#### @Test

The most basic JUnit annotation is `@Test`, which marks methods that are to be run as tests.
It is virtually unchanged, although it no longer takes optional arguments: [Expected exceptions](http://junit.org/junit4/javadoc/latest/org/junit/Test.html#expected%28%29) and [timeouts](http://junit.org/junit4/javadoc/latest/org/junit/Test.html#timeout%28%29) have to be verified with [assertions](#Assertions).

#### Before And After

You might want to run code to set up and tear down your tests.
There are four method annotations to help you do that:

`@BeforeAll`
:   Executed once; runs before the tests and methods marked with `@BeforeEach`.

<pullquote>Lifecycle annotations work exactly like in JUnit 4.</pullquote>

`@BeforeEach`
:   Executed before each test.

`@AfterEach`
:   Executed after each test.

`@AfterAll`
:   Executed once; runs after all tests and methods marked with `@AfterEach`.

These annotations work exactly like their similarly named siblings in JUnit 4.

The order in which different methods within the same class that bear the same annotation are executed is undefined.
The same is not true for inherited methods with the same annotation, which are executed in a top-down fashion for lifecycle methods that are executed *before* a test and bottom-up for those running *after* a test.

By default, a new instance is created for each test, so there is no obvious instance on which to call the `@BeforeAll`/`@AfterAll` methods.
In that case they have to be static.

#### Test Class Lifecycle

When talking about test instances just now, I said that JUnit creates a new one for each method *by default*.
That means tests can not share state via non-static fields of the test class and this has been true for every JUnit since the first.

Other testing frameworks, for example TestNG, have a different approach, though, and use the same instance for all tests in the class.
Personally, I don't think that's a good default, considering the very real risk of horrible inter-test-dependencies.
On the other hand, I have never used it and some people stick to TestNG for just that reason, so I may be tragically wrong

Be that as it may, with JUnit 5 you can switch to having just a single instance by putting `@TestInstance(Lifecycle.PER_CLASS)` on your test class.
If you want to use it by default, you can [configure](https://junit.org/junit5/docs/current/user-guide/#running-tests-config-params) that with the property `junit.jupiter.testinstance.lifecycle.default=per_class`.

<pullquote>You can switch to a single instance for all test methods</pullquote>

### Assertions

If `@Test`, `@Before...`, and `@After...` are a test suite's skeleton, assertions are its flesh.
After the instance under test was prepared and the functionality to test was executed on it, assertions make sure that the desired properties hold.
If they don't, they fail the running test.

#### Classic

Classic assertions either check a property of a single instance (e.g. that it is not null) or do some kind of comparison (e.g. that two instances are equal).
In both cases they optionally take a message as a last parameter, which is shown when the assertion fails.
If constructing the message is expensive, it can be specified as a lambda expression, so construction is delayed until the message is actually required.

```java
@Test
void assertWithBoolean_pass() {
	assertTrue(true);
	assertTrue(this::truism);

	assertFalse(false, () -> "Really " + "expensive " + "message" + ".");
}

boolean truism() {
	return true;
}

@Test
void assertWithComparison_pass() {
	List<String> expected = asList("element");
	List<String> actual = new LinkedList<>(expected);

	assertEquals(expected, actual);
	assertEquals(expected, actual, "Should be equal.");
	assertEquals(expected, actual, () -> "Should " + "be " + "equal.");

	assertNotSame(expected, actual, "Obviously not the same instance.");
}
```

As you can see, JUnit 5 doesn't change much here.
The names are the same as before and comparative assertions still take a pair of an expected and an actual value (in that order).

That the expected-actual order is so critical in understanding the test's failure message and intention, but can be mixed up so easily, is a big blind spot.
There's no way to fix this, though, short of creating a new assertion framework.
Considering big players like [Hamcrest](http://hamcrest.org/JavaHamcrest/) (ugh!) or [AssertJ](http://joel-costigliola.github.io/assertj/) (yeah!), this would not have been a sensible way to invest the limited time.
Hence the goal was to keep the assertions focused and effort-free.

New is that failure message come last.
I like it because it keeps the eye on the ball, i.e.
the property being asserted.
As a nod to Java 8, Boolean assertions now accept [suppliers](https://docs.oracle.com/javase/8/docs/api/java/util/function/BooleanSupplier.html), which is a nice detail.

#### Extended

Aside from the classical assertions that check specific properties, there are a couple more interesting ones.

The first is not even a real assertion, it just fails the test with a failure message.

```java
@Test
void failTheTest() {
	fail("epicly");
}
```

Then we have `assertAll`, which takes a variable number of assertions and tests them all before reporting which failed (if any).

```java
@Test
void assertAllProperties_fail() {
	Address address = new Address("New City", "Some Street", "No");

	assertAll("address",
			() -> assertEquals("Neustadt", address.city),
			() -> assertEquals("Irgendeinestraße", address.street),
			() -> assertEquals("Nr", address.number)
	);
}
```

```
org.opentest4j.MultipleFailuresError: address (3 failures)
	expected: <Neustadt> but was: <New City>
	expected: <Irgendeinestraße> but was: <Some Street>
	expected: <Nr> but was: <No>
```

This is great to check a number of related properties and get values for all of them as opposed to the common behavior where the test reports the first one that failed and you never know the other values.

To compare collections you can use `assertArrayEquals` and `assertIterableEquals`, which work like you would expect: the given arrays or iterables need to contain the same number of elements and these elements must be pairwise equal in the order in which they are encountered.

A special case of comparing collections is made for lists of strings.
The use case are log messages or other textual reporting results that need to be compared to verify a system is running as expected.
In it's simplest case it compares the string lists element by element, but it can also do regular expression matching (where `expected` acts as the regex) or fast forwarding.

```java
assertLinesMatch(
	asList("first", ">> skipped until next match >>", "V", "last"),
	asList("first", "I", "II", "III", "IV", "V", "last"));
```

This feature was first developed internally to test [the console launcher](junit-5-setup#command-line-for-the-win) and verify whether it creates the correct output.

Then we have `assertThrows`, which fails the test if the given method does not throw the specified exception.
It also returns the exception instance so it can be used for further verification, for example to check whether the message contains certain information.

```java
@Test
void assertExceptions_pass() {
	Exception exception = assertThrows(Exception.class, this::throwing);
	assertEquals("Because I can!", exception.getMessage());
}
```

Finally, I want to point you towards two assertions that deal with a test's run time: `assertTimeout` fails a test if the code handed to it runs too long and `assertTimeoutPreemptively` even aborts it once the time is up:

```java
@Test
void assertTimeout_runsLate_failsButFinishes() {
	assertTimeout(of(100, MILLIS), () -> {
		sleepUninterrupted(250);
		// you will see this message
		System.out.println("Woke up");
	});
}

@Test
void assertTimeoutPreemptively_runsLate_failsAndAborted() {
	assertTimeoutPreemptively(of(100, MILLIS), () -> {
		sleepUninterrupted(250);
		// you will NOT see this message
		System.out.println("Woke up");
	});
}
```

Together, `assertThrows` and `assertTimeoutPreemptively` replace the `expected` and `timeout` attributes of JUnit 4's `@Test` annotation.

#### Alternatives

The communication between assertions and the test framework is usually very loose and happens via exceptions.
JUnit 5 keeps this approach, which means alternative assertion libraries like Hamcrest, AssertJ, or Google Truth work in JUnit 5 without changes.

At the same time, JUnit 5 does not depend on any of these (unlike JUnit 4, which depends on Hamcrest), so you have to add your favorite as a test-scoped dependency.

### Assumptions

Assumptions allow you to specify certain preconditions for a test and skip it if they are not fulfilled.
This can be used to reduce the run time and verbosity of test suites, especially in the case of failure.

```java
@Test
void exitIfFalseIsTrue() {
	assumeTrue(false);
	System.exit(1);
}

@Test
void exitIfTrueIsFalse() {
	assumeFalse(this::truism);
	System.exit(1);
}

private boolean truism() {
	return true;
}

@Test
void exitIfNullEqualsString() {
	assumingThat(
			"null".equals(null),
			() -> System.exit(1)
	);
}
```

Assumptions can either be used to abort tests whose preconditions are not met or to execute (parts of) a test only if a condition holds.
The main difference is that aborted tests (the first two) are reported as disabled, whereas a test that was empty because a condition did not hold (the last one) is plain green.

## Universal Mechanisms

There are a few cross cutting features that you can apply everywhere in JUnit 5.
They may not be the most thrilling ones, but they're very useful and you should definitely know about them.

### Disabling Tests

It's Friday afternoon and you just want to go home?
No problem, just slap `@Disabled` on the test (optionally giving a reason) and run.

```java
@Test
@Disabled("Y U No Pass?!")
void failingTest() {
	assertTrue(false);
}
```

Much more often then roundly deactivating a test you may want to disable it under certain conditions, say on a specific operating system or Java version:

```java
@Test
@DisabledOnOs(OS.WINDOWS)
@DisabledOnJre(JRE.JAVA_8)
void someTest() { /*...*/ }
```

This should get you started.
If you're looking for more details head over to [my post on enabling/disabling tests with included and custom conditions](junit-5-disabled-conditions).

### Naming Tests

JUnit 5 comes with an annotation `@DisplayName`, which gives developers the possibility to have more readable names for their test classes and methods:

```java
@DisplayName("What a nice name...")
class NamingTest {

	@Test
	@DisplayName("... for a test")
	void test() { }

}
```

This creates very readable output (for example, in your IDE), but I have to admit that I rarely use it - way too often it's just the method name with spaces instead of camel case or underscores and that doesn't add enough value for me.

<contentimage slug="junit-5-basics-named-test" options="narrow"></contentimage>

### Tagging Tests

Not all tests are created equal.
Some are blazingly fast and you want to run them all the time, others... not so much.
Database tests, front-end tests, end-to-end tests, they typically take their time.
By tagging tests, you can identify groups that share certain characteristics and tell your build tool or IDE to only run some of them.

Tagging itself is easy...

```java
@Tag("unit")
class UserTest { }

@Tag("db")
class UserRepositoryTest { }

@Tag("integration")
class UserServiceTest { }
```

... and configuring tools is not much more complicated, but I will go into the ins and outs as well as how to get the most out of this feature in another post.
For now I'll leave you with links to how-tos for [Maven](https://maven.apache.org/surefire/maven-surefire-plugin/examples/junit.html) (search for *JUnit Categories*, it uses the same mechanism), [Gradle](https://docs.gradle.org/4.6/release-notes.html#junit-5-support), [IntelliJ](https://www.jetbrains.com/help/idea/run-debug-configuration-junit.html) (search for *@Tag*), and [Eclipse](https://www.eclipse.org/community/eclipse_newsletter/2017/october/article5.php) (search for *Tagging and filtering*).

## Preview On Advanced Features

With `@Test`, the lifecycle methods, assertions, assumptions, and the universal mechanisms you're good to go and can start writing tests with JUnit 5.
On the other hand, this were really just the basics and I don't want to end on them because they make look JUnit 5 rather boring.
So let's look at a few more interesting features!

### Test Interfaces

All we've seen so far, and much of [what's about to come](tag:junit-5) can not only happen in classes, but also in interfaces:

```java
public interface Interface {

	@BeforeAll
	static void beforeAll() { /*...*/ }

	@BeforeEach
	void beforeAll() { /*...*/ }

	@Test
	default void test() { /*...*/ }

	@AfterEach
	void afterEach() { /*...*/ }

	@AfterAll
	static void afterAll() { /*...*/ }

}

class Implementation implements Interface {

	// for this class, JUnit executes the
	// inherited test and lifecycle methods

}
```

Test interfaces are a straightforward approach to testing implementations of interfaces.
All you need to do while developing a new interface (for your production code) is to write a test interface alongside with it.
Then each test class for an implementation of that production interface can implement the respective test interface and get all tests for free.

I think there's an even better way to test interfaces, though, and it has to do with nested tests.
Which are up next.

### Nesting Tests

JUnit 5 makes it near effortless to nest test classes.
Simply annotate inner classes with `@Nested` and all test methods in there are executed as well:

```java
class NestedTest {

	@Test
	void topLevelTest() { /*...*/ }

	@Nested
	class Inner {

		@Test
		void innerTest() { /*...*/ }

		@Nested
		class Innerer {

			@Test
			void innererTest() { /*...*/ }

		}

	}

}
```

I'll explain details in a post focused on nested tests - how they work, how to make good use of them, and how they are great for testing interfaces.

### Parameterized Tests

Last but definitely not least come parameterized tests.
In short, they're awesome in JUnit 5!

```java
@ParameterizedTest
@ValueSource(strings = { "Hello", "JUnit" })
void withValueSource(String word) {
	assertNotNull(word);
}
```

But don't just take my word on it - I've written [an entire post on parameterized tests](junit-5-parameterized-tests).
If your heart is not made of stone, you should give it a read right now.

## Reflection

That's it, you made it!
We've discussed the basics of how to use JUnit 5 and now you know all you need to write plain tests: How to annotate test methods (with `@Test`) and the lifecycle methods (with `@[Before|After][All|Each]`) and how assertions and assumptions work (much like before).

Beyond that we rushed through [conditionally disabling](junit-5-disabled-conditions), naming, nesting, and [parameterizing](junit-5-parameterized-tests) tests.
But wait, there's more!
We didn't yet talk about parameter injection, the [extension mechanism](junit-5-extension-model), or the [project's architecture](junit-5-architecture-jupiter).
(Each link takes you to an article in [this JUnit 5 series](tag:junit-5) that discusses one feature in all detail.)
