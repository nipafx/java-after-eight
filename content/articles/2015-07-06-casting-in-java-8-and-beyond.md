---
title: "Casting In Java 8 (And Beyond?)"
tags: [java-8, optional, streams]
date: 2015-07-06
slug: casting-in-java-8-and-beyond
description: "Proposal to implement new casting methods on Java's `Class`. They aim to fulfill the need for improved ways to cast which was created by Java 8."
intro: "Proposal to implement new casting methods on Java's `Class`. They aim to fulfill the need for improved ways to cast which was created by Java 8's `Optional` and `Stream`."
searchKeywords: "cast"
featuredImage: casting-java-8-and-beyond
---

Casting an instance to a type reeks of bad design.
Still, there are situations where there is no other choice.
The ability to do this has hence been part of Java since day one.

I think Java 8 created a need to slightly improve this ancient technique.

## Static Casting

The most common way to cast in Java is as follows:

```java
Object obj; // may be an integer
if (obj instanceof Integer) {
	Integer objAsInt = (Integer) obj;
	// do something with 'objAsInt'
}
```

This uses the `instanceof` and cast operators, which are baked into the language.
The type to which the instance is cast, in this case `Integer`, must be statically known at compile time, so let's call this static casting.

If `obj` is no `Integer`, the above test would fail.
If we try to cast it anyways, we'd get a `ClassCastException`.
If `obj` is `null`, it fails the `instanceof` test but could be cast because `null` can be a reference of any type.

## Dynamic Casting

A technique I encounter less often uses the methods on `Class` that correspond to the operators:

```java
Object obj; // may be an integer
if (Integer.class.isInstance(obj)) {
	Integer objAsInt = Integer.class.cast(obj);
	// do something with 'objAsInt'
}
```

Note that while in this example the class to cast to is also known at compile time, this is not necessarily so:

```java
Object obj; // may be an integer
Class<T> type = // may be Integer.class
if (type.isInstance(obj)) {
	T objAsType = type.cast(obj);
	// do something with 'objAsType'
}
```

Because the type is unknown at compile type, we'll call this dynamic casting.

The outcomes of tests and casts for instances of the wrong type and null references are exactly as for static casting.

## Casting In Streams And Optionals

### The Present

Casting the value of an `Optional` or the elements of a `Stream` is a two-step-process: First we have to filter out instances of the wrong type, then we can cast to the desired one.

With the methods on `Class`, we do this with method references.
Using the example of `Optional`:

```java
Optional<?> obj; // may contain an Integer
Optional<Integer> objAsInt = obj
		.filter(Integer.class::isInstance)
		.map(Integer.class::cast);
```

That we need two steps to do this is no big deal but I feel like it is somewhat awkward and more verbose than necessary.

### The Future (Maybe)

I propose to implement casting methods on `Class` which return an `Optional` or a `Stream`.
If the passed instance is of the correct type, an `Optional` or a singleton `Stream` containing the cast instance would be returned.
Otherwise both would be empty.

Implementing these methods is trivial:

```java
public Optional<T> castIntoOptional(Object obj) {
	if (isInstance(obj))
		return Optional.of((T) obj);
	else
		Optional.empty();
}

public Stream<T> castIntoStream(Object obj) {
	if (isInstance(obj))
		return Stream.of((T) obj);
	else
		Stream.empty();
}
```

This lets us use `flatMap` to filter and cast in one step:

```java
Stream<?> stream; // may contain integers
Stream<Integer> streamOfInts = stream.
		flatMap(Integer.class::castIntoStream);
```

Instances of the wrong type or null references would fail the instance test and would lead to an empty `Optional` or `Stream`.
There would never be a `ClassCastException`.

### Costs And Benefits

What is left to be determined is whether these methods would pull their own weight:

-   How much code could actually use them?
-   Will they improve readability for the average developer?
-   Is saving one line worth it?
-   What are the costs to implement and maintain them?

I'd answer these questions with *not much*, *a little*, *yes*, *low*.
So it's close to a zero-sum game but I am convinced that there is a small but non-negligible benefit.

What do you think?
Do you see yourself using these methods?
