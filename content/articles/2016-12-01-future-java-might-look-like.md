---
title: "What Future Java Might Look Like"
tags: [java-next, project-valhalla, generics, pattern-matching, primitive-classes]
date: 2016-12-01
slug: what-future-java-might-look-like
canonicalUrl: https://www.sitepoint.com/what-java-might-one-day-look-like/
description: "Java's future is full of cool advances: data classes, value types, generics over primitives, pattern matching, etc. Let's peek into Java's future!"
featuredImage: what-future-java-might-look-like
---

During the second week of November was [Devoxx Belgium](https://devoxx.be/), Europe's biggest Java conference, and as every year the community's who's-who showed up.
One of them was Brian Goetz, Java Language Architect at Oracle, and he gave what I would consider the conference's most thrilling talk: ["Java Language and Platform Futures: A Sneak Peek"](https://www.youtube.com/watch?v=oGll155-vuQ).
In it he presented ideas that the JDK team is currently kicking around.
And boy, is the pipeline full of great stuff!
Java won't look the same once it's all out in the wild.

When will that be?
Nobody knows.
And that's not *nobody* as in *nobody outside of Oracle*, that's *nobody* as in *nobody knows [whether happy endings exist for arbitrary `n`](https://en.wikipedia.org/wiki/Happy_ending_problem)*.
Brian went to great lengths to stress how very, very speculative all of the following is and how much things might evolve or simply get dropped.
He went so far to let everyone in the audience sign an acknowledgment thereof (just mentally but still) and explicitly forbade any sensationalist tweets.

Well... first of all, this is no tweet and second of all, I wasn't in that audience.
So here we go!
(Seriously though, take this as what it is: a glimpse into one of many, many possible futures.)

## Crash Course

Before we go through the ideas one by one, let's jump right in and have a look at what code might look like that uses all of the envisaged features.
The following class is a simple linked list that uses two types of nodes:

-   `InnerNode`s that contain a value and link to the next node
-   `EndNode`s that only contain a value

One particularly interesting operation is `reduce`, which accepts a seed value and a [`BinaryOperator`](https://docs.oracle.com/javase/8/docs/api/java/util/function/BinaryOperator.html) and applies it to the seed and all of the nodes' values.
This is what that might one day look like:

```java
public class LinkedList<any T> {

	private Optional<Node<T>> head;

	// [constructors]
	// [list mutation by replacing nodes with new ones]

	public T reduce(T seed, BinaryOperator<T> operator) {
		var currentValue = seed;
		var currentNode = head;

		while (currentNode.isPresent()) {
			currentValue = operator
					.apply(currentValue, currentNode.get().getValue());
			currentNode = switch (currentNode.get()) {
				case InnerNode(_, var nextNode) -> Optional.of(nextNode);
				case EndNode(_) -> Optional.empty();
				default: throw new IllegalArgumentException();
			}
		}

		return currentValue;
	}

	private interface Node<any T> {
		T getValue();
	}

	private static class InnerNode<any T>(T value, Node<T> next)
			implements Node<T> { }

	private static class EndNode<any T>(T value)
			implements Node<T> { }

}
```

Wow!
Hardly Java anymore, right?!
Besides the omitted constructors there's only code that actually *does* something - I mean, where's all the boilerplate?
And what if I told you that on top of that performance would be much better than today?
Sounds like a free lunch, heck, like an entire free all-you-can-eat buffet!

Here's what's new:

-   The generic type argument is marked with `any` - what's up with that?
-   Where are the type information for `currentValue` and `currentNode` in `reduce`?
-   That `switch` is almost unrecognizable.
-   The classes `InnerNode` and `EndNode` look, err, empty.

Let's look at all the ideas that went into this example.

## Data Objects

When was the last time you created a domain object that was essentially a dumb data holder, maybe with one or two non-trivial methods, that still required a hundred lines for constructors, static factory methods, accessors, [`equals`](https://www.sitepoint.com/implement-javas-equals-method-correctly/), [`hashCode`](https://www.sitepoint.com/how-to-implement-javas-hashcode-correctly/), and `toString`.
(Right now, you say?
Don't worry, I don't judge.) And while IDEs happily generate all of that, making typing it unnecessary even today, it is still code that needs to be understood (does the constructor do any validation?) and maintained (better not forget to add that new field to `equals`).

In an aggressive move to reduce boilerplate, the compiler might generate all of that stuff on the fly without us having to bend a finger!

Here's what a user might look like:

```java
public class User(String firstName, String lastName, DateTime birthday) { }
```

We can get everything else I mentioned above for free and only need to actually implement what's non-standard (maybe users have an ID that alone determines equality, so we'd want an according `equals` implementation).
Getting rid of all that code would be a great boost for maintainability!

Looking at the linked list example we can see that `InnerNode` and `EndNode` depend on this feature.

## Value Types

When Java was created an arithmetic operation and a load from main memory took about the same number of cycles (speaking in magnitudes here).
This changed considerably over the last 20 and more years to the point where memory access is about three magnitudes slower.

That all abstract Java types are objects, linked to each other via references, requires pointer hunting and makes the problem even worse.
The benefits are that such types have identity, allow mutability, inheritance, and a couple of other things... which we don't actually always need.
This is very unsatisfactory and something needs to be done!

In comes [Project Valhalla](http://openjdk.java.net/projects/valhalla/), as part of which value types are being developed as we speak.
They can be summarized as self-defined primitives.
Here's a simple example:

```java
value class ComplexNumber {

	double real;
	double imaginary;

	// constructors, getters, setters, equals, hashCode, toString
}
```

Looks like a regular class - the only difference is the keyword `value` in there.

Like primitives, value types incur neither memory overhead nor indirection.
A self-defined `ComplexNumber`, like the one above with two `double` fields `real` and `imaginary`, will be inlined wherever it is used.
Like primitives, such numbers have no identity - while there can be two different `Double` objects with value 5.0, there can't be two different doubles 5.0.
This precludes some of the things we like to do to objects: setting them to null, inheriting, mutating, and locking.
In turn, it will only require the memory needed for those two doubles and an array of complex numbers will essentially be an array of real/imaginary pairs.

Like classes, value types can have methods and fields, encapsulate internals, use generics, and implement interfaces (but not extend other classes).
Thus the slogan: ["Codes like a class, works like an int."](http://cr.openjdk.java.net/~jrose/values/values-0.html) This will allow us to no longer weigh an abstraction we would prefer against the performance (we imagine) we need.

Talking about performance, the advantages are considerable and can speed up just about any code.
In a `HashMap`, for example, the nodes could become value types, speeding up one of Java's most ubiquitous data structures.
But this is not a low-level feature only hardcore library developers will want to use!
It allows all of us to chose the right abstraction and inform the compiler as well as our colleagues that some of our objects in fact aren't objects but values.

By the way, my personal guess is that the compiler would be just as helpful as with data objects and chip in constructors, getters, setters, etc.:

```java
value class ComplexNumber(double real, double imaginary) { }
```

In case this wasn't perfectly obvious: This is a deep change and interacts with basically everything:

-   the language (generics, wildcards, raw types, ...)
-   the core libraries (collections, streams)
-   the JVM (type signatures, bytecodes, ...)

So... where exactly in the linked list example do value types come in?
Admittedly, they don't play a big role.
If I were clever enough to write a [persistent data structure](https://en.wikipedia.org/wiki/Persistent_data_structure), the nodes could be value types (remember, they have to be immutable), which could be pretty interesting.

But there's one possible value type in there: `Optional`.
In Java 8 it is already marked as a [value-based class](java-value-based-classes), something that might one day become a value type or a wrapper thereof.
This makes it flat and eliminates the memory indirection and possible cache miss it currently imposes.

## Specialized Generics

With everybody and their dog creating primitive-like value types it becomes necessary to look at how they interact with parametric polymorphism.
As you know, generics do not work for primitives - there can't be an `ArrayList<int>`.
This is already painful with eight primitives (see the [primitive specializations of Stream](http://hg.openjdk.java.net/jdk8/jdk8/jdk/file/687fd7c7986d/src/share/classes/java/util/stream/IntPipeline.java) or libraries like [Trove](https://bitbucket.org/trove4j/trove)) but becomes unbearable when developers can define more.
If value types would have to be boxed to interact with generics (like primitives are today), their use would be fairly limited and they would be a non-starter.

So we want to be able to [use generics with value types](http://cr.openjdk.java.net/~briangoetz/valhalla/specialization.html) - and primitives can come along for the ride.
In the end we not only want to instantiate an `ArrayList<int>` or `ArrayList<ComplexNumber>`, we also want it to be backed by an `int[]` or `ComplexNumber[]`, respectively.
This is called specialization and opens a whole new can of worms.
(To take a good look at those worms, watch the talk ["Adventures in Parametric Polymorphism"](https://www.youtube.com/watch?v=Tc9vs_HFHVo), which Brian gave at [JVMLS 2016](https://www.sitepoint.com/jvmls-2016#projectvalhalla).
That article also contains a list of talks you can watch if you want to get deeper.)

Code that wants to generify not only over reference types but also over value types must mark the respective type parameters with `any`.
You can see that `LinkedList`, `Node`, and its implementations do exactly that.
This means that in a `LinkedList<int>` the nodes would actually have `int` fields as opposed to the `Object` fields holding boxed `Integer`s as would be the case with a `LinkedList<Integer>` nowadays.

## More Type Inference

Java has done type inference since Java 5 (for type witnesses in generic methods) and the mechanism was extended in Java 7 (diamond operator), 8 (lambda parameter types), and 9 (diamond on anonymous classes).
In Java X it might very well cover variable declarations.
Brian's example is this one:

```java
// now
URL url = new URL("...")
URLConnectoin conn = url.openConnection();
Reader reader = new BufferedReader(
		new InputStreamReader(conn.getInputStream()));

// maybe in the future
var url = new URL("...")
var conn = url.openConnection();
var reader = new BufferedReader(
		new InputStreamReader(conn.getInputStream()));
```

Here, the types of `url`, `conn`, and `reader` are perfectly obvious.
As a consequence the compiler can infer them, making it unnecessary for us to specify them.
In general, type inference can reduce boilerplate but also hide essential information.
If you consider variable names to be more important than their types, you'll like this as it aligns the names perfectly while throwing out redundant information.

Note that type inference is *not* dynamic typing - it's still strong typing just with less typing (Brian's pun - presumably intended).
The type information will still end up in the bytecode and IDEs will also be able to show them - it's just that we don't have to write it out anymore.

An automatic process deducing types implies that code changes will change the outcome of that computation.
While it is generally ok for a local variable to change its type (e.g. to its supertype), the same is not true for fields, method parameters or return values, etc.
On the contrary, any change here could cause binary incompatibilities, which would lead to code compiled against an old version failing to link at runtime.
Not good and hence forbidden.

So that only local variables' types are inferred is more about protecting the ecosystem from unstable code than protecting developers from unreadable code.

## Pattern Matching

[Java's current `switch` statement](https://www.sitepoint.com/javas-switch-statement/) is pretty weak.
You can use it for primitives, enums and strings but that's it.
If you want to do anything more complex, you either resort to if-else-if chains or, if you can't get the Gang of Four book out of your head, the [visitor pattern](https://en.wikipedia.org/wiki/Visitor_pattern).

But think about it, there's not really an intrinsic reason for these limitations.
On a higher level a switch can be described to be using a variable to evaluate some conditions and choosing a matching branch, evaluating what it finds there - why should the variable's type be so limited and the conditions only check equality?
Come to think of it, why would the `switch` only *do* something as opposed to *become* something.
Following this trail we end up with pattern matching, which has none of these limitations.

First of all, all kinds of variables could be allowed.
Secondly, conditions could be much broader.
They could, for example, check types or even deconstruct entire data objects.
And last but not least, the whole `switch` should be an expression, evaluated to the expression in the branch of the matching condition.

Here are Brian's examples:

```java
// matching types
String formatted;
switch (constant) {
	case Integer i: formatted = String.format("int %d", i); break;
	case Byte b: //...
	case Long l: // ...
	// ...
	default: formatted = "unknown"
}

// used as an expression
String formatted = switch (constant) {
	case Integer i -> String.format("int %d", i);
	case Byte b: //...
	case Long l: // ...
	// ...
	default: formatted = "unknown"
}

// deconstructing objects
int eval(ExprNode node) {
	return switch (node) {
		case ConstantNode(var i) -> i;
		case NegNode(var node) -> -eval(node);
		case PlusNode(var left, var right) -> eval(left) + eval(right);
		case MulNode(var left, var right) -> eval(left) * eval(right);
		// ...
	}
}
```

For the linked list I also used it as an expression and to deconstruct the nodes:

```java
currentNode = switch (currentNode.get()) {
	case InnerNode(_, var nextNode) -> Optional.of(nextNode);
	case EndNode(_) -> Optional.empty();
	default: throw new IllegalArgumentException();
}
```

Much nicer than what it would have to look like now:

```java
if (currentNode.get() instanceof InnerNode) {
	currentNode = Optional.of(((InnerNode) currentNode.get()).getNext());
} else if (currentNode.get() instanceof EndNode) {
	currentNode = Optional.empty();
} else {
	throw new IllegalArgumentException();
}
```

(Yes, I know, this particular example could be solved with polymorphism.)

## Summary

Again, wow!
Data objects, value types, generic specialization, more type inference, and pattern matching - that's a set of huge features the JDK team is working on.
I can't wait for them to come out!
(By the way, while I presented all the features here, Brian provides so much more interesting background - you should definitely check out [the entire talk](https://www.youtube.com/watch?v=oGll155-vuQ).)

What do you think?
Would you like to code in that Java?
