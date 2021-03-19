---
title: "Java 9 Additions To `Stream`"
tags: [java-9, streams]
date: 2016-06-20
slug: java-9-stream
description: "Java 9 is coming! One of the many changes are new Stream methods: `takeWhile`, `dropWhile`, and `ofNullable`. For more fun with streams!"
intro: "Java 9 is coming! And it is more than just Jigsaw. One of the many changes are new Stream methods: `takeWhile`, `dropWhile`, and `ofNullable`. For more fun with streams!"
searchKeywords: "Java 9 stream"
featuredImage: java-9-stream
repo: java-x-demo
---

Java 9 is coming!
And it is more than just [Project Jigsaw](tag:project-jigsaw).
(I was surprised, too.) It is bringing a lot of small and not-so-small changes to the platform and I'd like to look at them one by one.
I'll tag all these posts and you can find them [here](tag:java-9).

Let's start with ...

## Streams

Streams learned three new tricks.
The first deals with prefixes, which streams now understand.
We can use a predicate to test a stream's elements and, starting at the beginning, either take or drop them until the first fails a test.

### `Stream::takeWhile`

Let's look at `takeWhile` first:

```java
Stream<T> takeWhile(Predicate<? super T> predicate);
```

Called on an ordered stream it will return a new one that consists of those element that passed the predicate *until the first one failed*.
It's a little like `filter` but it cuts the stream off as soon as the first element fails the predicate.
In its parlance, it takes elements from the stream while the predicate holds and stops as soon as it no longer does.

Let's see an example:

```java
Stream.of("a", "b", "c", "", "e")
	.takeWhile(s -> !String.isEmpty(s));
	.forEach(System.out::print);

Console: abc
```

Easy, right?
Note how `e` is not part of the returned stream, even though it would pass the predicate.
It is never tested, though, because `takeWhile` is done after the empty string.

#### Prefixes

Just to make sure we're understanding [the documentation](http://download.java.net/java/jdk9/docs/api/java/util/stream/Stream.html#takeWhile-java.util.function.Predicate-), let's get to know the terminology.
A subsequence of an ordered stream that begins with the stream's first element is called a *prefix*.

```java
Stream<String> stream = Stream.of("a", "b", "c", "d", "e");
Stream<String> prefix = Stream.of("a", "b", "c");
Stream<String> subsequenceButNoPrefix = Stream.of("b", "c", "d");
Stream<String> subsetButNoPrefix = Stream.of("a", "c", "b");
```

The `takeWhile`-operation will return the *longest prefix* that contains only elements that pass the predicate.

Prefixes can be empty so if the first element fails the predicate, it will return the empty stream.
Conversely, the prefix can be the entire stream and the operation will return it if all elements pass the predicate.

#### Order

Talking of prefixes only makes sense for ordered streams.
So what happens for unordered ones?
As so often with streams, the behavior is deliberately unspecified to enable performant implementations.

Taking from an unordered stream will return an arbitrary subset of those elements that pass the predicate.
Except if all of them do, then it *always* returns the entire stream.

#### Concurrency

Taking from an ordered parallel stream is not the best idea.
The different threads have to cooperate to ensure that the longest prefix is returned.
This overhead can degrade performance to the point where it makes more sense to make the stream [sequential](http://download.java.net/java/jdk9/docs/api/java/util/stream/BaseStream.html#sequential--).

### `Stream::dropWhile`

Next is `dropWhile`:

```java
Stream<T> dropWhile(Predicate<? super T> predicate);
```

It does just the opposite of `takeWhile`: Called on an ordered stream it will return a new one that consists of the first element that failed the predicate and all following ones.
Or, closer to its name, it drops elements while the predicate holds and returns the rest.

Time for an example:

```java
Stream.of("a", "b", "c", "de", "f")
	.dropWhile(s -> s.length <= 1);
	.forEach(System.out::print);

Console: def
```

Note that the stream contains `f` even though it would not pass the predicate.
Analog to before, the operation stops after the first string fails the predicate, in this case `ef`.

Called on an unordered stream the operation will drop a subset of those elements that fail the predicate.
Unless all of them do, in which case it will always return an empty stream.
Everything else we said above about terminology and concurrency applies here as well.

### `Stream::iterate`

`Stream` already has a method `iterate`.
It's a static factory method that takes a seed element of type `T` and a function from `T` to `T`.
Together they are used to create a `Stream<T>` by starting with the seed element and iteratively applying the function to get the next element:

```java
Stream.iterate(1, i -> 2 * i)
	.forEach(System.out::println);
// output: 1 2 4 8 ...
```

Great!
But how can you make it stop?
Well, you can't, the stream is infinite.
(You can of course cut it short afterwards, using `limit`, but then you can't use a condition and have to know exactly how many elements you want to have.)

Or rather you *couldn't* because this is where the new overload comes in.
It has an extra argument in the middle: a predicate that is used to assess each element before it is put into the stream.
As soon as the first elements fails the test, the stream ends:

```java
Stream.iterate(1, i -> i <= 10, i -> 2 * i)
	.forEach(System.out::println);
// output: 1 2 4 8
```

Used as `iterate(1, i -> i <= 3, i -> i + 1)` it looks more like a traditional for loop than the more succinct but somewhat alien `IntStream.rangeClosed(1, 3)` (which I still prefer but YMMV).
It can also come in handy to turn "iterator-like" data structures into streams, like the ancient `Enumeration`:

```java
Enumeration<Integer> en = // ...
if (en.hasMoreElements()) {
	Stream.iterate(
			en.nextElement(),
			el -> en.hasMoreElements(),
			el -> en.nextElement())
		.forEach(System.out::println);
}
```

You could also use it to manipulate a data structure while you stream over it, like [popping elements off a stack](http://stackoverflow.com/q/38159906/2525313).
This not generally advisable, though, because the source may end up in a surprising state - you might want to discard it afterwards.

<admonition type="note">

Not True!
Turns out neither the `Enumeration` above nor the `Stack` mentioned in the link can be streamed like this - at least not fully.
The predicate (in our cases `el -> en.nextElement()` and `el -> stack.pop()`) is evaluated *after* an element was taken from the source.
This is in line with how the traditional `for`-loop works but has an unfortunate effect.

After taking the last element from the source but before pushing it into the stream, the predicate is consulted and returns false because there is no element *after* the last one.
The element does hence not appear in the stream, which means the last element is always missing.

Thanks to Piotr for pointing this out!

</admonition>

### `Stream::ofNullable`

That one's really trivial.
Instead of talking about it, lets see it in action:

```java
long one = Stream.ofNullable("42").count();
long zero = Stream.ofNullable(null).count();
```

You got it, right?
It creates a stream with the given element unless it is `null`, in which case the stream is empty.
Yawn!

It has its use cases, though.
Before, if some evil API gave you an instance that could be null, it was circuitous to start operating on a stream that instance could provide:

```java
// findCustomer can return null
Customer customer = findCustomer(customerId);

Stream<Order> orders = customer == null
	? Stream.empty()
	: customer.streamOrders();
// do something with stream of orders ...

// alternatively, for the Optional lovers
Optional.ofNullable(customer)
	.map(Customer::streamOrders)
	.orElse(Stream.empty()
	. // do something with stream of orders
```

This gets much better now:

```java
// findCustomer can return null
Customer customer = findCustomer(customerId);

Stream.ofNullable(customer)
	.flatMap(Customer::streamOrders)
	. // do something with stream of orders
```

## Reflection

We've seen how `takeWhile` will return elements that pass the predicate and cut the stream off when the first element fails it.
Conversely, `dropWhile` will also cut the stream when the first element fails the predicat but will return that one and all after it.

As a farewell, let's see a final example, in which we stream all lines from an HTML file's `meta` element:

```java
Files.lines(htmlFile)
	.dropWhile(line -> !line.contains("")
	.skip(1)
	.takeWhile(line -> !line.contains("")
```

`Stream::iterate`'s new overload allows us to create a finite stream, consisting of a seed element and all the elements generated from it until the first fails a test.

We also learned about `ofNullable`.
I wonder why it seems so familiar?
Ah yes, [`Optional`](tag:optional) of course!
Coincidently I will [cover that next](java-9-optional).
:)

Stay tuned!
