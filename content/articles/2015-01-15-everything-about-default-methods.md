---
title: "Everything You Need To Know About Default Methods"
tags: [default-methods, java-8, java-basics]
date: 2015-01-15
slug: java-default-methods-guide
description: "Covering literally everything there is to know about Java 8's default methods."
searchKeywords: "Default Methods"
featuredImage: concepts-default-methods
---

So, default methods... yesterday's news, right?
Yes but after a year of use, a lot of facts accumulated and I wanted to gather them in one place for those developers who are just starting to use them.
And maybe even the experienced ones can find a detail or two they didn't know about yet.

I guess I failed in giving this post a meaningful narrative.
The reason is that, in its heart, it's a wiki article.
It covers different concepts and details of default methods and while these are naturally related, they do not lend themselves to a continuous narration.

But this has an upside, too!
You can easily skip and jump around the post without degrading your reading experience much.
Check the table of contents for a complete overview over what's covered and go where your curiosity leads you.

## Default Methods

By now most developers will already have used, read and maybe even implemented default methods, so I'm going to spare everyone a detailed introduction of the syntax.
I'll spend some more time on its nooks and crannies before covering broader concepts.

### Syntax

What the new language feature of default methods comes down to is that interfaces can now declare non-abstract methods, i.e.
ones with a body.

The following example is a modified version of `Comparator.thenComparing(Comparator)` ([link](http://docs.oracle.com/javase/8/docs/api/java/util/Comparator.html#thenComparing-java.util.Comparator-)) from JDK 8:

```java
default Comparator<T> thenComparing(Comparator<? super T> other) {
	return (o1, o2) -> {
		int res = this.compare(o1, o2);
		return (res != 0) ? res : other.compare(o1, o2);
	};
}
```

This looks just like a "regular" method declaration except for the keyword `default`.
This is necessary to add such a method to an interface without a compile error and hints at the [method call resolution strategy](#Resolution-Strategy).

Every class which implements `Comparator` will now contain the public method `thenComparing(Comparator)` without having to implement it itself - it comes for free, so to speak.

#### Explicit Calls to Default Methods

Further below, we will see some reasons why one might want to explicitly call a default implementation of a method from some specific superinterface.
If the need arises, this is how it's done:

```java
class StringComparator implements Comparator<String> {

	// ...

	@Override
	public Comparator<String> thenComparing(
			Comparator<? super String> other) {
		log("Call to 'thenComparing'.");
		return Comparator.super.thenComparing(other);
	}
}
```

Note how the name of the interface is used to specify the following `super` which would otherwise refer to the superclass (in this case `Object`).
This is syntactically similar to how the [reference to the outer class](http://docs.oracle.com/javase/tutorial/java/javaOO/nested.html#shadowing) can be accessed from a nested class.

It is not possible to call a method from an interface that is not mentioned in the `implements` clause.
If, for example, our `StringComparator` were to implement `ObjectComparator<T> extends Comparator<T>`, the call `Comparator.super.thenComparing` would cause a compile error.
When implementing two interfaces where one extends the other `Comparator.super` causes a different compile error.
Together this means that it is not possible to explicitly call overridden or [reabstracted](#re-abstracting-methods) default methods.

(Thanks to [charlie](java-default-methods-guide)<!-- comment-2444568189 --> for pointing me into the right direction.)

### Resolution Strategy

So let's consider an instance of a type which implements an interface with default methods.
What happens if a method is called for which a default implementation exists?
(Note that a method is identified by its [signature](http://docs.oracle.com/javase/specs/jls/se8/html/jls-8.html#jls-8.4.2 "§8.4.2.
Method Signature - Java Language Specification"), which consists of the name and the parameter types.)

> **Rule \#1**:
> :   Classes win over interfaces.
If a class in the superclass chain has a declaration for the method (concrete or abstract), you're done, and defaults are irrelevant.
>
> **Rule \#2**:
> :   More specific interfaces win over less specific ones (where specificity means "subtyping").
A default from `List` wins over a default from `Collection`, regardless of where or how or how many times `List` and `Collection` enter the inheritance graph.
>
> **Rule \#3**:
> :   There's no Rule \#3.
If there is not a unique winner according to the above rules, concrete classes must disambiguate manually.
>
> [Brian Goetz - Mar 3 2013 (formatting mine)](http://mail.openjdk.java.net/pipermail/lambda-dev/2013-March/008435.html)

First of all, this clarifies why these methods are called *default methods* and why they must be started off with the keyword `default`:

Such an implementation is a backup in case a class and none of its superclasses even consider the method, i.e.
provide no implementation and are not declaring it as abstract (see **Rule \#1**).
Equivalently, a default method of interface `X` is only used when the class does not also implement an interface `Y` which extends `X` and declares the same method (either as default or abstract; see **Rule \#2**).

While these rules are simple, they do not prevent developers from creating complex situations.
[This post](http://zeroturnaround.com/rebellabs/how-your-addiction-to-java-8-default-methods-may-make-pandas-sad-and-your-teammates-angry/ "How your addiction to Java 8 default methods may make pandas sad and your teammates angry!
by Oleg Shelajev") gives an example where the resolution is not trivial to predict and arguments that this feature should be used with care.

The resolution strategy implies several interesting details...

#### Conflict Resolution

**Rule \#3**, or rather its absence, means that concrete classes must implement each method for which competing default implementations exist.
Otherwise the compiler throws an error.
If one of the competing implementations is appropriate, the method body can just [explicitly call that method](#Explicit-Calls-to-Default-Methods).

This also implies that adding default implementations to an interface can lead to compile errors.
If a class `A` implements the unrelated interfaces `X` and `Y` and a default method which is already present in `X` is added to `Y`, class `A` will not compile anymore.

What happens if `A`, `X` and `Y` are not compiled together and the JVM stumbles upon this situation?
Interesting question to which [the answer seems somewhat unclear](https://jvilk.com/blog/java-8-specification-bug/).
Looks like the JVM will throw an `IncompatibleClassChangeError`.

#### Re-Abstracting Methods

If an abstract class or interface `A` declares a method as abstract for which a default implementation exists in some superinterface `X`, the default implementation of `X` is overridden.
Hence all concrete classes which subtype `A` must implement the method.
This can be used as an effective tool to enforce the reimplementation of inappropriate default implementations.

This technique is used throughout the JDK, e.g. on `ConcurrentMap` ([link](http://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ConcurrentMap.html)) which re-abstracts a number of methods for which `Map` ([link](http://docs.oracle.com/javase/8/docs/api/java/util/Map.html).

Note that concrete classes can not [explicitly call the overridden default implementation](#Explicit-Calls-to-Default-Methods).

#### Overriding Methods on 'Object'

It is not possible for an interface to provide default implementations for the methods in `Object`.
Trying to do so will result in a compile error.
Why?

Well first of all, it would be useless.
Since every class inherits from `Object`, **Rule \#1** clearly implies that those methods would never be called.

But that rule is no law of nature and the expert group could have made an exception.
The mail which also contains the rules, [Brian Goetz gives many reasons](http://mail.openjdk.java.net/pipermail/lambda-dev/2013-March/008435.html) why they didn't.
The one I like best (formatting mine):

> At root, the methods from `Object` -- such as `toString`, `equals`, and `hashCode` -- are all about the object's **state**.
But interfaces do not have state; classes have state.
These methods belong with the code that owns the object's state -- the class.

### Modifiers

Note that there are a lot of modifiers you can not use on default methods:

-   the visibility is fixed to public (as on other interface methods)
-   the keyword `synchronized` is forbidden (as on abstract methods)
-   the keyword `final` is forbidden (as on abstract methods)

Of course these features were requested and comprehensive explanations for their absence exist (e.g. for [final](https://stackoverflow.com/a/23476994/2525313) and [synchronized](https://stackoverflow.com/a/23463334/2525313)).
The arguments are always similar: This is not what [default methods were intended for](#Interface-Evolution) and introducing those features will result in more complex and error prone language rules and/or code.

You can use `static` though, which will reduce the need for [plural-form utility classes](#Ousting-Utility-Classes).

## A Little Context

Now that we know all about how to use default methods let's put that knowledge into context.

### Interface Evolution

The expert group which introduced default methods can often be found stating that their goal was to allow "interface evolution":

> The purpose of *default methods* \[...\] is to enable interfaces to be evolved in a compatible manner after their initial publication.
>
> [Brian Goetz - Sep 2013](http://cr.openjdk.java.net/~briangoetz/lambda/lambda-state-final.html)

Before default methods it was practically impossible (excluding some organizational patterns; see [this nice overview](http://blog.jooq.org/2013/02/01/defensive-api-evolution-with-java-interfaces/)) to add methods to interfaces without breaking all implementations.
While this is irrelevant for the vast majority of software developers which also control those implementations, it is a crucial problem for API designers.
Java always stayed on the safe side and never changed interfaces after they were released.

But with the introduction of lambda expressions, this became unbearable.
Imagine the collective pain of always writing `Stream.of(myList).forEach(...)` because `forEach` could no be added to `List`.

So the [expert group which introduced lambdas](https://www.jcp.org/en/jsr/detail?id=335) decided to find a way to enable interface evolution without breaking any existing implementations.
Their focus on this goal explains the [characteristics of default methods](#Default-Methods).

This not only allows to add methods like the JDK did.
It is also opens up the possibility to refactor or remove interface methods in a backwards-compatible manner if clients can be expected to update their code in a transition phase.
It is even possible to formulate [cookbook rules for that process](java-default-methods-interface-evolution).

Where the group deemed it possible without degrading usability of this primary use case, they also enabled the use of default methods to create [traits](#Default-Methods-vs-Mixins-and-Traits) -- or rather something close to them.
Still, they were frequently attacked for not going "all the way" to mixins and traits, to which the often repeated answer was: "Yes, because that is/was not our goal."

### Ousting Utility Classes

The JDK and especially common auxiliary libraries like [Guava](https://github.com/google/guava) and [Apache Commons](http://commons.apache.org/) are full of utility classes.
Their name is usually the plural form of the interface they are providing their methods for, e.g. [Collections](http://docs.oracle.com/javase/8/docs/api/java/util/Collections.html) or [Sets](http://docs.guava-libraries.googlecode.com/git/javadoc/com/google/common/collect/Sets.html).
The primary reason for their existence is that those utility methods could not be added to the original interface after its release.
With default methods this becomes possible.

All those static methods which take an instance of the interface as an argument can now be transformed into a default method on the interface.
As an example, look at the static `Collections.sort(List)` ([link](http://docs.oracle.com/javase/8/docs/api/java/util/Collections.html#sort-java.util.List-)), which as of Java 8 simply delegates to the new instance default method `List.sort(Comparator)` ([link](http://docs.oracle.com/javase/8/docs/api/java/util/List.html#sort-java.util.Comparator-)).
Another example is given in my post on [how to use default methods to improve the decorator pattern](decorator-pattern-default-methods).
Other utility methods which take no arguments (usually builders) can now become static default methods on the interface.

While removing all interface-related utility classes in a code base is possible, it might not be advisable.
The usability and cohesiveness of the interface should remain the main priority -- not stuffing every imaginable feature in there.
My guess is that it only makes sense to move the most general of those methods to the interface while more obscure operations could remain in one (or more?) utility classes.
(Or [remove them entirely](http://www.yegor256.com/2014/05/05/oop-alternative-to-utility-classes.html), if you're into that.)

### Classification

In his argument for new [Javadoc tags](#Documentation), Brian Goetz weakly classifies the default methods which were introduced into the JDK so far (formatting mine):

> **1. Optional methods**:
>
> This is when the default implementation is barely conformant, such as the following from Iterator:
>
> ```java
> default void remove() {
>     throw new UnsupportedOperationException("remove");
> }
> ```
>
> It adheres to its contract, because the contract is explicitly weak, but any class that cares about removal will definitely want to override it.
>
> **2. Methods with *reasonable* defaults but which might well be overridden by implementations that care enough**:
>
> For example, again from Iterator:
>
> ```java
> default void forEach(Consumer<? super E> consumer) {
>     while (hasNext())
>         consumer.accept(next());
> }
> ```
>
> This implementation is perfectly fine for most implementations, but some classes (e.g., `ArrayList`) might have the chance to do better, if their maintainers are sufficiently motivated to do so.
> The new methods on `Map` (e.g., `putIfAbsent`) are also in this bucket.
>
> **3. Methods where its pretty unlikely anyone will ever override them**:
>
> Such as this method from Predicate:
>
> ```java
> default Predicate<T> and(Predicate<? super T> p) {
>     Objects.requireNonNull(p);
>     return (T t) -> test(t) && p.test(t);
> }
> ```
>
> [Brian Goetz - Jan 31 2013](http://mail.openjdk.java.net/pipermail/lambda-libs-spec-experts/2013-January/001211.html)

I call this classification "weak" because it naturally lacks hard rules about where to place a method.
That does not make it useless, though.
Quite the opposite, I consider it a great help in communicating about them and a good thing to keep in mind while reading or writing default methods.

### Documentation

Note that default methods were the primary reason to introduce the new (unofficial) Javadoc tags **@apiNote**, **@implSpec** and **@implNote**.
The JDK makes frequent use of them, so it is important to understand their meaning.
A good way to learn about them is to read [my last post](javadoc-tags-apiNote-implSpec-implNote) (smooth, right?), which covers them in all detail.

## Inheritance and Class-Building

Different aspects of inheritance and how it is used to build classes often come up in discussions about default methods.
Let's take a closer look at them and see how they relate to the new language feature.

### Multiple Inheritance -- Of What?

With inheritance a type can assume characteristics of another type.
Three kinds of characteristics exist:

-   **type**, i.e.
by subtyping a type *is* another type
-   **behavior**, i.e.
a type inherits methods and thus behaves the same way as another type
-   **state**, i.e.
a type inherits the variables defining the state of another type

Since classes subtype their superclass and inherit all methods and variables, class inheritance clearly covers all three of those characteristics.
At the same time, a class can only extend one other class so this is limited to single inheritance.

Interfaces are different: A type can inherit from many interfaces and becomes a subtype of each.
So Java has been supporting this kind of multiple inheritance from day 1.

But before Java 8 an implementing class only inherited the interface's type.
Yes, it also inherited the contract but not its actual implementation so it had to provide its own behavior.
With default methods this changes so from version 8 on Java supports multiple inheritance of behavior as well.

Java still provides no explicit way to inherit the state of multiple types.
Something similar can be achieved with default methods, though, either with an [evil hack](http://kerflyn.wordpress.com/2012/07/09/java-8-now-you-have-mixins/ "Java 8: Now You Have Mixins?
by François Sarradin") or the [virtual field pattern](http://mail.openjdk.java.net/pipermail/lambda-dev/2012-August/005455.html).
The former is dangerous and should never be used, the latter also has some drawbacks (especially regarding encapsulation) and should be used with great care.

### Default Methods vs Mixins and Traits

When discussing default methods, they are sometimes compared to [mixins](https://en.wikipedia.org/wiki/Mixins) and [traits](https://en.wikipedia.org/wiki/Trait_%28computer_programming%29).
This article can not cover those in detail but will give a rough idea how they differ from interfaces with default methods.
(A helpful comparison of mixins and traits can be found on [StackOverflow](http://stackoverflow.com/q/925609/2525313).)

#### Mixins

Mixins allow to inherit their type, behavior and state.
A type can inherit from several mixins, thus providing multiple inheritance of all three characteristics.
Depending on the language one might also be able to add mixins to single instances at runtime.

As interfaces with default methods allow no inheritance of state, they are clearly no mixins.

#### Traits

Similar to mixins, traits allow types (and instances) to inherit from multiple traits.
They also inherit their type and behavior but unlike mixins, conventional traits do not define their own state.

This makes traits similar to interfaces with default methods.
The concepts are still different, but those differences are not entirely trivial.
I might come back to this in the future and write a more detailed comparison but until then, I will leave you with some ideas:

-   As we've seen, [method call resolution](#Resolution-Strategy) is not always trivial which can quickly make the interaction of different interfaces with default methods a complexity burden.
Traits typically alleviate this problem one way or another.
-   Traits allow certain operations which Java does not fully support.
See the bullet point list after "selection of operations" in the [Wikipedia article about traits](http://en.wikipedia.org/wiki/Trait_%28computer_programming%29).
-   The paper ["Trait-oriented Programming in Java 8"](http://dl.acm.org/citation.cfm?id=2647520) explores a trait-oriented programming style with default methods and encounters some problems.

So while interfaces with default methods are no traits, the similarities allow to use them in a limited fashion like they were.
This is in line with [the expert group's design goal](#Interface-Evolution) which tried to accommodate this use-case wherever it did not conflict with their original goal, namely interface evolution and ease of use.

### Default Methods vs Abstract Classes

Now that interfaces can provide behavior they inch into the territory of abstract classes and soon the question arises, which to use in a given situation.

#### Language Differences

Let's first state some of the differences on the language level:

While interfaces allow multiple inheritance they fall short on basically every other aspect of class-building.
Default methods are never final, can not be synchronized and can not override `Object`'s methods.
They are always public, which severely limits the ability to write short and reusable methods.
Furthermore, an interface can still not define fields so every state change has to be done via the public API.
Changes made to an API to accommodate that use case will often break encapsulation.

Still, there are some use cases left, in which those differences do not matter and both approaches are technically feasible.

#### Conceptual Differences

Then there are the conceptual differences.
Classes define what something *is*, while interfaces usually define what something *can do*.

And abstract classes are something special altogether.
[Effective Java's item 18](http://books.google.de/books?id=ka2VUBqHiWkC&lpg=PP1&pg=PA93#v=onepage&q&f=true) comprehensively explains why interfaces are superior to abstract classes for defining types with multiple subtypes.
(And this does not even take default methods into account.) The gist is: Abstract classes are valid for skeletal (i.e.
partial) implementations of interfaces but should not exist without a matching interface.

So when abstract classes are effectively reduced to be low-visibility, skeletal implementations of interfaces, can default methods take this away as well?
Decidedly: *No!* Implementing interfaces almost always requires some or all of those class-building tools which default methods lack.
And if some interface doesn't, it is clearly a special case, which should not lead you astray.
(See [this earlier post](java-non-capturing-lambdas) about what can happen when an interface is implemented with default methods.)

## More Links

I wrote some [other posts about default methods](tag:default-methods) but I want to explicitly recommend one which presents precise steps on how to use default methods for their intended goal:

-   [Interface Evolution With Default Methods](java-default-methods-interface-evolution)

And the internet is of course full of articles about the topic:

-   final version of [State of the Lambda](http://cr.openjdk.java.net/~briangoetz/lambda/lambda-state-final.html) (chapter 10 covers default methods)
-   [official tutorial](http://docs.oracle.com/javase/tutorial/java/IandI/defaultmethods.html)
-   [official tutorial on how to evolve interfaces](http://docs.oracle.com/javase/tutorial/java/IandI/nogrow.html)
-   [StackOverflow question (and answer by Brian Goetz) about default methods as traits](http://stackoverflow.com/q/28681737/2525313)

## Reflection

This article should have covered **everything** one needs to know about default methods.
If you disagree, [tweet](https://twitter.com/nipafx), [mail](mailto:nicolai@nipafx.dev) or leave a comment.
Approval and +1's are also acceptable.
