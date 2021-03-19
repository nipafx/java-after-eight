---
title: "Unlocking Intersection Types With `var`"
tags: [generics, java-10, var]
date: 2018-06-11
slug: java-var-intersection-types
description: "Java 10's `var` makes intersection types in Java more approachable. Generics tricks are still needed, but `var` makes it easy to declare such variables."
intro: "With `var` it is much easier to work with intersection types in Java 10 and later. You still need non-trivial tricks with generics to declare intersection types, but thanks to `var` it is now easy to create local variables of such types."
searchKeywords: "intersection types"
featuredImage: var-intersection-type-venn
repo: java-x-demo
---

Whether you're already using Java 10 or not, I'm sure you've heard [all about `var`](java-10-var-type-inference), Java's new ~~keyword~~ reserved type name that allows you to declare local variables without having to specify their types.
While `var` looks simple at first glance, it opens the door to a richer type system and can be turned into a surprisingly powerful tool with which you can do some really cool things.
Today I want to look at how to use it to work with intersection types:

```java
// `elements` should implement both
// `Closeable` and `Iterator<String>`
// (DOES NOT COMPILE!)
Closeable & Iterator<String> elements = // ...
// `try` only works because `elements` is `Closeable`
try (elements) {
	// the method `stream` expects an `Iterator<E>`,
	// (and returns a `Stream<E>`), so it can only be
	// called if `elements` implements it
	Optional<String> first = stream(elements)
		.filter(s -> s.startsWith("$"))
		.findAny();
}
```

The declaration of `elements` looks pretty weird: It wants the variable to be of two types, `Closeable` and `Iterator<String>`.
That's called an intersection type, but Java's syntax doesn't allow them and so the code doesn't even compile - our goal is to find a variant that does.
After some background on the why, what, and how of intersection types in Java, I will show you how to use them with `var`.

Spoiler: The declaration will end up as `var elements = // ...`.


## What Are Intersection Types?

We've all been in this situation: You're writing a method and at some point realize that some parameter's type isn't powerful enough - you need more!
Maybe a `List` instead of a `Collection` or a `Megacorp` instead of a mere regular `Corporation`.
Often that's pretty straight forward: There's a type which has what you need, so you can simply use it.

Other times it's not so easy, though.
As an example, say you need an `Iterator` to also be `Closeable`, so you can do this:

```java
static <E> Optional<E> firstMatch(
		/* Closeable & Iterator<E> */ elements,
		Predicate<? super E> condition) {
	try (elements) {
		// `stream` turns `Iterator<E>` into `Stream<E>`
		return stream(elements)
			.filter(condition)
			.findAny();
	}
}
```

You want to iterate over `elements`, so it needs to be an `Iterator`, but you also want the safety of a `try`-with-resources block, so it needs to be `Closeable` as well.
If you think of the type `Iterator` as a circle that contains all variables of that type and likewise for `Closeable`, `elements` needs to be in the intersection of these circles - it needs to be of an *intersection type* of `Iterator` and `Closeable`.

## Just Create A New Interface

The obvious way to limit `elements` to the desired types is to create a new interface:

```java
public interface CloseableIterator<E> extends Closeable, Iterator<E> { }

static <E> Optional<E> firstMatch(
		CloseableIterator<E> elements,
		Predicate<? super E> condition)
		throws IOException {
	// ...
}
```

That's great if you have control over all implementations of `Closeable` and `Iterator` and can change them to also implement `CloseableIterator`.
But what if you don't?
Neither the JDK nor third-party libraries and frameworks know anything about your new interface and although they might return instances that implement both `Closeable` and `Iterator`, you still couldn't pass them to `firstMatch`:

```java
// `Scanner` implements `Iterator<String>` and `Closeable`
Scanner scanner = new Scanner(System.in);
Optional<String> dollarWord =
	// compile error because `scanner` is no `CloseableIterator` :(
	firstMatch(iterator, s -> s.startsWith("$"));
```

No, what you really need is a way to express `Closeable & Iterator<E>` without creating a new interface.

## Declaring Intersection Types With Generics

While you can't declare a variable as `Closeable & Iterator<E>`, that expression *can* be syntactically valid in Java.
Just not as a type in a variable declaration but as a *bounded type parameter*, for example in a generic method:

<pullquote>Generics allow intersection types in bounded type parameters</pullquote>

```java
private static <E, T extends Closeable & Iterator<E>>
		Optional<E> firstMatch(T elements, Predicate<? super E> condition)
		throws IOException {
	// ...
}
```

That's not straight forward, but it works: First you declare a new type parameter `T` which has to extend `Closeable` and `Iterator` - here the ampersand is syntactically correct and limits `T` to the intersection of these two types - and then you declare `elements` as being of that type.

The next step is to get an actual instance of `Closeable & Iterator`.
That's pretty easy, every class that implements both interfaces, for example `Scanner`, works fine:

```java
Scanner scanner = new Scanner(System.in);
Optional<String> dollarWord = firstMatch(
	scanner,
	s -> s.startsWith("$"));
```

But what if the instance is not so easy to create?
What if you need to involve another method and its return type can not be specific, like `Scanner` but needs to be the general intersection?
We can once again use generics for that:

```java
@SuppressWarnings("unchecked")
private static <T extends Closeable & Iterator<String>>
		T createCloseableIterator(boolean empty) {
	if (empty)
		return (T) new Empty();
	else
		return (T) new Scanner(System.in);
}
```

You can combine `createCloseableIterator` and `firstMatch` as follows:

```java
Optional<String> dollarWord = firstMatch(
	createCloseableIterator(empty),
	s -> s.startsWith("$"));
```

That's actually pretty neat and all of that works without `var`, so where does local-variable type inference enter the picture?
That's next!

## Declaring Variables Of Intersection Types

The construction we have so far has the weak spot that it breaks down under refactoring.
Want to extract a variable for `createCloseableIterator(empty)`?
That's too bad because, without `var`, you can't:

```java
// does not compile
Closeable & Iterator<String> elements = createCloseableIterator(empty);
// compiles, but can not be passed to `firstMatch`
Closeable elements = createCloseableIterator(empty);
Iterator<String> elements = createCloseableIterator(empty);
// compiles and can be passed, but can fail at run time
// (depending on `empty`)
Scanner elements = (Scanner) createCloseableIterator(empty);
Empty elements = (Empty) createCloseableIterator(empty);
```

What you really need is the first line, but Java's syntax won't let you compile it.
That doesn't mean the JVM can't handle it, though.
In fact, this would work:

```java
static <T extends Closeable & Iterator<String>>
		void readAndPrint(boolean empty)
		throws IOException {
	T elements = createCloseableIterator(empty);
	Optional<String> dollarWord =
		firstMatch(elements, s -> s.startsWith("$"));
	System.out.println(dollarWord);
}
```

This goes too far, though.
We're now exposing a type `T` to callers of this method that they see neither as parameter nor return type and would clearly be confused by.
And imagine what the method would look like if we needed another such variable!

No, `createCloseableIterator` and `firstMatch` have a right to a complicated signature because they actually need it, but `readAndPrint` is ridiculous!
Is there no better way to declare `elements`?

After all this build-up, and given your knowledge of [how `var` works](java-10-var-type-inference), the solution is rather straight forward:

```java
static void readAndPrint(boolean empty) throws IOException {
	var elements = createCloseableIterator(empty);
	Optional<String> dollarWord =
		firstMatch(elements, s -> s.startsWith("$"));
	System.out.println(dollarWord);
}
```

There you go, with `var` you can declare `elements` in a way that lets the compiler deduce that its type is the intersection of `Closeable` and `Iterator`, just like you wanted.
That means you can now freely declare such variables and use them across methods without having to expose your callers to crazy generic signatures.

<pullquote>Use var to capture a generic method's returned intersection type</pullquote>

## Reflection

While the concept of intersection types is quite simple and a common theme in other (JVM) languages like Scala, Java's lack of first-class support makes it complicated to work with.
You have to resort to non-trivial constructions using generics and that always reduces the code's readability and accessibility.
Still, there are situations where they are the best available solution and their added complexity is acceptable.

With the introduction of `var` the situation improved considerably.
Even if it's still a long shot from proper support, being able to easily declare variables of an intersection type makes them much more usable.
If you haven't already, it is high time to add intersection types to your tool belt.
