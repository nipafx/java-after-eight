---
title: "JUnit 5 - Dynamic Tests"
tags: [junit-5, lambda, libraries, testing]
date: 2018-08-05
slug: junit-5-dynamic-tests
description: "With JUnit 5's dynamic tests, we can create tests at run time, for example to parameterize tests, create hierarchical test plans, or define tests with lambdas."
intro: "With dynamic tests, JUnit 5 allows us to create tests at run time. With this we can parameterize tests. generate hierarchical test plans, and even define tests with lambdas!"
searchKeywords: "dynamic tests"
featuredImage: junit-5-dynamic-tests
repo: junit-5-demo
---

With JUnit 5's dynamic tests it is possible to define fully fledged test cases at run time.
This way, tests can be created from parameters, external data sources, or simple lambda expressions.
They are particularly suitable for hierarchical data.
This fixes a long-standing weakness of JUnit 4, where tests had to be defined at compile time.

## Static Tests In JUnit 4

JUnit 3 identified tests by parsing method names and checking whether they started with `test`.
JUnit 4 took advantage of the (then new) annotations and introduced `@Test`, which gave us much more freedom.
Both of these techniques share the same approach: Tests are defined at compile time.

This can turn out to be quite limiting, though.
Consider, for example, the common scenario that the same test is supposed to be executed for a variety of input data, in this case for many different points:

```java
void testDistanceComputation(Point p1, Point p2, double distance) {
	assertEquals(distance, p1.distanceTo(p2));
}
```

What are our options?
The most straightforward one is to create a number of interesting points and then simply call our test method in a loop:

```java
@Test
void testDistanceComputations() {
	List<PointPointDistance> testData = createTestData();
	for (PointPointDistance datum : testData) {
		testDistanceComputation(
			datum.point1(), datum.point2(), datum.distance());
	}
}
```

If we do that, though, JUnit will see our loop as a single tests.
This means that tests are only executed until the first fails, reporting suffers, and tool support is generally sub par.

There are a couple of JUnit 4 features and extensions that address this issue.
They all more or less work but are often limited to a specific use case ([Theories](https://github.com/junit-team/junit4/wiki/Theories)), are awkward to use ([Parameterized](https://github.com/junit-team/junit4/wiki/Parameterized-tests)), and usually require a runner (like the commendable [JUnitParams](https://github.com/Pragmatists/JUnitParams)).
The reason is that they all suffer from the same limitation: JUnit 4 does not really support creating tests at run time.

The same applies to creating tests with lambdas.
Some would like to define tests like this:

```java
class PointTest {

	"Distance To Origin" -> {
		Point origin = Point.create(0,0);
		Point p = Point.create(3,4);
		assertEquals(5, origin.distanceTo(p));
	}

}
```

This is of course just an ideal - it does not even compile in Java.
Nevertheless, it would be interesting to see how close we can get.
Alas, individual lambdas can not be statically identified, either, so the same limitation applies here.

But I wouldn't be writing all of this if JUnit 5 did not propose a solution: Dynamic tests to the rescue!

## Dynamic Tests

Jupiter, [JUnit 5's primary test API](junit-5-architecture-jupiter#splitting-junit-5), offers a few classes and an annotation that together address our problem.

### DynamicTest, DynamicContainer, and @TestFactory

First, there are `DynamicTest` and `DynamicContainer`.
The former is a simple wrapper class for a single test and the latter a just-as-simple wrapper for a bunch of dynamic tests.

A `DynamicTest` stores the test's name as a `String` and its code as an `Executable` - that's like a `Runnable` that can throw a `Throwable` (the naming is formidable).
It is created with a static factory method:

```java
public static DynamicTest dynamicTest(String name, Executable test);
```

A `DynamicContainer` is even simpler.
It stores a name and a bunch of dynamic tests.
It also comes with a static factory method:

```java
public static DynamicContainer dynamicContainer(
		String name, Iterable<DynamicNode> dynamicNodes);
```

(There is also an overload that accepts a `Stream<DynamicNode>`.)

Then there is `@TestFactory`, which can annotate methods.
Those methods must return an `Iterator`, `Iterable` (this includes all collections), or `Stream` of dynamic nodes.
(This can of course not be enforced at compile time so JUnit will barf at run time if we return something else.)

```java
@TestFactory
List<DynamicTest> testPointsDynamically() {
	return List.of(
		DynamicTest.dynamicTest(
			"A Great Test For Point",
			() -> {
				// test code
			}),
		DynamicTest.dynamicTest(
			"Another Great Test For Point",
			() -> {
				// test code
			})
	);
}
```

It is easy to guess how this works:

1. When looking for `@Test` methods, JUnit also discovers `@TestFactory` methods.
2. While building the test tree, it executes these methods and add the generated tests to the tree.
3. Eventually, the tests are executed.

We are hence able to dynamically create tests at run time.
And the cool thing is, tools won't know the difference and report on each dynamic test individually.

<contentimage slug="junit-5-dynamic-point-tests-output" options="narrow"></contentimage>

### Lifecycle

The current implementation of dynamic tests is deliberately "raw".
One of the ways this shows is that they are not integrated into the lifecycle.
From the user guide:

> This means that @BeforeEach and @AfterEach methods and their corresponding extension callbacks are not executed for dynamic tests.
In other words, if you access fields from the test instance within a lambda expression for a dynamic test, those fields are not reset by callback methods or extensions between the execution of dynamic tests generated by the same @TestFactory method.

There is an [issue to address this](https://github.com/junit-team/junit5/issues/378), though.

## Parameterized Tests

To create parameterized tests, we do something similar to the earlier approach, where we simply looped over the data and called the test method with it:

```java
@TestFactory
Stream<DynamicTest> testDistanceComputations() {
	List<PointPointDistance> testData = createTestData();
	return testData.stream()
		.map(datum -> DynamicTest.dynamicTest(
			"Testing " + datum,
			() -> testDistanceComputation(
				datum.point1(), datum.point2(), datum.distance())));
}
```

The critical difference to what we did earlier is that we do not directly execute `testDistanceComputation` anymore.
Instead we create a dynamic test for each datum, so JUnit understands that these are many tests and not just one.

In cases like this we can use another method to generate dynamic tests:

```java
@TestFactory
Stream<DynamicTest> testDistanceComputations() {
	return DynamicTest.stream(
		createTestData().iterator(),
		datum -> "Testing " + datum,
		datum -> testDistanceComputation(
			datum.point1(), datum.point2(), datum.distance()));
}
```

Here we hand our test data to `stream` and then tell it how to create names and tests from that.

Note that for the particular case of running test methods with different parameters, JUnit Jupiter offers a better approach than dynamic tests.
It actually [supports parameterized tests out of the box](junit-5-parameterized-tests), which is implemented with [the extension point `TestTemplateInvocationContextProvider`](https://github.com/junit-team/junit5/blob/master/junit-jupiter-api/src/main/java/org/junit/jupiter/api/extension/TestTemplateInvocationContextProvider.java).

Parameterized tests are *flat*, though.
If test data is inherently hierarchical, dynamic tests offer a great way to organize them.

## Hierarchical Tests

Imagine you have a hierarchical data structure (could be JSON or a POJO tree) and want to generate a test case for each element in that structure.
Then a combination of dynamic containers and dynamic tests lets you organize these tests in a way that closely resembles the data structure, making it much easier to navigate between the two.
Going into detail on how to do this deserves its own post, but let's walk through it on a conceptual level.

Let's call the individual things you want to create tests for *nodes* (it could help to think of the entire data structure as a tree).
When walking the structure, maybe by iteration, recursion, or visitor pattern, you're gonna do three things for each node:

-   create `DynamicTest` instances to test the node's behavior
-   create a `DynamicContainer` for each of the node's children
-   create a `DynamicContainer` for the node itself to wrap the previously created tests and containers

This gives you a one-to-one relationship of nodes in your data structure to dynamic containers in the resulting test tree, where each container holds the tests that apply to the corresponding node.

Here is [a simplified example](https://github.com/nipafx/demo-junit-5/blob/master/src/test/java/org/codefx/demo/junit5/dynamic/ArithmeticTreeTest.java) for recursively creating that test tree:

```java
// ArithmeticTreeTestData know the test results for each node
private static DynamicNode generateTestTreeFor(
		ArithmeticNode node, ArithmeticTreeTestData testData) {
	// generate tests for the node itself, then for its children...
	var testsForNode = generateTestsFor(node, testData);
	var testsForChildren =
		generateTestsFor(node.operands(), testData);
	// ... then put them together into a container
	var displayName = generateNameFor(node, testData);
	return DynamicContainer.dynamicContainer(
		displayName, concat(testsForNode, testsForChildren));
}

private static Stream<DynamicNode> generateTestsFor(
		ArithmeticNode node, ArithmeticTreeTestData testData) {
	// straightforward: generate tests as needed with
	// DynamicTest.dynamicTest and return a stream of them
}

private static Stream<DynamicNode> generateTestsFor(
		List<ArithmeticNode> nodes,
		ArithmeticTreeTestData testData) {
	// recurse to the children
	return nodes.stream()
			.map(node -> generateTestTreeFor(node, testData));
}
```

With code like that I got the following output:

<contentimage slug="junit-5-hierarchical-test-output" options="narrow"></contentimage>

The cool thing is, if a node's correct behavior depends on its children's correct behavior, you can follow the path of failed tests to the root cause.
In this case, the wrong final result ("Addition should evaluate to 46") was apparently caused by the addition of 42 and 4 ("Addition of operands should evaluate to 46"), whereas everything up to then went fine.
If the addition of 2 and 5 would have created the wrong result, a lot more tests would be yellow.

Nice, eh?

## Lambda Tests

Ok, let's see how close we can get to the much-coveted lambda tests.
Now, dynamic tests were not explicitly created for this so we have to tinker a bit.
(This tinkering is, err, "heavily inspired" by one of [Jens Schauder](http://schauderhaft.de/)'s [presentations about JUnit 5](http://blog.schauderhaft.de/junit-lambda-talk/junit.html).
Thanks Jens!)

A dynamic test needs a name and an executable and it sounds reasonable to create the latter with a lambda.
To be able to do do this, though, we need a target, i.e.
something the lambda is assigned to.
A method parameter comes to mind...

But what would that method do?
Obviously it should create a dynamic test but then what?
Maybe we can dump that test somewhere and have JUnit pick it up later?

```java
public class LambdaTest {

	private final List<DynamicTest> tests = new ArrayList<>();

	// use a lambda to create the 'Executable'
	public void registerTest(String name, Executable test) {
		tests.add(DynamicTest.dynamicTest(name, test));
	}

	// JUnit collects all registered tests when calling this method
	@TestFactory
	void List<DynamicTest> tests() {
		return tests;
	}

}
```

Ok, that looks promising.
But where do we get an instance of `LambdaTest`?
The easiest solution would be for our test class to simply extend it and then repeatedly call `registerTest`.
If we do so, we might prefer a shorter name, though; and we can also make it protected:

```java
// don't do this at home!
protected void λ(String name, Executable test) {
	tests.add(DynamicTest.dynamicTest(name, test));
}
```

Looks like we're getting there.
All that's left is to call `λ` and the only apparent way to do this in time for JUnit to pick up the tests is from inside the test class' constructor:

```java
class PointTest extends LambdaTest {

	public PointTest() {
		λ("A Great Test For Point", () -> {
			// test code
		})
	}

}
```

We're done tinkering.
To get further, we have to start hacking.
Ever heard of [double brace initialization](http://c2.com/cgi/wiki?DoubleBraceInitialization)?
This is a somewhat strange feature that uses an initializer block to execute code during construction.
(I used to think that this creates an anonymous subclass, but that's not the case in this scenario as [Duncan](junit-5-dynamic-tests)<!-- comment-2817548442 --> and, in more detail, [Reinhard](https://reinhard.codes/2016/07/30/double-brace-initialisation-and-java-initialisation-blocks/) pointed out.) With it, we can go further:

```java
class PointTest extends LambdaTest {{

	λ("A Great Test For Point", () -> {
		// test code
	});

}}
```

If we're really eager, we can shave off another few characters.
With [this one weird trick](http://benjiweber.co.uk/blog/2015/08/17/lambda-parameter-names-with-reflection/), we can determine a lambda's parameter name via reflection and use that as the test's name (we're now being inspired by [Benji Weber](https://twitter.com/benjiweber); note that this trick only works on 8u60+, but [stopped working on Java 9](https://bugs.openjdk.java.net/browse/JDK-8138729)).
To take advantage of that we need a new interface and have to change `LambdaTest::λ` a bit:

```java
@FunctionalInterface
// the interface we are extending here allows us
// to retrieve the parameter name via 'prettyName()'
// (the black magic is hidden inside that method;
//  look at 'MethodFinder' and 'NamedValue' in Benji's post)
public interface NamedTest extends ParameterNameFinder {
	void execute(String name);
}

protected void λ(NamedTest namedTest) {
	String name = namedTest.prettyName();
	Executable test = () -> namedTest.execute(name);
	tests.add(DynamicTest.dynamicTest(name, test));
}
```

Putting it all together we can create tests as follows:

```java
class PointTest extends LambdaTest {{

	λ(A_Great_Test_For_Point -> {
		// test code
	});

}}
```

What do you think?
Is it worth all that hacking?
To be honest, I don't mind having my IDE generate test method boilerplate so my answer would be "No".
But it was a fun experiment.
:)

## Reflection

So what have we seen?
Up to now JUnit only knew about tests that were declared at compile time.
JUnit 5 has a concept of dynamic tests, which are created at run time and consist of a name and an executable that holds the test code.
With that we have seen how we can create parameterized tests, mirror hierarchical data structures in our test plan, and use lambdas to define tests in a more modern style.

What do you think?
Eager to try it out?
