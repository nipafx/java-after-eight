---
title: "Immutable Collections In Java - Not Now, Not Ever"
tags: [collections]
date: 2019-05-29
slug: immutable-collections-in-java
description: "The JDK contains immutable collections, but no type `ImmutableCollection`. Here's why that's so and why it won't change."
intro: "The JDK contains immutable collections, but no type `ImmutableCollection`. I explain why and claim that that's never gonna change."
searchKeywords: "immutable collections"
featuredImage: immutable-collections
---

Mutability is bad, mkay?
Hence, [immutability is good](https://medium.com/@johnmcclean/dysfunctional-programming-in-java-2-immutability-a2cff487c224).
Central data structures whose ubiquity make immutability particularly rewarding are collections; `List`, `Set`, and `Map` in Java.
But while the JDK comes with immutable (or unmodifiable?) collections, the type system knows nothing about that.
There's no `ImmutableLst` in the JDK and, as a type, I consider Guava's to be borderline useless.
Why, though?
Why not just add `Immutable...` to the mix and call it a day?

## What's An Immutable Collection?

In JDK terminology, *immutable* and *unmodifiable* have shifted over the last few years.
Originally, *unmodifiable* marked an instance that offered no mutability (by throwing `UnsupportedOperationException` on mutating methods) but may be changed in other ways (maybe because it was just a wrapper around a mutable collection).
This understanding is reflected in the methods `Collections::unmodifiableList`, `unmodifiableSet`, and `unmodifiableMap` and [their JavaDoc](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/Collection.html#unmodview).

At first, [the term *immutable* was used](https://docs.oracle.com/javase/9/docs/api/java/util/List.html#immutable) for the collections returned by [Java 9's collection factory methods](java-9-tutorial#collection-factories).
The collections themselves could not be changed in any way (well, [reflection](https://www.sitepoint.com/java-reflection-api-tutorial/), but that doesn't count) and so they seem to warrant the attribute immutable.
Alas, that may easily cause confusion.
Will a method that prints all elements in an immutable collection always have the same output?
Yes?
No?

If you didn't answer *No* immediately, you have first-person insight into the possible confusion.
An *immutable collection of secret agents* might sound an awful lot like an *immutable collection of immutable secret agents*, but the two are not the same.
The immutable collection may not be editable by adding/removing/clearing/etc, but, if secrets agents are mutable (although the lack of character development in spy movies seems to suggest otherwise), that doesn't mean the collection of agents as a whole is immutable.
Hence the shift to call these collections *unmodifiable* instead of *immutable* as indicated by [the new JavaDoc](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/List.html#unmodifiable).

Personally, I disagree with that shift.
To me, *immutable collection* only means that the collection itself can't be mutated, but says nothing about the elements it contains.
That has the added advantage that it doesn't define the term *immutability* in a way that makes it borderline useless in the Java ecosystem.

<pullquote>In this post, immutable collections can contain mutable elements</pullquote>

Anyway, in this post, we're talking about *immutable collections* where...

-   the instances it contains are defined during construction
-   those instances can never be removed or added to
-   no assertion is made regarding the mutability of these elements

That settles that, now let's add immutable collections.
Or rather, an immutable list - everything that follows applies just the same to all the other collection types.

## Just Add Immutable Collections, Already!

We create an interface `ImmutableList` and make it `List`'s, err..., supertype or subtype?
Let's go with the former.

<contentimage slug="immutable-collections-mutable-extends-immutable" options="bg narrow"></contentimage>

Neat, `ImmutableList` has no mutating methods and so it's always safe to use, right?
Right?!
Nope.

```java
List<Agent> agents = new ArrayList<>();
// compiles because `List` extends `ImmutableList`
ImmutableList<Agent> section4 = agents;
// prints nothing
section4.forEach(System.out::println);

// now lets mutate `section4`
agents.add(new Agent("Motoko");
// prints "Motoko" - wait, how the fuck did she get in here?!
section4.forEach(System.out::println);
```

This example shows that you could pass such a not-really-immutable list to an API that may rely on immutability, thus voiding all guarantees that type name may allude to.
This is a recipe for disaster.

Ok, then `ImmutableList` extends `List`.
Maybe?

<contentimage slug="immutable-collections-immutable-extends-mutable" options="bg narrow"></contentimage>

Now, if an API expects an immutable list, it will actually get one, but there are two downsides:

-   immutable lists still have to offer mutating methods (because these are defined on the supertype) and the only possible implementation is throwing an exception
-   `ImmutableList` instances are also instances of `List` and if assigned to such a variable, passed as such an argument, or returned as such a type it is reasonable to assume that mutation is allowed

Put together this means that `ImmutableList` could only ever be used locally because as soon as it passes API boundaries as a `List`, which requires superhuman levels of care to prevent, it explodes at run time.
That's not as bad as when `List` extends `ImmutableList`, but it's still far from an ideal solution.

In fact, this is what I meant when I said that Guava's `ImmutableList` is borderline useless as a type.
It's a great piece of code and very reliable for local immutable lists (which is why I tend to use it a lot), but it's too easy to opt out of to be the iron-clad, compiler-guaranteed stronghold that immutable types have to be to unfold their full potential.
It's better than nothing but insufficient as a solution for the JDK itself.

If `ImmutableList` can't extend `List` and the other way around doesn't work either, then how is this supposed to work at all?

<pullquote>How is this supposed to work at all?!</pullquote>

## Immutability Is A Feature

The problem with our first two tries of adding immutable types was the misconception that immutability is just the absence of something: Take a `List`, remove the mutating code and you've got an `ImmutableList`.
But that's not how this works.

If we simply remove mutating methods from `List`, we end up with a list that is read-only.
Or, in the terminology established earlier, we can call it an `UnmodifiableList` - it can still change under you, it's just that you won't be the one changing it.

Now there are two things we can add:

-   we can make it mutable by adding the according methods
-   we can make it immutable by adding the according guarantees

The important insight here is that *both of these are features* - immutability is not an absence of mutation, it's a guarantee that there won't be mutation.
A feature isn't necessarily something you can use to do good, it may also be the promise that something bad won't happen - think of thread-safety, for example.

<pullquote>Immutability is not an absence of mutation, it's a guarantee there won't be mutation</pullquote>

Obviously, mutability and immutability conflict with one another, which is why we couldn't make the two inheritance hierarchies above work.
Types inherit features from other types so whichever way you slice it, if one of these two types inherits from the other, it contains both features.
💣

Ok, so `List` and `ImmutableList` can't extend one another.
But we arrived here by way of `UnmodifiableList`, and indeed both types share their read-only API with it, so they should extend it.

<contentimage slug="immutable-collections-both-extend-unmodifiable" options="bg narrow"></contentimage>

Almost.

<pullquote>Scala does it like that.</pullquote>

While I wouldn't use those exact names, the hierarchy itself is sound.
Scala, for example, [does it almost like that](https://docs.scala-lang.org/overviews/collections/overview.html).
The difference is that its shared supertype, what we've called `UnmodifiableList`, defines mutating methods that return a modified collection, but keep the original untouched.
This makes the immutable list [*persistent*](https://en.wikipedia.org/wiki/Persistent_data_structure) and gives the mutable variant two sets of modifying methods - the inherited one for getting modified copies and their own for mutating in place.

What about Java, though?
Can a hierarchy like this with new supertypes and siblings be retrofitted?

## Can Unmodifiable And Immutable Collections Be Retrofitted?

Of course it's no problem to add the types `UnmodifiableList` and `ImmutableList` and create the inheritance hierarchy described above.
The problem is that this would be close to pointless in the short and midterm.
Let me explain.

The cool thing about having `UnmodifiableList`, `ImmutableList`, and `List` as types is that APIs can clearly express what they need and what they offer.

```java
public void payAgents(UnmodifiableList<Agent> agents) {
	// mutating methods are not required for payments,
	// but immutability isn't necessary either
}

public void sendOnMission(ImmutableList<Agent> agents) {
	// a mission is dangerous (lots of threads, har har),
	// and it is important that the team is stable
}

public void downtime(List<Agent> agents) {
	// during downtime, team members may leave, and new
	// members may be hired, so the list needs to be mutable
}

public UnmodifiableList<Agent> teamRoster() {
	// you can look at the team, but you can't edit it and
	// you also can't be sure that nobody else edits it
}

public ImmutableList<Agent> teamOnMission() {
	// if the team's on a mission, it won't change
}

public List<Agent> team() {
	// getting a mutable list implies that you can edit
	// the list and see the changes in this object
}
```

But unless you're starting from scratch, that functionality already exists and it most likely looks like this:

```java
// there's a good chance that an `Iterable<Agent>`
// suffices, but lets assume we really need a list
public void payAgents(List<Agent> agents) { }

public void sendOnMission(List<Agent> agents) { }

public void downtime(List<Agent> agents) { }

// personally, I tend to return streams because they
// are unmodifiable, but `List` is still more common
public List<Agent> teamRoster() { }

// likewise, this may already be `Stream<Agent>`
public List<Agent> teamOnMission() { }

public List<Agent> team() { }
```

That's not good because to benefit from the new collections we just introduced, we actually need to use them (duh!).
The above looks like application code, so a refactoring towards `UnmodifiableList` and `ImmutableList` as shown in the earlier snippet may be feasible.
Could be a lot of work and may cause confusion when old and updated code needs to interact, but at least it's tractable.

What about frameworks, libraries, and the JDK itself, though?
Here, the picture is bleak.
Changing a parameter or return type from `List` to `ImmutableList` is *source incompatible*, i.e.
existing source code will not compile against the new version, because these types are unrelated.
Likewise, changing a return type from `List` to its new supertype `UnmodifiableList` results in compile errors.

But even widening a parameter type from `List` to `UnmodifiableList` is a problem because this change is *bytecode incompatible*.
When your source code calls a method, the compiler will turn that call into bytecode that references the target method by:

<pullquote>Introducing the new types would require changes and recompilations throughout the entire ecosystem</pullquote>

-   the name of the class the target instance is declared as
-   the method name
-   the method parameter types
-   the method return type

Any change to a method's parameter or return type means existing bytecode references it by the wrong signature, leading to a `NoSuchMethodError` at run time.
If the change is source compatible, like narrowing a return type or widening a parameter type, a recompile would suffice.
But for a far-reaching change like introducing new collections, it's not that simple - we'd effectively need to recompile the entire Java ecosystem for this to go through.
This is a loosing proposition.

The only compatible way to make use of the new collections is to duplicate existing methods with a new name, change the API, and deprecate the old variant.
Can you imagine what a monumental and effectively eternal task that would be?!

## Reflection

While immutable collection types are a great thing to have, we're unlikely to ever see them in the JDK.
Proper implementations of `List` and `ImmutableList` can never extend one another (instead both extend a read-only list type like `UnmodifiableList`), which complicates their introduction into existing APIs.

Beyond any specific type relationships, changing existing method signatures is always a problem because the change is bytecode incompatible.
It requires a recompile at minimum, which for a intrusive change like this one would require us to recompile the entire Java ecosystem.

That's not gonna happen - not now, not ever.
