---
title: "Teeing Collector in Java 12"
tags: [java-12, streams]
date: 2019-03-04
slug: java-12-teeing-collector
description: "The teeing collector, available since Java 12 as Collectors::teeing, forwards its input to two other collectors before merging their results with a function."
searchKeywords: "teeing"
featuredImage: teeing-collector
repo: java-x-demo
---

Java 12 comes out in two weeks and, [with `switch` expressions](java-13-switch-expressions), takes the first step towards pattern matching.
But the new release has more to offer than that - well, a little bit more.
It also introduces the "teeing collector" as a static method `Collectors::teeing`.
Just like [the linux command `tee`](https://en.wikipedia.org/wiki/Tee_(command)), this collector forwards its input to two other collectors before merging their results with a function:

```java
List<Integer> numbers = // ...
Range<Integer> range = numbers.stream()
	.collect(teeing(
		minBy(Integer::compareTo),
		maxBy(Integer::compareTo),
		Range::ofOptional));
```

Easy enough, but read on if you want to learn more.

## Motivation

It's an unfortunate effect of Java's verbosity and lack of tuples that it is often cumbersome to have a stream pipeline operate on two pieces of information at the same time.
Here, I want to filter out those megacorps for which I don't have a valid address:

```java
Map<Megacorp, Address> headquarters = // ...
List<Megacorp> megacorps = // ...
Optional<Megacorp> firstWithValidHq = megacorps.stream()
	// we stream megacorps, but need to add addresses ...
	.map(megacorp -> { new Object() {
		Megacorp _corp = megacorp;
		Optional<Address> _hq =
			Optional.ofNullable(headquarters.get(megacorp));
	})
	// ... only for evaluation, though ...
	.filter(o -> o._hq.isPresent())
	.filter(o -> isValid(o._hq.get()))
	// ... in the end we can get rid of them again
	.map(o -> o._corp)
	// possible further processing ensues
	.findAny();
```

There are different ways to handle this (in this case, I'm using an anonymous class), but none of them are particularly elegant.

In the second example, the situation occurs at the end of the pipeline.
I have a stream of numbers and want to compute the `Range`, which is a simple object referencing just the smallest and greatest number:

```java
List<Integer> numbers = // ...
Range<Integer> range = numbers.stream()
	.reduce(
		// the initial range - parameters are `min` and `max`
		// in that order, so this range is empty
		Range.of(Integer.MAX_VALUE, Integer.MIN_VALUE),
		// combining an existing range with the next number from the stream
		(_range, number) -> {
			int newMin = Math.min(number, _range.min());
			int newMax = Math.max(number, _range.max());
			return Range.of(newMin, newMax);
		},
		// combining two ranges (needed at the end of a parallel stream)
		(_range1, _range2) -> {
			int newMin = Math.min(_range1.min(), _range2.min());
			int newMax = Math.max(_range1.max(), _range2.max());
			return Range.of(newMin, newMax);
		});
```

Don't worry if you can't immediately make full sense of it - the `reduce` overload I use here is not the easiest one around.
And the details don't even matter.
My point is that handling two pieces of information at the same time (in this case the minimum and maximum) is more complicated than seems necessary.
Formally put, the solution's incidental complexity overshadows the problem's inherent complexity.

With Java 12 we get a tool that makes the latter problem, collecting two pieces of information at the end of a stream, more comfortable.

## Teeing Collector

On `Collectors` there's a new static method `teeing` that accepts two collectors and a function to merge their results.
This collector feeds each stream element into both collectors and, when the stream is exhausted, tells the collectors to finalize their results before it uses the provided function to merge them.
We'll discuss details in a second, but beforehand, let's recreate the example from the introduction.

### Teeing To Minimum And Maximum To Create A Range

Here's how we can compute the range with `Collectors::teeing`:

```java
List<Integer> numbers = // ...
Range<Integer> range = numbers.stream()
	.collect(Collectors.teeing(
		// first collector collects the minimum
		Collectors.minBy(Integer::compareTo),
		// second collector collects the maximum
		Collectors.maxBy(Integer::compareTo),
		// now we need to merge their results,
		// both of which are `Optional<Integer>`;
		// I created a static factory method for that
		Range::ofOptional))
```

Comments aside, this is *much* shorter and more readable than the pre-Java-12 solution and it gets even better with static imports:

```java
List<Integer> numbers = // ...
Range<Integer> range = numbers.stream()
	.collect(teeing(
		minBy(Integer::compareTo),
		maxBy(Integer::compareTo),
		Range::ofOptional))
```

This example is particularly powerful because it reuses existing collectors.
And that's no coincidence, either.
One of the problems with collecting two pieces of information used to be that, even if you needed the exact functionality of an existing collector for one or even both of them, you could not apply that already-implemented solution and had to rewrite it instead.
Fortunately, from Java 12 onwards that's no longer the case.

To make sure we fully understand the signature of `Collectors::teeing` and how it handles stream characteristics, let's have a look at those next.

### Method Signature

I usually don't go into method signatures, but this one has a few type parameters that you need to line up correctly, so it may be helpful to take a look.
(Also, I've got words to spare.
😉)

Formally, collectors are of type `Collector<T, A, R>`.
As you can see, they have three type parameters:

-   The first parameter is the type of elements that go into the collector.
This is also the type of the stream's elements, so for a `Stream<T>`, it's `T` or a supertype thereof.
-   The second parameter is the type of the intermediate data structure the collector uses to accumulate elements.
This type is just a technical requirement and usually not exposed, so it mostly shows up as `?`.
-   The third parameter is the type of the result that the collector produces after merging and finalizing intermediate results.

As an example, this is the signature of `Collectors::toList`:

```java
public static <T> Collector<T, ?, List<T>> toList()
```

Makes sense, right?
Now, here's the signature of `Collectors::teeing`:

```java
public static <T, R1, R2, R> Collector<T, ?, R> teeing(
	Collector<? super T, ?, R1> downstream1,
	Collector<? super T, ?, R2> downstream2,
	BiFunction<? super R1, ? super R2, R> merger);
```

Since we're operating on a stream of `T`, it makes sense that both collectors accept that (or a more general type) as input.
Their intermediate result types don't matter, but they produce different end results: `R1` and `R2`.
And that (or, once again, more general types) is what the `merger` function takes as input to produce the final result of type `R`.

### Stream Characteristics

For the stream API to be both correct and fast, it has to juggle a lot of information in the background.
One such piece of information are stream characteristics, which govern what kinds of optimizations are possible.
Collectors have an impact here:

-   A collector can be `CONCURRENT`, which means it can accumulate elements across several threads without the stream API having to bother about synchronization.
-   A collector can be `UNORDERED` (e.g. when collecting a `Set`).
Since the collector doesn't preserve order anyway, the stream API doesn't have to bother in which order it feeds elements into it.
-   A collector can have an `IDENTITY_FINISH`, which means the intermediate result (of type `A`) can be the final result (of type `R`) without explicit finalization (`A` must be a subtype of `R`).

A teeing collector is `CONCURRENT` and/or `UNORDERED` if both collectors are and never has an `IDENTITY_FINISH` because it always needs to merge the two collector's results.

## Reflection

And that's all about `Collectors::teeing`!
☀️ In summary:

-   use `teeing` to collect two distinct results at the end of a stream pipeline
-   try to reuse existing collectors for that
-   keep the type parameters `T`, `R1`, `R2`, and `R` in mind to line them up correctly
-   as usual, rely on the stream API to infer the correct characteristics

**PS**: If you want to join me in figuring these things out, [head over to Twitch](http://twitch.tv/nipafx/) where I live-stream my experiments with new stuff, like the one with Java 12 ([preserved on YouTube](https://www.youtube.com/watch?v=TiObH-1NtNY)).
