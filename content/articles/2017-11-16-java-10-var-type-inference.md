---
title: "First Contact With 'var' In Java 10"
tags: [java-10, java-basics, var]
date: 2017-11-16
slug: java-10-var-type-inference
description: "Java 10 introduces the `var` keyword, which lets the compiler infer local variable types. Here's how var works, why it exists, how it impacts readability."
searchKeywords: "var"
featuredImage: var-example
repo: java-x-demo
---

[Java 10 will be released on March 20th 2018](https://medium.com/codefx-weekly/java-10-and-cross-references-in-asciidoctor-597abcfef8d3) and all features that want to be shipped with it must be merged into the main development line by December 14th.
Of the few features that target Java 10 as of yet, local-variable type inference ([JEP 286](http://openjdk.java.net/jeps/286)) is surely the most interesting one.
It brings the `var` keyword to Java and gives you the option to cut variable declarations short:

```java
var users = new ArrayList<User>();
```

And that's it, thanks for reading!

Nah, I'm sure you're interested to learn more.
In this post I'll discuss where `var` applies and where it doesn't, how it impacts readability, and what happened to `val`.
If you want to learn about more advanced applications of `var`, check out my posts on [intersection types](java-var-intersection-types), [traits](java-var-traits), and [anonymous classes](java-var-anonymous-classes-tricks).

## Replacing Type Declarations With `var`

As a Java developer we're used to typing types twice, once for the variable declaration and once again for the following constructor:

```java
URL codefx = new URL("https://nipafx.dev");
```

We also often declare types for variables that are used just once and on the next line:

```java
URL codefx = new URL("https://nipafx.dev");
URLConnection connection = codefx.openConnection();
Reader reader = new BufferedReader(
	new InputStreamReader(connection.getInputStream()));
```

This is not particularly terrible, but it *is* somewhat redundant.
And while IDEs can help a lot with writing such code, readability suffers when variable names jump around a lot because their types have very different character counts or when developers avoid declaring intermediate variables because type declarations would eat up a lot of attention without adding much value.

<pullquote>From Java 10 on developers can choose to let the compiler infer types by using var</pullquote>

From Java 10 on, developers have an alternative and can choose to let the compiler infer the type by using `var`:

```java
var codefx = new URL("https://nipafx.dev");
var connection = codefx.openConnection();
var reader = new BufferedReader(
	new InputStreamReader(connection.getInputStream()));
```

When processing `var`, the compiler looks at the right hand side of the declaration, the so-called initializer, and uses its type for the variable.
And not just for internal bookkeeping, it writes that type into the resulting bytecode.

<pullquote>The compiler uses the initializer's type</pullquote>

As you can see, this saves a few characters when typing, but more importantly it deduplicates redundant information and neatly aligns the variable's names, which eases reading them.
The cost is obvious: Some variables' types, of `connection` for example, are not immediately obvious.
IDEs can of course show them on demand, but that doesn't help in any other environment (think code reviews).

By the way, in case you're worried about clashes with methods and variables named `var`: don't be.
Technically, `var` is not a keyword, but a *reserved type name*, meaning it can only be used in places where the compiler expects a type name, but everywhere else it's a valid identifier.
That means that only classes called `var` will no longer work, but that shouldn't happen particularly often.

<pullquote>var is a reserved type name</pullquote>

Local-variable type inference looks like a straight-forward feature, but that's deceptive.
You might already have some questions:

-   bleh, is this Java or JavaScript?
-   where can I use this?
-   won't `var` hurt readability?
-   why is there no `val` or `let`?

Let's go through them one by one.

## No, This Is Not JavaScript

I want to start by stressing that `var` does not change Java's commitment to static typing by one iota.
The compiler infers all involved types and puts them into the class files as if you typed them yourself.

<pullquote>This does not change Java's commitment to static typing</pullquote>

Case in point, here's the result of IntelliJ's (actually Fernflower's) decompilation of the class file with the URL example:

```java
URL codefx = new URL("https://nipafx.dev");
URLConnection connection = codefx.openConnection();
BufferedReader reader = new BufferedReader(
	new InputStreamReader(connection.getInputStream()));
```

This is byte by byte the same result as if you had declared the types yourself.
In fact, this feature only exists in the compiler and has no runtime component whatsoever, which also means there is no performance impact.
So relax, this is not Javascript and nobody's going to [turn 0 into god](https://twitter.com/hsjoihs/status/792206474902515712).

If you're still worried that lacking explicit types makes all code worse, I have a question for you: Have you ever written a lambda without defining its argument types?

```java
rhetoricalQuestion.answer(yes -> "see my point?");
```

## Where To Use `var` (And Where Not To)

JEP 286's title, "local-variable type inference", kinda gives away where `var` can be used: for local variables.
More precisely, for "local variable declarations with initializers", so even the following won't work:

```java
// nope
var foo;
foo = "Foo";
```

It really has to be `var foo = "Foo"`.
Even then it doesn't cover all cases as `var` won't work with so called "poly expressions", like lambdas and method references, whose type the compiler determines in relation to an expected type:

```java
// none of this works
var ints = {0, 1, 2};
var appendSpace = a -> a + " ";
var compareString = String::compareTo
```

The only other eligible spots besides local variables are `for` loops and `try`-with-resources blocks:

```java
// var in for loops
var numbers = List.of("a", "b", "c");
for (var nr : numbers)
	System.out.print(nr + " ");
for (var i = 0; i < numbers.size(); i++)
	System.out.print(numbers.get(i) + " ");

// var in try-with-resources
try (var file = new FileInputStream(new File("no-such-file"))) {
	new BufferedReader(new InputStreamReader(file))
			.lines()
			.forEach(System.out::println);
} catch (IOException ex) {
	// at least, we tried
	System.out.println("There's actually no `no-such-file`. :)");
}
```

That means fields, method signatures, and `catch` clauses still require manual type declaration.

```java
// nope
private var getFoo() {
	return "foo";
}
```

### Avoiding "Action At A Distance" Errors

That `var` can only be used locally is not a technical limitation, but a design decision.
Sure, it would be nice to have it work like this:

```java
// cross fingers that compiler infers List<User>
var users = new ArrayList<User>();
// but it doesn't, so this is a compile error:
users = new LinkedList<>();
```

The compiler could easily look at all assignments and infer the most concrete type that fits all of them, but it doesn't.
The JDK team wanted to avoid "action at a distance" errors, meaning changing code in some place should not lead to a seemingly unrelated error far away.

As an example look at the following:

```java
// inferred as `int`
var id = 123;
if (id < 100) {
	// very long branch; unfortunately
	// not its own method call
} else {
	// oh boy, much more code...
}
```

So far, so... I don't want to say "good", but you know what I mean.
I'm sure you've seen such code.
Now we append this line:

```java
id = "124"
```

What would happen?
This is not a rhetorical question, think about it.

The answer is that the `if`-condition throws an error because `id` will no longer be an `int` and can thus not be compared with `<`.
That error is at quite a distance from the change that caused it and on top of that it's a surely unforeseen consequence of simply assigning a value to a variable.

From that perspective the decision to limit type inference to the immediate declaration makes sense.

### Why Can't Field And Method Types Be Inferred?

Fields and methods have a far larger scope than local variables and as such the distance between changes and errors increases considerably.
In the worst case, changing a method parameter's type can lead to binary incompatibilities and thus runtime errors.
That's a rather extreme consequence of having changed some implementation detail.

So because non-private fields and methods become part of a type's contract and because that shouldn't be changed accidentally, these types are not inferred.
Sure, an exception could have been made for private fields or methods, but that would make the feature rather weirdly scoped.

The basic idea is that local variables are implementation details and can not be referenced from "far away" code, which reduces the need to strictly, explicitly, and verbosely define their type.

## Background On var

Let's have a look behind the scenes and find out why `var` was introduced, how its impact on readability was envisioned and why there is no `val` (or `let`) accompanying it.
If you're interested in even more detail have a look at [the JEP 286 discussions](http://mail.openjdk.java.net/pipermail/platform-jep-discuss/2016-December/000066.html), [the `var` FAQ](http://cr.openjdk.java.net/~briangoetz/jep-286/lvti-faq.html), or [the Project Amber mailing list](http://mail.openjdk.java.net/pipermail/amber-dev/).

### But why?!

Java's overall tendency to be pretty verbose, particularly compared to younger languages, is one of the biggest pain points for developers and a common critique of the language by novice and experienced Java devs alike.
[Project Amber](http://openjdk.java.net/projects/amber/), under which `var` was developed, aims "to explore and incubate smaller, productivity-oriented Java language features" and has the goal to generally reduce the ceremony involved in writing and reading Java code.

Local-variable type inference is aligned with that goal.
On the writing side, it obviously makes declaring variables much easier, although I'd guess that a good half of my declarations are generated by the IDE, either during a refactoring or because it's just plain faster to write a constructor or method call and then create a variable for it.

Beyond making declarations easier it also makes them more amenable.
What do I mean by that?
Declarations can be quite ugly, particularly if enterprise-grade class names and generics are involved.

```java
InternationalCustomerOrderProcessor<AnonymousCustomer, SimpleOrder<Book>> orderProcessor = createInternationalOrderProcessor(customer, order);
```

There's a long-ass type name, which pushes the variable name most of the way to the end of the line and leaves you with either bumping line length to 150 chars or initializing the variable on a new line.
Both options suck if you're aiming for readability.

```java
var orderProcessor = createInternationalOrderProcessor(customer, order);
```

With `var` it is much less burdensome and easier on the eye to declare intermediate variables and we might end up doing that in places where we didn't before.
Think about nested or chained expressions where you decided against breaking them apart because the reduction in complexity got eaten by the increase in ceremony.
Judicious use of `var` can make intermediate results more obvious and more easily accessible.

In short, `var` is about reducing verbosity and ceremony, not about saving characters.

### And What About Readability?

Now let's turn to readability.
Surely it must get worse when types are missing, right?
Generally speaking, yes.
When trying to figure out how a piece of code works, types are an essential ingredient and even if IDEs would develop features that allow displaying all inferred types, it would still be more indirect than having them always present in the source.

So `var` starts at a readability disadvantage and has to make up for it.
One way it does that is by aligning variable names:

```java
// with explicit types
No no = new No();
AmountIncrease<BigDecimal> more = new BigDecimalAmountIncrease();
HorizontalConnection<LinePosition, LinePosition> jumping =
	new HorizontalLinePositionConnection();
Variable variable = new Constant(5);
List<String> names = List.of("Max", "Maria");

// with inferred types
var no = new No();
var more = new BigDecimalAmountIncrease();
var jumping = new HorizontalLinePositionConnection();
var variable = new Constant(5);
var names = List.of("Max", "Maria");
```

Type names are important, but variable names can be better.
Types describe a general concept in the context of the entire Java ecosystem (for JDK classes), a general use case (library or framework), or a business domain (application) and thus will always have general names.
Variables on the other hand are defined in a specific and very small context, in which their name can be very precise.

With `var`, variable names become front and center and stand out in a way they didn't before, particularly if code highlighters mark the keyword and thus make it easier to instinctively ignore it.
For a while I spent an hour or two per day reading Kotlin and I immediately got used to this.
It can considerably improve readability.

As pointed out above, the other improvement to readability can come from having more intermediate variables declared because it comes at a cheaper cost when writing and reading.

### Finding A Style

It is of course easy to go overboard with `var` and have code with shitty variable names *and* no visible types.
It is up to us, the community in the large and each team in the small, to come up with a style that suits our needs and strikes a balance between verbosity and clarity.

Brian Goetz, Java language architect at Oracle and in charge of Project Amber, gave a first heuristic:

> Use the `var` construct when it makes the code clearer and more concise and you're not loosing essential information.

In that line I hope IDEs will not generally warn if a type declaration can be replaced with `var`.
This is not an all-the-way construct like lambdas.

### Why Is There No val/const/let?

Many languages that have `var` also offer an additional keyword for immutable variables.
It's often `val` or `const`, sometimes `let`, but Java 10 has neither and we have to use `final var` instead.
The rationale is:

-   while immutability is important, local variables are the least important place for it
-   since Java 8 we have the concept of effectively final, already pushing us into the direction of immutable local variables
-   where `var` was almost universally applauded (74% strongly, 12% mildly in favor) feedback on both `var`/`val` and `var`/`let` was very mixed

I agree with the first two points and have to accept the latter, but I still find the result a bit disappointing.
Having `val` or `let` next to `var` would ease the tension between those developers putting `final` on everything and those appalled by the verbosity.

Well, maybe in the future... until then we have to use `final var`.

## Reflection

When declaring local variables you can use `var` instead of a class or interface name to tell the compiler to infer the type.
This only works if the variable is immediately initialized, for example as in `var s = ""`.
Indexes in `for` loops can also be declared with `var`.
The type inferred by the compiler is put into the bytecode, so nothing changes at runtime - Java is still a statically typed language.

Beyond local variables, for example in fields and method signatures, `var` can not be applied.
This was done to avoid "action at a distance" errors and to keep an inferred variable's use site close to its declaration site, thus easing readability concerns.

While `var` can be used to make code worse, this is a chance for Java developers to write more readable code by finding a new balance between the noise of declarations and the complexity of nested/chained expressions.
For more advanced applications of `var` check out my posts on [intersection types](java-var-intersection-types), [traits](java-var-traits) and [tricks involving anonymous classes](java-var-anonymous-classes-tricks).
