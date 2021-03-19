---
title: "Oh No, I Forgot `Stream::iterate`!"
tags: [java-9, streams]
date: 2016-07-25
slug: java-9-stream-iterate
description: "In Java 9 `Stream` gets a couple of new methods - one of them is an overload of `iterate` that takes a predicate and returns a finite stream."
intro: "There I go babbling about new stream methods and then I forget one: a `Stream::iterate` overload that produces a finite stream."
searchKeywords: "stream iterate"
featuredImage: java-9-stream
---

There I go talking about the new things that [Java 9 will bring to the stream API](java-9-stream) and then I forget one: a new overload for `iterate`.
[D'oh!](https://www.youtube.com/watch?v=cnaeIAEp2pU) I updated that post but to make sure you don't miss it, I also put it into this one.

## `Stream::iterate`

`Stream` already has a method `iterate`.
It's a static factory method that takes a seed element of type `T` and a function from `T` to `T`.
Together they are used to create a `Stream<T>` by starting with the seed element and iteratively applying the function to get the next element:

```java
Stream.iterate(1, i -> i + 1)
	.forEach(System.out::println);
// output: 1 2 3 4 5 ...
```

Great!
But how can you make it stop?
Well, you can't, the stream is infinite.

Or rather you *couldn't* because this is where the new overload comes in.
It has an extra argument in the middle: a predicate that is used to assess each element before it is put into the stream.
As soon as the first elements fails the test, the stream ends:

```java
Stream.iterate(1, i -> i <= 3, i -> i + 1)
	.forEach(System.out::println);
// output: 1 2 3
```

As it is used above, it looks more like a traditional for loop than the more succinct but somewhat alien `IntStream.rangeClosed(1, 3)` (which I still prefer but YMMV).
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
