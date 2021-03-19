---
title: "Beware Of findFirst() And findAny()"
tags: [java-8, streams]
date: 2016-01-14
slug: java-stream-findfirst-findany-reduce
description: "`Stream.findFirst()` and `findAny()` work with any number of elements in the stream. Make sure to `reduce(toOnlyElement())` if there should be at most one."
intro: "When using `Stream.findFirst()` or `findAny()`, you will often assume that there is at most one element left in the stream. But neither tests that assumption so maybe you should use a different approach."
searchKeywords: "stream findFirst findAny"
featuredImage: stream-findfirst-findany-reduce
---

After filtering a [Java 8 `Stream`](https://docs.oracle.com/javase/8/docs/api/java/util/stream/Stream.html) it is common to use [`findFirst()`](https://docs.oracle.com/javase/8/docs/api/java/util/stream/Stream.html#findFirst--) or [`findAny()`](https://docs.oracle.com/javase/8/docs/api/java/util/stream/Stream.html#findAny--) to get the element that survived the filter.
But that might not do what you really meant and subtle bugs can ensue.

## So What's Wrong With `findFirst()` And `findAny()`?

As we can see from their Javadoc ([here](https://docs.oracle.com/javase/8/docs/api/java/util/stream/Stream.html#findFirst--) and [here](https://docs.oracle.com/javase/8/docs/api/java/util/stream/Stream.html#findAny--)) both methods return an arbitrary element from the stream - unless the stream has an [encounter order](https://docs.oracle.com/javase/8/docs/api/java/util/stream/package-summary.html#Ordering), in which case `findFirst()` returns the first element.
Easy.

A simple example looks like this:

```java
public Optional<Customer> findCustomer(String customerId) {
	return customers.stream()
			.filter(customer -> customer.getId().equals(customerId))
			.findFirst();
}
```

Of course this is just the fancy version of the good old for-each-loop:

```java
public Optional<Customer> findCustomer(String customerId) {
	for (Customer customer : customers)
		if (customer.getId().equals(customerId))
			return Optional.of(customer);
	return Optional.empty();
}
```

But both variants contain the same potential bug: they are built on the implicit assumption that there can only be one customer with any given ID.

Now, this might be a very reasonable assumption.
Maybe this is a known invariant, guarded by dedicated parts of the system, relied upon by others.
In that case this is totally fine.

But in many cases I see out in the wild, it is not.
Maybe the customers were just loaded from an external source that makes no guarantees about the uniqueness of their IDs.
Maybe an existing bug allowed two books with the same ISBN.
Maybe the search term allows surprisingly many unforeseen matches (did anyone say regular expressions?).

<pullquote>Often the code relies on a unique matching element but does nothing to assert this.</pullquote>

Often the code's correctness relies on the assumption that there is a unique element matching the criteria but it does nothing to enforce or assert this.

Worse, the misbehavior is entirely data-driven, which might hide it during testing.
Unless we have this scenario in mind, we might simply overlook it until it manifests in production.

Even worse, it fails silently!
If the assumption that there is only one such element proves to be wrong, we won't notice this directly.
Instead the system will misbehave subtly for a while before the effects are observed and the cause can be identified.

So of course there is nothing inherently wrong with `findFirst()` and `findAny()`.
But it is easy to use them in a way that leads to bugs within the modeled domain logic.

## Failing Fast

So let's fix this!
Say we're pretty sure that there's at most one matching element and we would like the code to [fail fast](https://en.wikipedia.org/wiki/Fail-fast) if there isn't.
With a loop we have to manage some ugly state and it would look as follows:

```java
public Optional<Customer> findOnlyCustomer(String customerId) {
	boolean foundCustomer = false;
	Customer resultCustomer = null;
	for (Customer customer : customers)
		if (customer.getId().equals(customerId))
			if (!foundCustomer) {
				foundCustomer = true;
				resultCustomer = customer;
			} else {
				throw new DuplicateCustomerException();
			}

	return foundCustomer
			? Optional.of(resultCustomer)
			: Optional.empty();
}
```

Now, streams give us a much nicer way.
We can use the often neglected `reduce`, about which [the documentation says](https://docs.oracle.com/javase/8/docs/api/java/util/stream/Stream.html#reduce-java.util.function.BinaryOperator-):

> Performs a [reduction](https://docs.oracle.com/javase/8/docs/api/java/util/stream/package-summary.html#Reduction) on the elements of this stream, using an [associative](https://docs.oracle.com/javase/8/docs/api/java/util/stream/package-summary.html#Associativity) accumulation function, and returns an Optional describing the reduced value, if any.
> This is equivalent to:
>
> ```java
> boolean foundAny = false;
> T result = null;
> for (T element : this stream) {
>     if (!foundAny) {
>         foundAny = true;
>         result = element;
>     }
>     else
>         result = accumulator.apply(result, element);
> }
> return foundAny ?
> Optional.of(result) : Optional.empty();
> ```
>
> but is not constrained to execute sequentially.

Doesn't that look similar to our loop above?!
Crazy coincidence...

So all we need is an accumulator that throws the desired exception as soon as it is called:

```java
public Optional<Customer> findOnlyCustomerWithId(String customerId) {
	return customers.stream()
			.filter(customer -> customer.getId().equals(customerId))
			.reduce((element, otherElement) -> {
				throw new DuplicateCustomerException();
			});
}
```

This looks a little strange but it does what we want.
To make it more readable, we should put it into a Stream utility class and give it a nice name:

```java
public static <T> BinaryOperator<T> toOnlyElement() {
	return toOnlyElementThrowing(IllegalArgumentException::new);
}

public static <T, E extends RuntimeException> BinaryOperator<T>
toOnlyElementThrowing(Supplier<E> exception) {
	return (element, otherElement) -> {
		throw exception.get();
	};
}
```

Now we can call it as follows:

```java
// if a generic exception is fine
public Optional<Customer> findOnlyCustomer(String customerId) {
	return customers.stream()
			.filter(customer -> customer.getId().equals(customerId))
			.reduce(toOnlyElement());
}

// if we want a specific exception
public Optional<Customer> findOnlyCustomer(String customerId) {
	return customers.stream()
			.filter(customer -> customer.getId().equals(customerId))
			.reduce(toOnlyElementThrowing(DuplicateCustomerException::new));
}
```

How is that for intention revealing code?

It should be noted that, unlike `findFirst()` and `findAny()`, this is of course no [short-circuiting operation](https://docs.oracle.com/javase/8/docs/api/java/util/stream/package-summary.html#StreamOps).
That means that for the common case that zero or one elements pass the filter, this operation will materialize the entire stream (whereas the `find...()` methods stop after the first such element).
Processing only finishes early when a second element is encountered.

<pullquote>This will materialize the entire stream.</pullquote>

## Context

There is actually a [question on StackOverflow](http://stackoverflow.com/q/22694884/2525313) about this precise use case and it has a lot of interesting answers.
The [alternative I like the most](http://stackoverflow.com/a/22695424/2525313), is a collector that throws when it encounters a second argument.
With it the code would look as follows:

```java
public Optional<Customer> findOnlyCustomer(String customerId) {
	return customers.stream()
			.filter(customer -> customer.getId().equals(customerId))
			.collect(onlyElement());
}
```

It is also worth noting that Guava has a similar functionality for iterators, namely [`Iterables::getOnlyElement`, which returns the only element from the specified `Iterable`](https://google.github.io/guava/releases/snapshot/api/docs/com/google/common/collect/Iterables.html#getOnlyElement(java.lang.Iterable)).
It behaves different for an empty iterable, though, where it throws a `NoSuchElementException`.
([.NET's `Enumerable::Single`](https://msdn.microsoft.com/en-us/library/bb155325(v=vs.110).aspx) does the same, by the way.)

## Reflection

We have seen how `findFirst()` and `findAny()` do not suffice to express the assumption that there is at most one element left in the stream.
If we want to express that assumption and make sure the code fails fast if it is violated, we need to `reduce(toOnlyElement())`.

You can find [the code on GitHub](https://gist.github.com/nicolaiparlog/74ac912658f0e11e9057?ts=4) and use it as you like - it is in the public domain.

Thanks to [Boris Terzic](http://www.aggregat4.net/) for making me aware of this intention mismatch in the first place.
