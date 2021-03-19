---
title: "Faster `flatMap`s with `Stream::mapMulti` in Java 16"
tags: [java-16, streams, performance]
date: 2020-11-04
slug: java-16-stream-mapmulti
description: "Java 16 adds a new method `mapMulti` to `Stream`. It fills the same role as `flatMap`, but is more imperative - and faster."
searchKeywords: "java 16 stream"
featuredImage: java-16-stream-mapMulti
repo: java-x-demo
---

`Stream::flatMap` is declarative:
"Give me a function that maps each element to a stream and I give you a stream of elements back."
The mild annoyance of having to turn collections into streams aside, this works very well.
But it has two drawbacks:

<pullquote>`flatMap` says: "Give me a function that maps to a stream"</pullquote>

* some collections aren't a `Collection` and turning them into a `Stream` can be cumbersome
* creating a lot of small or even empty `Stream` instances can drag performance down


## Enter `Stream::mapMulti`

Java 16 addedd a new stream method:

```java
// plus wildcards
<R> Stream<R> mapMulti​(BiConsumer<T, Consumer<R>> mapper)
```

You call it on a `Stream<T>` to map a single element of type `T` to multiple elements of type `R`.
So far, so `flatMap`, but in contrast to that method, you don't pass a function that turns `T` into `Stream<R>`.
Instead you pass a "function" that receives a `T` and can emit arbitrary many `R`s by passing them to a `Consumer<R>` (that it also receives).
I say "function" because it's actually a bi-consumer that doesn't return anything.

### A Pointless Example

Here's an example where we don't actually do anything:

```java
Stream.of(1, 2, 3, 4)
	.mapMulti((number, downstream) -> downstream.accept(number))
	.forEach(System.out::print);
// prints "1234"
```

Our `BiConsumer<Integer, Consumer<T>>` is called for each element in the stream `[1, 2, 3, 4]` and each time it simply passes the given `number` to the `downstream` consumer.
Hence, each number is mapped to itself and so the resulting stream is also `[1, 2, 3, 4]`.

Pointless.

### An `Optional` Example

It gets a bit more interesting with `Optional`, where the performance-part of the argument against `flatMap` can apply.
[Before we measure that](#streamoptional-performance), let's see how to use it here:

```java
Stream
	.of(Optional.of("0"), Optional.of("1"), Optional.empty())
	.mapMulti(Optional::ifPresent) // !!!
	.forEach(System.out::print);
```

Did you spot the sleek `Optional::ifPresent`?
In case you're wondering why that method reference works here, that's perfectly understandable - it does some heavy lifting.
This is what it would look like with a long-form lambda:

```java
.mapMulti(
	(Optional<String> element, Consumer<String> downstream)
		-> element.ifPresent(downstream))
```

This works because `ifPresent` accepts a `Consumer`, which is what `downstream` happens to be.
So the lambda takes the first argument that it receives (of type `Optional`) and calls a method on it (`ifPresent`) that accepts all the remaining lambda arguments (one of type `Consumer`).
That's exactly what the `Type::method`-style method reference (where `method` is not static) was made for - hence `Optional::ifPresent`.


## The Unfortunate Type Witness

One thing that quickly becomes apparent when you work with `mapMulti` is that it confuses the compiler to the point where generic type inference breaks down.
Let's revisit the first example, but with a slight twist - now we want to collect to a list:

```java
List<Integer> numbers = Stream.of(1, 2, 3, 4)
	.mapMulti((number, downstream) -> downstream.accept(number))
	.collect(toList());
```

Simple, right?
Unfortunately not.

Just like its sister `flatMap`, `mapMulti` changes the stream elements' type and called on a `Stream<T>` returns a `Stream<R>`.
It does that by a passing, as a second argument to the lambda, a `Consumer<R>`.
And what consumer can consume an `Integer`?
A `Consumer<Integer>` of course.
Or a `Consumer<Serializable>`.
Or a `Consumer<Comparable<Integer>>`.
Or the all-powerful `Consumer<Object>`.

<pullquote>That sucks. Solution: add a type witness.</pullquote>

And so the compiler doesn't know what to infer, gives up (translation: "picks `Consumer<Object>`") and the stream returned by `mapMulti` is `Stream<Object>`.
That can't be collected to a `List<Integer>` and so the snippet above gives a compile error:

```
error: incompatible types: inference variable T has incompatible bounds
```

That sucks.
Solution: add a type witness for the `Consumer`'s generic type parameter `R`.

```java
List<Integer> numbers = Stream.of(1, 2, 3, 4)
	.<Integer> mapMulti((number, down) -> down.accept(number))
	.collect(toList());

List<String> strings = Stream
	.of(Optional.of("0"), Optional.of("1"), Optional.of(""))
	.<String> mapMulti(Optional::ifPresent)
	.collect(toList());
```

Not horrible, but makes `mapMulti` a little less enticing.


## `Stream<Optional>` Performance

Ok, let's look at the performance.
I'm no expert at this (so take everything with a pack of salt), but [I did some benchmarks](https://github.com/nipafx/benchmarks#stream-mapmulti) for a `Stream<Optional<Integer>>`.

### Benchmarks

I measured the following methods...

```java
private List<Optional<Integer>> numbers;

@Benchmark
public long flatMap_count() {
	return numbers.stream()
		.flatMap(Optional::stream)
		.count();
}

@Benchmark
public long mapMulti_count() {
	return numbers.stream()
		.mapMulti(Optional::ifPresent)
		.count();
}

@Benchmark
public int flatMap_sum() {
	return numbers.stream()
		.flatMap(Optional::stream)
		.mapToInt(i -> i)
		.sum();
}

@Benchmark
public int mapMulti_sum() {
	return numbers.stream()
		.<Integer> mapMulti(Optional::ifPresent)
		.mapToInt(i -> i)
		.sum();
}
```

... where `numbers` has 10k, 100k, or 1M optionals with 1%, 10%, 50%, or 80% of them empty (distributed randomly).

### Results

I ran three forks, each with three warmup and as many measurement runs per benchmark method.
I gave each method 5 seconds.
System:

* JDK 16-ea+19-985
* JMH 1.23
* Gentoo Linux with 5.8.16 kernel
* Ryzen 9 3900X
* 2 x 16GB G.Skill Trident Z b/w, DDR4-3600

Here are my [raw results](https://github.com/nipafx/benchmarks#stream-mapmulti).

### Interpretation

Trying to make sense of them, I compared otherwise identical configurations of `flatMap` vs `mapMulti`:

* `count` and `sum`
* 10k, 100k, and 1M optionals
* 1%, 10%, 50%, and 80% empty

That's 24 comparisons in total.
Here are the speedups of `mapMulti` over `flatMap`:

| `count` |  10k | 100k |  1M |
| -------:| ----:| ----:| ---:|
| 1%      |  8.2 |  6.3 | 4.9 |
| 10%     | 10.1 |  6.4 | 5.5 |
| 50%     |  6.9 |  3.1 | 0.4 |
| 80%     | 14.0 |  4.7 | 4.8 |
| ---     |  --- |  --- | --- |
| `sum` | **10k** | **100k** | **1M** |
| 1%      |  6.2 |  6.3 | 0.7 |
| 10%     |  6.4 |  6.2 | 0.8 |
| 50%     | 11.5 |  3.3 | 0.7 |
| 80%     | 11.3 |  4.3 | 4.6 |

A speedup > 1 means `mapMulti` is faster so at first glance, this looks pretty good.
But I struggle to make sense of many of these numbers.
For example, what's up with the wide margin and inconsistent impact of the share of empty optionals?

<pullquote>It looks pretty good for `mapMulti`</pullquote>

It's also pretty surprising (to me) that the speedup of `mapMulti` over `flatMap` not only decreases as numbers of elements increase, it even drops below 1, meaning `mapMulti` becomes _slower_ than `flatMap`.
Looking at the raw measurements again, we can see that this is the result of `flatMap` getting some ridiculous speedups at 1 million elements:

| Benchmark      |  %0s |  Size     | Score ± Error (us/op) |
| -------------- | ----:| ---------:|----------------------:|
| flatMap_count  |  0.5 |    10'000 |   101.504 ±   3.363   |
| flatMap_count  |  0.5 |   100'000 |  1150.309 ±  17.065   |
| flatMap_count  |  0.5 | 1'000'000 |  1561.187 ± 324.065   |
| flatMap_sum    | 0.01 |    10'000 |   113.009 ±   6.977   |
| flatMap_sum    | 0.01 |   100'000 |  1158.694 ±  74.973   |
| flatMap_sum    | 0.01 | 1'000'000 |  1622.151 ± 533.694   |
| flatMap_sum    |  0.1 |    10'000 |   108.073 ±   1.227   |
| flatMap_sum    |  0.1 |   100'000 |  1155.964 ±  54.148   |
| flatMap_sum    |  0.1 | 1'000'000 |  1777.393 ± 453.216   |
| flatMap_sum    |  0.5 |    10'000 |   113.230 ±   5.485   |
| flatMap_sum    |  0.5 |   100'000 |  1284.879 ±  63.869   |
| flatMap_sum    |  0.5 | 1'000'000 |  2906.395 ± 259.311   |

You can see that from 10k to 100k elements, it takes roughly 10x the time (as expected), but from 100k to 1M it's well below that, somewhere around 1.5x.
These are all the instances where that happens and you can see that these are exactly the cases where `mapMulti`'s speedup collapses.
Why does `flatMap` get so fast, but `mapMulti` doesn't?
Your guess is as good as mine.
Actually, chances are decent that your guess is better. 😉

My conclusion is that `mapMulti` has the potential to be much faster than `flatMap`, but this may not always materialize.
Fortunately, the way to figure that out for your project is the same way you want to measure any performance work: benchmarks of your actual system with real-life data.


## Reflection

If you're in a situation where `flatMap` doesn't quite work because you can't easily turn the element into a `Stream` or when it's just too slow because of the many `Stream` instance it creates, give `mapMulti` a try.
It accepts a lambda that gets each stream element in turn together with a `Consumer` that you can pass arbitrary many elements into to show up in the next stream operation.

This makes it a bit more imperative, which gives you more leeway in turning a single element into many elements.
It also prevents the creation of `Stream` instances, which _may_ improve performance, but while superficial benchmarks are generally favorable, the speedup varies and may even be below 1.

One thing to note is that you will most likely need to add a type witness when using `mapMulti`, which makes it less convenient than `flatMAp`.
Not only for that reason, the latter should remain your default when mapping a single to multiple elements.

That said, there's at least one very cool thing that you can abuse `mapMulti` for ([hint](https://twitter.com/nipafx/status/1319656592925708289)), but more on that in another post.
