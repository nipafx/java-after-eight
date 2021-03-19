---
title: "Test Collection Implementations with Guava"
tags: [collections, libraries, testing]
date: 2015-03-09
slug: test-collection-implementations-guava
description: "Here's how to use Guava-Testlib to easily and thoroughly test your own Java collection implementations."
searchKeywords: "Test Collection Implementation"
featuredImage: test-collection-implementations-with-guava
---

I'm currently adding a new feature to [LibFX](http://libfx.codefx.org/), for which I create some custom collections akin to those from the [Java Collections Framework](https://docs.oracle.com/javase/8/docs/technotes/guides/collections/).
I went looking for tests I could run against them and was delighted to find out that [Google's Guava](https://github.com/google/guava) contains just what I need: a massive test suite which verifies every nook and cranny of my implementation for all collection interfaces from the JDK and Guava.

Let's have a quick look at it.

## Setup

For this to work we need JUnit, the Guava-Testlib and a little boilerplate code.

### Get JUnit

In case you're not already using JUnit in your project, get it [here](http://search.maven.org/#artifactdetails%7Cjunit%7Cjunit%7C4.12%7Cjar).
If you use Maven or Gradle:

```xml
<dependency>
	<groupId>junit</groupId>
	<artifactId>junit</artifactId>
	<version>4.12</version>
	<scope>test</scope>
</dependency>
```

```groovy
testCompile 'junit:junit:4.12'
```

### Get Guava

What we actually need is not Guava itself but the [Guava-Testlib](https://github.com/google/guava/tree/master/guava-testlib).
You can download it from [the central repository](https://search.maven.org/#artifactdetails%7Ccom.google.guava%7Cguava-testlib%7C18.0%7Cjar), which also contains the dependency information for different managers.

For your convenience:

```xml
<dependency>
	<groupId>com.google.guava</groupId>
	<artifactId>guava-testlib</artifactId>
	<version>18.0</version>
	<scope>test</scope>
</dependency>
```

```groovy
testCompile 'com.google.guava:guava-testlib:18.0'
```

### Write Some Boilerplate

Assume you want to write a `MySet` and the corresponding `MySetTest`.

Doing this the JUnit-3.8.x-way, create a method `public static Test suite();`.
JUnit looks for this method and uses it to identify all tests which it will run for that class.
Inside that method create a TestSuite and add the tests we're going to write further down:

```java
public class MySetTest {

	public static Test suite() {
		return new MySetTest().allTests();
	}

	public Test allTests() {
		TestSuite suite =
			new TestSuite("package.name.of.MySetTest");
		suite.addTest(testForOneToWayUseMySet());
		suite.addTest(testForAnotherWayToUseMySet());
		return suite;
	}

}
```

(I did not try to do this with JUnit 4's annotations.
If you did, ping me and I will include it here.)

With this boilerplate in place you can run this class with JUnit, e.g. from inside your IDE or on your CI server.

## Test Your Implementations

Now that that's done we can start actually creating tests for our implementations.
Or, more precisely, tell Guava how to do that for us.
This is a two part process: one creates a generator for the elements in the collection and the unit under test, the other uses one of Guava's test suite builders to create a comprehensive set of tests tailored to the implementation.

We will continue to test an implementation of `Set`.
Below we will see for what other interfaces test suites are available.

### Generator For Elements And The Unit Under Test

The test suite builder requires you to give it a possibility to create the sample elements in the collection and instantiate your collection.
To do this you have to implement the `TestSetGenerator<E>` (where `E` is the type of the elements).

This is straight forward with `order(List<E>)` being the only method which may require some thought.
Note that contrary to the documentation the current versoin of the testlib (18.0) does call this method even when `CollectionFeature.KNOWN_ORDER` is not reported (see below for details about features).
[In my case](https://github.com/nipafx/LibFX/blob/680e87321ab3a7f09920b275e63b673afc3dc98e/src/test/java/org/codefx/libfx/collection/transform/TransformingSetTest.java?ts=4#L83-L86) it suffices to return the insertion order.

### Test Suite Builder

Now this is were the real magic happens.
You take your generator from above, pass it to the correct test suite builder, specify which features your collection has and it will create a tailored and comprehensive suite of tests:

```java
public Test testForOneToWayUseMySet() {
	return SetTestSuiteBuilder
			.using(new MySetGenerator())
			.named("one way to use MySet")
			.withFeatures(
					CollectionSize.ANY,
					CollectionFeature.ALLOWS_NULL_VALUES,
					CollectionFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION,
					CollectionFeature.SUPPORTS_ADD,
					CollectionFeature.SUPPORTS_ITERATOR_REMOVE,
					CollectionFeature.SUPPORTS_REMOVE,
			)
			.createTestSuite();
}
```

#### Features

It is important to specify the correct features.
Take a look at the two enums `CollectionSize` and `CollectionFeatures` to see which possibilities exist to describe your collection's behavior.

Note that the created tests verify the features both ways!
E.g. if `ALLOWS_NULL_VALUES` is left out, the builder will generate tests which verify that adding null to the collection throws a `NullPointerException`.

#### Suppressing Tests

By calling `suppressing` on the builder, you can specify test methods which will not be run.
It seems to exist as a last resort when the features do not suffice to precisely specify the behavior.
I did not use it.

#### Setup & Teardown

If you have to run code before or after each test, you can hand it as a `Runnable` to `withSetUp` or `withTearDown`, respectively (can both be called on the builder).

### Available Test Suites

Of course you can generate tests suites for other interfaces as well.
A first glance yields these possibilities:

Java's collections:

* `Collection`
* `Iterator`
* `List`
* `Map`
* `NavigableMap`
* `NavigableSet`
* `Queue`
* `Set`
* `SortedMap`
* `SortedSet`

Guava's collections:

* `BiMap`
* `ListMultimap`
* `Multimap`
* `Multiset`
* `SetMultimap`
* `SortedMultiset`
* `SortedSetMultimap`

A type search for _\*TestSuiteBuilder_ (note the wildcard) yields some other builders.
I did not investigate them but it is possible that those can be used to create tests for other cases.

In order to use these, simply implement the according `Test...Generator` and hand it to the respective `...TestSuiteBuilder`.

## Reflection

We have seen how to test collection implementations with Guava's Testlib: how to include it and JUnit in our project, what boilerplate we need to make it run and an overview over the generator and test suite builder.
The latter is where all the magic happens as it creates comprehensive tests, tailored to our description of our implementation and its features.
