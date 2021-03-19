---
title: "Broken `Stream::group` with Java 16's `mapMulti`"
tags: [java-16, streams]
date: 2020-11-16
slug: java-16-stream-mapmulti-group
description: "Java 16 adds a new method `mapMulti` to `Stream` and it can be abused to simulate a reverse-`flatMap` aka `group` operation (with shortcomings)."
searchKeywords: "java stream group"
featuredImage: java-16-stream-mapMulti-group
repo: java-x-demo
---

Because [I've already introduced `Stream::mapMulti` in detail](java-16-stream-mapmulti), I'll stick to a blitz intro.
Here's its signature:

```java
// plus wildcards
<R> Stream<R> mapMulti​(BiConsumer<T, Consumer<R>> mapper)
```

You call it on a `Stream<T>` to map a single element of type `T` to multiple elements of type `R`.
This is done by accepting the `T` instance, transforming it to arbitrary many `R` instances, and passing them to the given `Consumer<R>`.
At least that's the intent - but we can do a bit more with it...

## Reverse `flatMap` - aka `group`

You know how `flatMap` (also) maps a single element of type `T` to multiple elements of type `R`?
Have you ever had the need to do the reverse - group a sequence of `T`s into a single `R`?

Say you run a museum and given a `Stream<Visitor>` need to turn that into a `Stream<VisitorGroup>` where a group holds up to three visitors.
There's no good solution for that use case within the `Stream` API, but `mapMulti` can double as one - under certain circumstances.
Let's try it out.

<pullquote>`mapMulti` can double as a reverse `flatMap`</pullquote>

First we need types for visitors and groups:

```java
record Visitor(String name) { }
record VisitorGroup(List<Visitor> visitors) { }
```

(All hail records for their expressiveness, but in real life I'd add constructors with null checks and defensive copies.)

Next, we need a builder for the groups.
At its core that'll be a method that accepts a visitor and the downstream consumer and emits new groups as it sees fit.
As we will see, there are two ways to implement this method and both misbehave outside of specific circumstances.


## Grouping Late

The straightforward approach is to add the visitor to a list and, once the list reaches the correct size, create a group and pass it downstream.

```java
public void group(Visitor visitor, Consumer<VisitorGroup> downstream) {
	visitors.add(visitor);
	if (visitors.size() == visitorsPerGroup) {
		downstream.accept(new VisitorGroup(visitors));
		visitors = new ArrayList<>();
	}
}
```

The rest of the class - I called it `VisitorGroupCoordinator` - is just the two fields `int visitorsPerGroup` and `List<Visitor> visitors` and a constructor.
Here's how you can use it:

```java
List<Visitor> visitors = List.of(
	new Visitor("Abraham Takahashi"),
	new Visitor("Kazumi Michelakis"),
	new Visitor("Aneko Kim"),
	new Visitor("Motoko Windrider"),
	new Visitor("Mahir Watanabe"));
visitors.stream()
	// creates groups of three
	.mapMulti(new VisitorGroupCoordinator(3)::group)
	.forEach(System.out::println);
```

But this won't work as intended.
Have you spotted the problem?
Take a second and walk through these steps for all five visitors, particularly for Mahir.

What's special about Mahir?
He's the last visitor, but the group he's in has just two people and so it never gets passed downstream.
Indeed, the program only outputs the first group with Abraham, Kazumi, and Aneko:

```shell
# manual line breaks for better readability
> VisitorGroup[visitors=[
> 	Visitor[name=Abraham Takahashi],
> 	Visitor[name=Kazumi Michelakis],
> 	Visitor[name=Aneko Kim]]
> ]
```

### Failure

Generally speaking, a "late emitting" grouping method may fail to create the last group unless:

<pullquote>A "late emitting" grouping method may fail to create the last group</pullquote>

1. a group can be created based on a property of the last element in the group _and_
2. the last element in the stream is guaranteed to have that property

Our visitor coordinator fails to create the last group because while 1. is upheld (a visitor's one-based index in the list of visitors is dividable by the group size), 2. is violated (the last visitor in the list may not fulfill that).

As an example for where 1. may be violated, consider a stream of log messages that you want to group by their timestamps' hours.
Given a specific message, it's impossible to know whether it is the last message during that hour.
Hence a message group can only be created when a message with a timestamp in another hour shows up.
In other words, a group can only be completed on the first member of the next group, which violates 1.

Another failure mode for all of this is parallel stream processing.
Unless the resulting groups are not based on the elements' order and the group builders are thread-safe, this is going to fail horribly.

### Success

It's not all doom and gloom, though.
There's at least one use case where properties 1. and 2. are often fulfilled and that's parsing.

Let's consider parsing JSON by streaming a file line by line ([e.g. with `Files::lines`](java-11-gems#streaming-lines-with-stringlines)).
Here's an example of an array of three `Person` instances:

```js
[
	{
		name: "Jane Doe",
		birthday: "2002-11-30"
	},
	{
		name: "John Doe",
		birthday: "2001-05-12"
	},
	{
		name: "Jekyll Doe",
		city: "Paris"
	}
]
```

Without writing the parser itself (see [the demo](https://github.com/nipafx/demo-java-x/blob/master/src/main/java/org/codefx/demo/java16/api/stream/MapMultiParse.java) for code that parses the above), we can immediately see that correct JSON fulfills properties 1. and 2:

1. a line containing `}` ends a person block, so when encountering it, the grouper/parser can create a `Person` and pass it downstream
2. every (valid) person block is guaranteed to end that way

And since parsing is usually done sequentially, the problem with parallel streams doesn't apply here.


## Grouping Early

After thoroughly covering one of the two ways to group museum visitors, let's turn to the other: grouping them early.
This works by relying on the `VisitorGroup`'s mutability, which is... not great.

<pullquote>Trigger warning: mutability</pullquote>

The trick is to create and emit a group as soon as the first visitor shows up and fill it up with new visitors as they trickle in:

```java
public void group(Visitor visitor, Consumer<VisitorGroup> downstream) {
	visitors.add(visitor);
	if (visitors.size() == 1)
		downstream.accept(new VisitorGroup(visitors));
	else if (visitors.size() == visitorsPerGroup)
		visitors = new ArrayList<>();
```

This only works because `VisitorGroup` is sloppy and doesn't create a defensive copy of the passed list `visitors`.
If it would, the approach could be salvaged by storing the current group and adding to its `visitors()` component.
A conscientious developer may decide to create an _immutable_ defensive copy with `List::copyOf`, though, in which case this approach is dead in the water.

But even if it works in theory, it has its practical shortcomings.
As before, see whether you can spot the problem when using the code above to group these stream's elements:

```java
List<Visitor> visitors = List.of(
	new Visitor("Abraham Takahashi"),
	new Visitor("Kazumi Michelakis"),
	new Visitor("Aneko Kim"),
	new Visitor("Motoko Windrider"),
	new Visitor("Mahir Watanabe"));
visitors.stream()
	.mapMulti(new VisitorGroupCoordinator(3)::group)
	.forEach(System.out::println);
```

Got it?
Here's the output:

```shell
> VisitorGroup[visitors=[Visitor[name=Abraham Takahashi]]]
> VisitorGroup[visitors=[Visitor[name=Motoko Windrider]]]
```

Each group appears to have only one visitor. 😮

That's because the stream pipeline doesn't execute _all operations for all elements_, but processes _(required) elements for all operations_ (note the order).
Consequently, as soon as the coordinator emits a group (with one visitor), it is passed to the downstream operation.
To observe the full groups, the operation following `mapMulti` needs to gather all elements - `collect` is the obvious choice:

```java
visitors.stream()
	.mapMulti(new VisitorGroupCoordinator(3)::groupEarly)
	.collect(toList())
	.forEach(System.out::println);
```

At this point, why not create a `Collector`, though?

<pullquote>At this point, why not create a `Collector`, though?</pullquote>

The only intermediate operation I am aware of that collects all elements before emitting them is `sorted`, which isn't particularly helpful either.

### Failure

An "early emitting" grouping method not only requires mutable groups, it also runs afoul of the `Stream` API's fundamental approach to processing pipelines: element by element (not operation by operation).
This leads to routinely observing incomplete groups, which can only be countered by forcing a full processing of the operation with `collect` or `sorted`.
That's not ideal, to say the least.

The characteristics regarding parallel streams are the same as for a "late emitting" grouping method.


## Reflection

As much as I hoped to be able to use `mapMulti` as a "reverse `flatMap`" operation, the hard truth is that it's not suitable outside very specific circumstances:

* a group can be created based on a property of the last element in the group
* the last element in the stream is guaranteed to have that property
* the stream is processed sequentially or the groups are order-independent

This makes it a good fit for parsing, at least.
Other than that, I'm back to [beseeching Brian](https://twitter.com/nipafx/status/1321368935988604929).
