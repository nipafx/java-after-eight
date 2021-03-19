---
title: "Pattern Matching in Java"
tags: [java-basics, pattern-matching]
date: 2021-02-16
slug: java-pattern-matching
description: "Java takes its first steps towards pattern matching but the topic is much larger than `instanceof`. Goals, terminology, flow scoping - these apply to all kinds of patterns."
featuredImage: pattern-matching
---

Type patterns (the `instanceof` thingy) are Java's first step on a longer path towards pattern matching and it rightfully gets a lot of attention right now.
Unfortunately, most introductory material doesn't do a very good job at sorting out the feature's properties:

* some properties are specific to type patterns
* others are inherent to pattern matching (in Java) and will be equally present in other patterns

<pullquote>This article discusses pattern matching in general</pullquote>

This article focuses on the second category and discusses pattern matching in general.
With this under your belt, it will be much easier to understand other patterns as they are released.
Of course they'll all get their own detailed posts, which will rely on the general properties established here.

<admonition type="warning">

Of course this article needs examples, but so far we only have one kind of pattern, which makes it tough to show general properties.
So I decided to do something that's either very clever or very stupid (it depends on you what it'll end up as) and opted to not limit this post to existing patterns.
I'll use the following:

* _type patterns_ - they exist, so we know how they work
* _destructuring patterns_ - [these are being worked on](jdk-news-1/#destructuring-patterns-in-for-each-loops), so we can make some educated guesses how they might turn out
* _regex patterns_ - I've made these up on the spot

That means many of the following snippets don't work now, most won't work in the foreseeable future, and some might never work.
These aren't promises of what's to come, just made-up examples to demonstrate commonalities.

</admonition>

With all of this out of the way, let's talk about pattern matching!

<pullquote>This post doesn't limit itself to existing patterns</pullquote>


## Pattern Matching Goals

Generally speaking, you'll use pattern matching to check whether a variable has certain properties, and if it does, extract parts of it into other variables to continue working with them.
As I just described it, that's nothing special and we're doing that all day every day:

```java
// checking a type
Animal animal = /* ... */;
if (animal instanceof Elephant) {
	Elephant elephant = (Elephant) animal;
	elephant.eatPlants();
}

// extracting members
Rectangle rectangle = /* ... */;
double width = rectangle.width();
double height = rectangle.height();
double area = width * height;

// matching regex
String url = /* ... */;
Pattern fxPattern = Pattern.compile("(.+)fx.*");
Matcher matcher = fxPattern.matcher(url);
if (matcher.find()) {
	String fx = matcher.group(1);
	System.out.println(fx);
}
```

The tests and extractions above mostly rely on library calls (except for `instanceof`).
Pattern matching takes a different approach and lets the compiler support commonly used or error-prone test-and-extract patterns.

```java
// TYPE PATTERN
Animal animal = /* ... */;
if (animal instanceof Elephant elephant)
	elephant.eatPlants();

// DESTRUCTURING PATTERN
Rectangle rectagle = /* ... */;
Rectangle(double width, double height) = rectangle;
double area = width * height;

// REGEX PATTERN
String url = /* ... */;
if (url.matches("(.+)fx.*") $1 fx)
	System.out.println(fx);
```

Being able to clearly express what we want to do makes the code more succinct, but that's more of an (intended) side effect.
First and foremost, a more expressive language allows for safer and more readable code.

<pullquote>Making the code more succinct is just an (intended) side effect</pullquote>

So, as many language features introduced over the last two decades, pattern matching aims to capture common coding patterns and support them directly in the language.


## Pattern Matching Terminology

However different they may look, all patterns work in very similar ways and consist of the same building blocks:

* _target_: a variable or expression that we try to match
* _test_: a run-time check of some property the target may or may not have
* _variable(s)_: capture (parts of) the target if it passes the test
* _pattern_: test and variable(s) taken together

Let's look at some examples:

```java
// TYPE PATTERNS
//         |--------- pattern --------|
//  target |----- test ------| variable
    animal instanceof Elephant elephant

// DESTRUCTURING PATTERNS
//  |--------------- pattern ---------------|
//  |- test -|--------- variables ----------|   | target |
    Rectangle(double xLength, double yLength) = rectangle;

// REGEX PATTERNS
// tar- |------- pattern --------|
//  get |----- test ------| var. |
    url.matches("(.+)fx.*") $1 fx)
```

As you can see on the example of destructuring patterns, the test can be a formality that always passes as long as the code compiles.

Using this terminology, we can restate the goal of pattern matching:
When applying a pattern to a target, you test the target and, if it passes, extract (parts of) it into variable(s).


## Pattern Variable Scope

A variable's _scope_ is, in simple terms, the part of the code where you can reference it.
Before we get into the scope of pattern variables, let's look at two mechanisms used for local variables.

### Local Variables

A local variable's scope starts with its declaration and ends with the end of the statement or block in which it has been declared.
This is quite intuitive.

Furthermore, a mechanism called _definitive assignment_ makes sure we never read a variable before it's been assigned to:

```java
Animal animal = /* ... */;
// note: no assignment!
Elephant elephant;
if (animal instanceof Elephant) {
	// assignment, so variable can be
	// used in the block's remainder
	elephant = (Elephant) animal;
	elephant.eatPlants();
} else {
	// compile error because the
	// variable is unassigned
	findFoodFor(elephant);
}
```

An interesting detail of definitive assignment is that it requires the compiler to understand conditions and loops.
Otherwise it wouldn't be able to analyze the control's flow to determine at what lines in the code a variable was already assigned.

### Flow Scoping

Variables of patterns where the test is a formality that always passes are scoped just like local variables.
For a proper test that can actually fail, the compiler is much smarter, though.

Pattern matching combines regular scoping and definitive assignment into a new-to-Java scoping mechanism called _flow scoping_.
For patterns where variables are only assigned if a certain test passes, definitive assignment is used to determine where exactly that's the case.
And that portion of the regular block-scope is the exact part of the code where the variable is in scope.

```java
void feed(Animal animal) {
	// `elephant` is in scope only in the next two lines
	if (animal instanceof Elephant elephant)
		elephant.eatPlants();
	// `tiger` is in scope only in the next two lines
	else if (animal instanceof Tiger tiger)
		tiger.eatMeat();
	// `elephant` and `tiger` are out of scope here;
	// the compiler knows that because it analyzed
	// where the two could be assigned
	System.out.println(animal);
}
```

That may sound complicated, but it boils down to a simple rule:
A pattern variable is in scope within those parts of its statement/block where the target passed the test.

<pullquote>A simple rule</pullquote>

#### Isn't This Flow Typing?

No, but it's similar.
[_Flow typing_](https://en.wikipedia.org/wiki/Flow-sensitive_typing) is a compiler feature that detects type checks and considers a variable to be of that type in the `true`-branch.
If Java had it, it would probably work like this:

```java
if (animal instanceof Elephant)
	// compiler would know `animal` is of type `Elephant`
	// because it passed the `instanceof` check
	animal.eatPlants();
```

The two features differ in two aspects:

* flow scoping requires the declaration of a new variable
* flow scoping is not limited to type checks - any kind of pattern can make use of it

That means for a bit of extra code we get a much more powerful feature.

### Trickery with Scopes

Flow scoping has a few pretty cool consequences.
One is that you can go on and use the variable within the same condition:

```java
boolean isLongString(Object object) {
	// only if the first part of the condition is true
	// (i.e. `object` is indeed a `String`) ...
	return (object instanceof String string
		// ... will the second part be evaluated,
		// which means `string` is in scope
		// and can thus be used
		&& string.length() > 50)
}
```

It also works well with [early returns](java-multiple-return-statements):

```java
boolean isLongString(Object object) {
	if (!(object instanceof String string))
		return false;

	// this code is only reachable if `object`
	// is a `String`, so `string` is in scope
	return string.length() > 50;
}
```

Finally, if the variables' scopes don't overlap, for example in `if`-chains, you can reuse the same variable name within the same block:

```java
void feed(Animal animal) {
	// there are two declarations of `eater`,
	// but each is only in scope within "its own" branch
	// and so there is no overlap and hence no conflict
	if (animal instanceof Elephant eater)
		eater.eatPlants();
	else if (animal instanceof Tiger eater)
		eater.eatMeat();
}
```

I don't think reusing the same variable name is a good idea, though.
Just because it's possible doesn't mean we have to do it. 😉


## Reassigning Pattern Variables

In Java 14 and 15, type pattern variables are implicitly final because reassigning them doesn't appear helpful.
To remove asymmetries between local and pattern variables, this was changed, though, and so on Java 16+ (when type patterns came out of [preview](enable-preview-language-features)), they can indeed be reassigned.
Once again:
Just because it's possible doesn't mean we have to do it. 😜

I assume this will also be the case for variables of other types of patterns.


## Combining Patterns

In principle, it is possible to combine patterns.
Here we use type and destructuring patters to check what kind of shape we have in hand and then extract the values we need:

```java
double area(Shape shape) {
	if (shape instanceof Rectangle(double width, double height))
		return width * height;
	if (shape instanceof Circle(double radius))
		return Math.PI * radius * radius;
	throw new IllegalArgumentException();
}
```

I'm positive, Java will allow that as well.


## Reflection

These are the general properties of pattern matching (in Java):

* they consist of four building blocks: target, test, variable(s), and pattern (= test + variable(s))
* applying a pattern to a target means testing it and, if it passes, extracting (parts of) it into variable(s)
* pattern variables are flow-scoped, which means their scope is that portion of their block where the test passed
* patterns can be combined

Now that we've got all of the general properties covered, I can go into type patterns with `instanceof`... in [the next post](java-type-pattern-matching).

And if you want to take a closer look at the evolution of pattern matching in Java, check out [JDK News #1](jdk-news-1):

<contentvideo slug="jdk-news-1"></contentvideo>
