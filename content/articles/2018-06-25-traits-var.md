---
title: "Unlocking Traits With `var`"
tags: [default-methods, java-10, lambda, var]
date: 2018-06-25
slug: java-var-traits
description: "In Java 10, `var` makes it is possible to ad-hoc combine traits into an instance that matches your exact requirements. Alas, it has some downsides."
intro: "With `var` it is possible to ad-hoc combine traits into an instance that matches your exact requirements. This allows for pretty cool experimentation, but unfortunately has some serious downsides."
searchKeywords: "traits"
featuredImage: var-traits
repo: java-x-demo
---

[Local-variable type inference, or more succinctly `var`](java-10-var-type-inference), turns out to be surprisingly versatile.
It unlocks [intersection types](java-var-intersection-types), allows using [ad-hoc fields and methods of anonymous types](java-var-anonymous-classes-tricks), and, as we will see now, enables working with traits.
In the end, [I don't think it's worth the hassle](#The-Dark-Side), but your mileage may vary - maybe it helps you out of a bind one day.

## What's A Trait?

Some programming languages have a concept of *traits*, which allows creating a new type in the middle of a variable declaration that combines features of several types into one.
Conceptionally, this could look as follows:

<pullquote>A trait ad-hoc combines features of several types into one</pullquote>

```java
type Megacorp {
	String name();
	BigDecimal earnings();
}

trait IsSuccessful {
	boolean isSuccessful() {
		return earnings().compareTo(SUCCESS_BOUNDARY) > 0;
	}
}

trait IsEvil {
	boolean isEvil() { return true; }
}

Megacorp & IsSuccessful & IsEvil corp =
	new (Megacorp & IsSuccessful & IsEvil)(/*...*/);
System.out.printf(
	"Corporation %s is %s and %s.\n",
	// relying on `corp` as `Megacorp`
	corp.name(),
	// relying on `corp` as `IsSuccessful`
	corp.isSuccessful() ? "successful" : "a failure",
	// relying on `corp` as `IsEvil`
	corp.isEvil() ? "evil" : "a failure"
);
```

Looks nice, doesn't it?
If only the syntax wouldn't be totally made up... But, as we will see in a minute, we can take a few steps into that direction with some trickery involving [lambdas](tag:lambda), [default methods](tag:default-methods), [and `var`](tag:var).
Before we come to that, though, I want to address a question you might have at this point.

## Wait, I Thought These Were Intersection Types!?

If this reminds you of [intersection types](java-var-intersection-types), you're on to something.
The ampersand in the declaration has the same semantics: The resulting intersection inherits from the joined types and it follows that variables of that intersection type have the methods of all joined types - in this case of `Megacorp`, `IsSuccessful`, and `IsEvil`.

The difference between intersection types and traits is that the former represent a *requirement*: A variable, parameter, or return value has to be of the intersection of the joined types.
Traits, on the other hand, are about *composing* types: The variable created by the construction will be of the intersection of the joined types.

Naturally, intersection types and traits go well together:

```java
public void createWithTraits() {
	// create `corp` with two traits
	Megacorp & IsSuccessful & IsEvil corp =
		new (Megacorp & IsSuccessful & IsEvil)(/*...*/);
	// call method that requires intersection
	// of `Megacorp` and `IsEvil`
	requireIntersection(corp)
}

public void requireIntersection(Megacorp & IsEvil corp) {
	// relying on `corp` as `Megacorp` and `IsEvil`
	System.out.printf(
		"Corporation %s is %s.\n",
		corp.name(),
		corp.isEvil() ? "evil" : "a failure"
	);
}
```

Now, if only any of that were valid Java syntax... Let's see how close we can get.

## Creating The Skeleton For Traits

The basic recipe to creating traits has three ingredients:

<pullquote>What the hell is going on here?</pullquote>

-   prepare an interface that can be created with a [lambda expression](tag:lambda)
-   cast that lambda to the intersection of the required types
-   assign the entire kerfuffle to a `var`-ed variable, so the type information don't get trimmed to a single type

In the end, it will look like this:

```java
// compiler infers desired intersection type for `corp`
var corp = (MegacorpDelegate & IsSuccessful & IsEvil) () -> megacorp;
```

I guess this leads us to the question: What the hell is going on here?

Let's start with the initial type, `Megacorp` in this example: It needs to be an interface.
To intersect it with other types, an extending interface like `MegacorpDelegate` needs to be created that [default implements](java-default-methods-guide) all methods by forwarding to a delegate:

```java
interface Megacorp {
	String name();
	BigDecimal earnings();
}

@FunctionalInterface
interface MegacorpDelegate extends Megacorp {
	Megacorp delegate();
	default String name() { return delegate().name(); }
	default BigDecimal earnings() { return delegate().earnings(); }
}
```

It is critical that the delegating interface's only abstract method is one that returns the delegate.
This enables you to create an instance of it from a given `Megacorp` with a lambda expression like `() -> megacorp`.
Lambdas are crucial here because they are so-called *poly expressions*: They don't have a defined type - instead it is inferred from the rest of the statement, in this case from the cast you place before them.

<pullquote>The delegating interface must have a single abstract method, which returns the delegate</pullquote>

The delegating interface doesn't add any domain functionality, it is only a technical requirement to make traits work in Java.
As such, the delegating interface is conceptually close to the original one and I recommend placing it in the same package.

With that, some parts of the earlier declaration start to work:

```java
Megacorp megacorp = // ...
// compiler infers `MegacorpDelegate` for `corp`
var corp = (MegacorpDelegate) () -> megacorp;
```

Creating such a delegating interface gives you the skeleton that you can now flesh out with the actual traits by writing interfaces that extend the original interface and add new methods.

## Creating Traits

After creating a delegating interface like `MegacorpDelegate` everything is set up for coding the actual traits.
They add specific features to the original interface, here `Megacorp`, that are apparently not general enough to become part it.
That's why traits are more likely to end up close to where they are needed instead of close to the original interface.

The second critical requirement is that the trait interfaces must not have abstract methods!
If they do, their intersection with the delegating interface could not be created with a lambda.

<pullquote>Trait interfaces must not have abstract methods</pullquote>

Here are some example for our megacorporations:

```java
interface IsSuccessful extends Megacorp {
	BigDecimal SUCCESS_BOUNDARY = new BigDecimal("500000000");

	default boolean isSuccessful() {
		return earnings().compareTo(SUCCESS_BOUNDARY) > 0;
	}

}

interface IsEvil extends Megacorp {

	default boolean isEvil() {
		return true;
	}

}
```

Because `IsSuccessful` and `IsEvil` have no abstract methods, their intersection with `MegacorpDelegate` can still be created with a lambda:

```java
// compiles and runs
Megacorp megacorp = // ...
var corp = (MegacorpDelegate & IsSuccessful & IsEvil) () -> megacorp;
System.out.printf(
	"Corporation %s is %s and %s.\n",
	// relying on `Megacorp`
	corp.name(),
	// relying on `IsSuccessful`
	corp.isSuccessful() ? "successful" : "a failure",
	// relying on `IsEvil`
	corp.isEvil() ? "evil" : "a failure"
);
```

Et voilà, given a `Megacorp` instance you can decide on the spot which other functionality you want to compose with it.
You can quickly and easily code that functionality up for your very specific context and start using it very naturally with plain method calls on the instance.

But the best thing is, neither the creators of `Megacorp`, `MegacorpDelegate`, or even `IsSuccessful` and `IsEvil` need to know what exactly you're doing and neither do you need to create new types for any specific combination of traits.
The latter is particularly important if you have many possible traits because the combinatorial explosion of creating a class for every combination would quickly overwhelm you.

## The Dark Side

Ok, we've finally settled how to simulate traits in Java by using lambda expressions, default methods, intersection types, and local-variable type inference.
This is a nice thought experiment and might even serve you as a stopgap one day, but it has severe disadvantages, which is why I don't recommend to use it except in critical emergencies.

The first thing you might have noticed throughout this post is that simulating traits requires the combination of a decent number of non-trivial Java features, which always makes code less readable and thus less maintainable.
Speaking of maintainability, variables of an intersection type only flourish *within* a method - as soon as they need to be passed *to or from* a method they [require some generic trickery to be represented](java-var-intersection-types#declaring-intersection-types-with-generics).
This makes refactoring such code considerably more complicated.

There's also some setup involved in creating the delegating interface and, even though that is unlikely to play a relevant role, the memory indirection it incurs reduces performance.
Beyond that, traits need to be interfaces without abstract methods, which limits the functionality you can implement.

The final death knell is something else, though: Default methods [can not implement `Object` methods](java-default-methods-guide#overriding-methods-on-object), which has the consequence that the combined instances can never forward a call to `equals` or `hashCode` to the underlying delegate.
This is bound to break code that puts such instances in collections and will result in strange and hard-to-debug misbehavior.

So as much fun as playing with traits is, I strongly recommend never to simulate them this way.
Alternatives are to simply gather the required methods in utility classes or to create subclasses after all.

## Reflection

These are the ingredients to ad-hoc compose traits with a given interface:

-   a delegating interface extending the given interface:
	-   uses default methods to forward all calls to delegate
	-   has single abstract method `delegate()`, so it can be created with `() -> delegate`
-   trait interfaces that have no abstract methods
-   declare variables as follows:

```java
var variable = (Interface & Trait1 & Trait2) () -> delegate;
```

This looks like a great way to avoid deep inheritance trees and instead compose the exact behavior you need exactly where you need.
Alas, it has several downsides:

-   cumbersome creation of the delegating interface
-   uses several non-trivial language features
-   use of intersection types makes refactoring harder
-   can not [correctly implement `equals`/`hashCode`](implement-java-equals-correctly)

