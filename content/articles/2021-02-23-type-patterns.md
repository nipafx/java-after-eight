---
title: "Type Pattern Matching with `instanceof`"
tags: [java-16, java-basics, pattern-matching]
date: 2021-02-23
slug: java-type-pattern-matching
description: "Type patters with `instanceof` are Java's first step towards pattern matching. Motivation, syntax, details, and future applications - here's all you need to know."
featuredImage: type-pattern-matching
repo: java-x-demo
---

A word of warning before we begin:
Most introductory pattern matching examples appear somewhat contrived.
They show code that you rarely write (hopefully!) because there are better solutions to the problem at hand, most often classic object-oriented tools like polymorphism.
Let's ignore that for a moment so we can explore the feature itself before I give you [some examples](#use-cases-in-2021) where this is actually the best solution.

<pullquote>Most pattern matching examples are contrived.</pullquote>

Ok?
Good, now let's get started.


## Introduction to Type Patterns

Say you have a few instances of an interface `Animal` and you need to feed the poor sobs.
You know about two implementations, `Elephant` and `Tiger`, but they eat different things, so they don't share the same `eat...` method.
Looks like you have to differentiate by type:

```java
void feed(Animal animal) {
	if (animal instanceof Elephant)
		((Elephant) animal).eatPlants();
	else if (animal instanceof Tiger)
		((Tiger) animal).eatMeat();
}
```

In each two-liner, three things are happening:

1. a type test, e.g. `animal instanceof Elephant`
2. a type conversion, e.g. `(Elephant) animal`
3. a variable declaration, e.g. `Elephant elephant = ...`

<pullquote>A _type pattern_ allows you to do these three steps in one expression</pullquote>

The last step is just conceptual in this case, but it's still necessary to be able to call `eatPlants()` on something of type `Elephant`.
If you do more operations than just a single method call, the casting and required parenthesis get very cumbersome, so you would usually create a dedicated variable:

```java
if (animal instanceof Elephant) {
	Elephant elephant = (Elephant) animal;
	elephant.eatPlants();
	elephant.drinkLotsOfWater();
	elephant.getSkinChecked();
}
```

A _type pattern_ allows you to do the three steps above in one expression:

```java
void feed(Animal animal) {
	if (animal instanceof Elephant elephant)
		elephant.eatPlants();
	else if (animal instanceof Tiger tiger)
		tiger.eatMeat();
}
```

In general, the expression `variable instanceof Type typedVar` (e.g. `animal instanceof Elephant elephant`) checks whether `variable` is an instance of `Type` and _if it is_, declares a new variable `typedVar` of that type.
You can then use the new variable everywhere where the condition is true (details on scoping below).

In the example above that means that you can use `elephant` and `tiger` as `Elephant` and `Tiger`, respectively.
Of course they're still the same instance as `animal` - all that changed is that you, compiler, and runtime agree that they're not just any animals but truly majestic critters.

And that's really all you need to know for basic syntax:

* pick any regular `instanceof` check
* append a variable name after the type
* use that variable as that type

Now, on to the details!


## Type Pattern Details

Type patterns are the first but likely not the last kind of pattern Java will support.
As such, it has some general properties shared by all patterns and a few specific ones.

### General Pattern Matching Properties

I described these general properties in [a dedicated post about pattern matching in Java](java-pattern-matching).
Here's what it covers:

* [goals](java-pattern-matching#pattern-matching-goals)
* [terminology](java-pattern-matching#pattern-matching-terminology)
* [variable scope](java-pattern-matching#pattern-variable-scope)
	* [local variables](java-pattern-matching#local-variables)
	* [flow scoping](java-pattern-matching#flow-scoping)
	* [trickery with scopes](java-pattern-matching#trickery-with-scopes)
* [reassigning pattern variables](java-pattern-matching#reassigning-pattern-variables)
* [combining patterns](java-pattern-matching#combining-patterns)

<pullquote>These properties are absolutely vital to understand how to use type patterns</pullquote>

I highly recommend to give it a thorough read.
And I'm not saying that because I want you to stay on my site (well, a bit 😁) but because these properties are absolutely vital to understand how to use type patterns.
Self check - if the following sentence makes sense, you're good:

> Thanks to flow scoping, pattern variables can be used anywhere where the target is guaranteed to have passed the test.

### Null Check Included

Type patterns use `instanceof` and just like that operator, they don't accept `null` instances.
That means you never need to worry whether the pattern variable is `null` - it's not.

### No Upcasting Allowed

Using a type pattern to cast a type to one of its supertypes makes little sense.
Accordingly, this is considered to be an implementation error and so the compiler throws an error:

```java
public void upcast(String string) {
	// compile error
	if (string instanceof CharSequence sequence)
		System.out.println("Duh");
}
```


## Past, Presence, and Future

With the basics and details covered, let's put the feature in context - why was it created, where can we make the best use of it, and how will it change in the future.

### Motivation

[JEP 394](https://openjdk.java.net/jeps/394) describes `instanceof`-and-cast chains as tedious, obfuscating, and error-prone:

> [A]ll Java programmers are familiar with the `instanceof`-and-cast idiom:
>
> ```java
> if (obj instanceof String) {
>	String s = (String) obj; // grr...
>	...
> }
> ```
>
> [...]
> This pattern is straightforward and understood by all Java programmers, but is suboptimal for several reasons.
> It is tedious; doing both the type test and cast should be unnecessary (what else would you do after an `instanceof` test?).
> This boilerplate - in particular, the three occurrences of the type `String` - obfuscates the more significant logic that follows.
> But most importantly, the repetition provides opportunities for errors to creep unnoticed into programs.

As you can see when we refactor the JEP's example, the resulting code is shorter, more clearly expresses its intent, and doesn't leave room for errors from copy-pasting or edits:

<pullquote>Type patterns are shorter, more clearly express intent, and don't leave room for errors</pullquote>

```java
if (obj instanceof String s) {
	...
}
```

This particular problem could be solved with flow typing, but the JEP alludes to that being an ad-hoc solution and instead proposes to embrace a more powerful language feature that can examine "object shapes" in general: pattern matching.
(Not sure what _flow typing_ is?
Told you to read [the post about pattern matching](java-pattern-matching#isnt-this-flow-typing).
😉)

### Use Cases in 2021

All of that makes sense, but honestly, how often do we write these `instanceof`-and-cast chains?
Not too often, I hope, because in many situations, OOP offers much better solutions.

#### Polymorphism for the Win!

Going back to feeding animals, what about this?

```java
void feed(Animal animal) {
	animal.eat();
}
```

Makes sense, right?

Another common example for type patterns is the computation of a shape's area:

```java
interface Shape { }
record Circle(double radius) implements Shape { }
record Rectangle(double width, double height) implements Shape { }

double area(Shape shape) {
	if (shape instanceof Circle circle)
		return circle.radius() * circle.radius() * Math.PI;
	if (shape instanceof Rectangle rect)
		return rect.width() * rect.height();
	throw new IllegalArgumentException("Unknown shape");
}
```

<!--
```java
interface Shape {
	double area();
}

record Circle(double radius) implements Shape {
	public double area() {
		return circle.radius() * circle.radius() * Math.PI;
	}
}

record Rectangle(double width, double height) implements Shape {
	public double area() {
		return rect.width() * rect.height();
	}
}
```
-->

Once again, the obvious solution is polymorphism by adding `area()` to `Shape` and implement it accordingly in `Rectangle` and `Circle`.
That's not only safer, it also allows for more encapsulation if less data needs to be exposed (that argument makes no sense for records, but you get my drift).

In most situations, this is definitely the preferable solution and I recommend to try it first before resorting to type-checking and casting - be it with or without type patterns.
That doesn't always work, though.

<pullquote>In most situations, classic OOP solutions are preferable to type patterns</pullquote>

#### If OOP Fails

One situation where type-checks are common is when handling primitives, usually somewhere close to the metal, for example when turning objects into an external representation.
Type patterns will be really helpful here.

Another good use case are `equals` implementations:

```java
// old
@Override
public boolean equals(Object o) {
	if (this == o)
		return true;
	if (!(o instanceof Equals))
		return false;
	Type other = (Type) o;
	return someField.equals(other.someField)
		&& anotherField.equals(other.anotherField);
}

// new
@Override
public final boolean equals(Object o) {
	return o instanceof Type other
		&& someField.equals(other.someField)
		&& anotherField.equals(other.anotherField);
}
```

Going back to implementing functionality on the types themselves, there are situations where that doesn't work, for example because you don't have control over their code.
In the earlier example, `Shape`, `Circle`, and `Rectangle` could come from a library, which would make it much harder to use polymorphism to solve the problem of computing their area.

Another situation where you might not extend a type is if you want to avoid stuffing it full of methods from various, mostly independent subdomains.
That's when you pull out the [visitor pattern](https://en.wikipedia.org/wiki/Visitor_pattern), which...
I don't know about you, but I'm not exactly enjoying the moments where I realize I need to use it.
Type patterns are not enough to replace the visitor pattern, but everything else we need for that is already in the making.

### Interaction With Upcoming Features

There are two more things needed to really make pattern matching - particularly with type patterns - shine:

<pullquote>Together, sealed classes, switch expressions, and pattern matching are very powerful</pullquote>

* [sealed classes](https://www.infoq.com/articles/java-sealed-classes/), so the compiler knows all subclasses of a class or implementations of an interface (in preview in Java 15 and 16)
* patterns in [`switch` expressions](java-13-switch-expressions) for exhaustiveness checks (under development)

If you put all three features together, you have a powerful and safe alternative to placing methods on interfaces if, for whatever reason, that's not the road you want to take.
Here's a quick example, but I think I'll go into more detail in a dedicated post:

```java
sealed interface Shape permits Circle, Rectangle { }
record Circle(double radius) implements Shape { }
record Rectangle(double width, double height) implements Shape { }

// strawman syntax ahead!
double area(Shape shape) {
	return switch (shape) {
		// type pattern in switch tests against type and casts
		case Circle circle ->
			circle.radius() * circle.radius() * Math.PI;
		case Rectangle rect ->
			rect.width() * rect.height();
		// compiler knows, `Shape` only has two subtypes
		// and switch expression verifies exhaustiveness
	}
}
```


### Reflection

Type patterns seamlessly integrate with existing `instanceof` checks:
Just append a variable name and start using it wherever the type check succeeded - it will be of the checked type and non-`null`.
Beyond that it has all the [general pattern matching properties](java-pattern-matching#reflection).

When using it in practice, make sure not to do it instead of a polymorphic solution that would work just as well and be more natural and safer.
