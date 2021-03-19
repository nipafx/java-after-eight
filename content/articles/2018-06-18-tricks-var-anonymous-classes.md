---
title: "Tricks with `var` and anonymous classes (that you should never use at work)"
tags: [anonymous-classes, java-10, var]
date: 2018-06-18
slug: java-var-anonymous-classes-tricks
description: "Local-variable type inference with `var` makes it easier to work with anonymous classes, e.g. for ad-hoc fields and methods. Don't do it, though!"
intro: "Local-variable type inference with `var` makes it easier to work with anonymous classes, for example to create ad-hoc fields and methods. But does that mean you should use them more often? I think not."
searchKeywords: "anonymous classes"
featuredImage: var-anonymous-classes
repo: java-x-demo
---

[Using `var` to infer types of local variables](java-10-var-type-inference) is a great tool for writing readable code.
More than that, it makes [working with intersection types](java-var-intersection-types) much more pleasant.
It has a dark underbelly, though, and while experimenting with it I found a few `var`-tricks related to [anonymous classes](java-getting-rid-of-anonymous-classes) (ad-hoc fields and ad-hoc methods) that were fun to explore but I think are ultimately not fit for duty.

These tricks hinge on the fact that compiler and JVM know a richer type system than can be expressed with Java's syntax.
With `var`, though, a type does not need to be written out and can instead be determined by the more powerful compiler.
This is particularly helpful when working with anonymous classes, which can not be expressed in source code.

## Ad-hoc Fields

Every time you call a constructor, you have the chance to add some fields and methods right then and there:

```java
Megacorp megacorp = // ...
Map<Megacorp, Address> headquarters = // ...
Object corpWithHq = new Object() {
	Megacorp _corp = megacorp;
	Optional<Address> _hq =
		Optional.ofNullable(headquarters.get(megacorp));
};
// does not compile
System.out.println(corpWithHq._corp);
```

The compiler will create an anonymous subclass (in this case of `Object`) and then instantiate it.
But since the subclass is only created during compilation there is no way to reference it in the source code and so `corpWithHq` is declared as an `Object`.
The unfortunate consequence is that the fields `_corp` and `_hq` can't be referenced because they aren't part of `Object`'s API.

If you use `var`, on the other hand, things work just fine:

```java
Megacorp megacorp = // ...
Map<Megacorp, Address> headquarters = // ...
var corpWithHq = new Object() {
	Megacorp _corp = megacorp;
	Optional<Address> _hq =
		Optional.ofNullable(headquarters.get(megacorp));
};
// compiles
System.out.println(corpWithHq._corp);
```

Now, `corpWithHq`'s type is the anonymous subclass and you can happily toil away with the fields you added.

<pullquote>With var, a variable can be of an anonymous type</pullquote>

### Enriching Streams

That specific example may not have been overly exciting but there are cases where this approach starts to look very appealing.
I'm sure you've occasionally written a stream pipeline where you enriched the stream's elements with some other piece of information, but needed to keep both kinds of elements around.
They form a pair, but Java has no tuples, so you start looking for another solution.
Maybe this?

```java
List<Megacorp> megacorps = // ...
Map<Megacorp, Address> headquarters = // ...
Optional<Megacorp> firstWithValidHq = megacorps.stream()
	// we stream megacorps, but need to add addresses ...
	.map(megacorp -> new Object() {
		Megacorp _corp = megacorp;
		Optional<Address> _hq =
			Optional.ofNullable(headquarters.get(megacorp));
	})
	// ... only for evaluation, though ...
	.filter(o -> o._hq.isPresent())
	.filter(o -> isValid(o._hq.get()))
	// ... in the end we can get rid of them again
	.map(o -> o._corp)
	.findAny();
```

Interestingly enough, this example works without `var` and already compiles on Java 8 because the streams intermediate's type, `Stream<$Anonymous>`, never needs to be expressed in source ode.
With `var`, you're able to declare intermediate variables, though, which wouldn't work without it:

```java
List<Megacorp> megacorps = // ...
Map<Megacorp, Address> headquarters = // ...
// Optional<$Anonymous>
var firstWithValidHq = megacorps.stream()
	.map(megacorp -> new Object() {
		Megacorp _corp = megacorp;
		Optional<Address> _hq =
			Optional.ofNullable(headquarters.get(megacorp));
	})
	.filter(o -> o._hq.isPresent())
	.filter(o -> isValid(o._hq.get()))
	// note that the map is gone!
	.findAny();
```

Without the second `map`, `firstWithValidHq` is an `Optional` containing the anonymous class with the two fields `_corp` and `_hq_`.

### Evaluation

As I mentioned in the intro, I don't think this is a trick you should use frequently, if at all.
First, creating the anonymous class is pretty verbose and will often span several lines.
Then it mixes two non-trivial features, anonymous classes and type inference, which makes the code harder to read and understand.

What bugs me the most, though, is that it falls apart under simple refactoring.
Assume the example we've seen grows a bit and somebody wants to extract two methods, one that determines the megacorp with its headquarter and another that processes it:

```java
List<Megacorp> megacorps = // ...
Map<Megacorp, Address> headquarters = // ...
// `determineCorp` must be declared to return a concrete
// type, so no amount of `var` magic is gonna help here
var corp = determineCorp(megacorps, headquarters);
processCorp(corp);
```

So while refactoring is the right idea, thanks to the ad-hoc fields it's an order of magnitude more work because a type with the right fields needs to be created and used.
That may very well deter developers from actually executing the refactoring and resistance to continuous improvement is not exactly a hallmark of maintainable code.

<pullquote>Using var with anonymous classes makes code harder to read, understand, and refactor</pullquote>

If you're looking for alternatives, I sometimes use `Map.Entry` for pairs, which [Java 9](tag:java-9) made much more usable with [the static method `Map::entry`](https://docs.oracle.com/javase/10/docs/api/java/util/Map.html#entry(K,V)).
Beyond that you could be looking for a library that comes with tuples, something you usually find in functional libraries like [Vavr](http://www.vavr.io/).
If you're patient, you can wait for Project Amber's [data classes](http://cr.openjdk.java.net/~briangoetz/amber/datum.html), which will make local classes a one-liner.

## Ad-hoc Methods

Just like fields, you can add methods:

```java
Megacorp corp = new Megacorp(/* ... */) {
	final BigDecimal SUCCESS_BOUNDARY = new BigDecimal("500000000");

	boolean isSuccessful() {
		return earnings().compareTo(SUCCESS_BOUNDARY) > 0;
	}

	boolean isEvil() {
		return true;
	}
};
```

And just like with fields, if you declare `corp` as `Megacorp`, the compiler will not let you use the new methods `isSuccessful` and `isEvil`.
With `var` it does:

```java
var corp = // like before
System.out.printf(
		"Corporation %s is %s and %s.\n",
		corp.name(),
		corp.isSuccessful() ? "successful" : "a failure",
		corp.isEvil() ? "evil" : "a failure"
);
```

It's the same principle as with ad-hoc fields and I have the same criticism.

### Evaluation

Like with ad-hoc fields, the code's readability and refactorability (what a word) suffer without appreciable benefits.

Alternatively, methods like `isSuccessful` and `isEvil` could either be members of `Megacorp` or a subclass or, if that's not possible or desirable for whatever reason, they can always be implemented as utility methods.
While that makes calling them a little less natural (`Megacorps.isEvil(corp)` instead of `corp.isEvil()`), it has the added benefit to enable reuse.

## Reflection

Using [local-variable type inference with `var`](java-10-var-type-inference), it is easy to add fields or methods to objects in an ad-hoc manner, simply by creating an instance of an anonymous class and assigning it to a local variable whose type is inferred.
While that is a neat trick, it does not carry its own weight:

-   code becomes less readable (with anonymous classes and type inference it relies on non-trivial Java features)
-   code becomes harder to refactor (those types can not readily be used in method signatures)

So instead of relying on a little magic with a lot of downsides, I recommend to stick to proven alternatives:

-   instead of ad-hoc fields, use `Map::entry`, your favorite FP library's tuple types, or wait for data classes
-   instead of ad-hoc methods, extend the class directly or use utility functions

But take my advice with a grain of salt, [I don't like anonymous classes anyways](java-getting-rid-of-anonymous-classes).
