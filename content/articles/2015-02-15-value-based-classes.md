---
title: "Value-Based Classes"
tags: [java-8, java-next, project-valhalla, primitive-classes]
date: 2015-02-15
slug: java-value-based-classes
description: "An explanation of value-based classes in Java 8. Why do they exist? What are their limitations? How (not) to use them?"
searchKeywords: "value-based classes"
featuredImage: value-based
---

In Java 8 some classes got a small note in Javadoc stating they are *value-based classes*.
This includes a link to a short explanation and some limitations about what not to do with them.
This is easily overlooked and if you do that, it will likely break your code in subtle ways in future Java releases.
To prevent that I wanted to cover value-based classes in their own post - even though I already mentioned the most important bits in other articles.

This post first looks at why value-based classes exist and why their use is limited before detailing those limitations (if you're impatient, jump [here](#limitations)).
It closes with a note on FindBugs, which will soon be able to help you out.

## Background

Let's have a quick look at why value-based classes were introduced and which exist in the JDK.

### Why Do They Exist?

A future version of Java will most likely contain value types.
I will write about them in the coming weeks ([so](https://twitter.com/nipafx) [stay](/feed.xml) [tuned](news)) and will present them in some detail.
And while they definitely have benefits, these are not covered in the present post, which might make the limitations seem pointless.
Believe me, they aren't!
Or don't believe me and [see for yourself](http://cr.openjdk.java.net/~jrose/values/values.html).

For now let's see what little I already wrote about value types:

> The gross simplification of that idea is that the user can define a new kind of type, different from classes and interfaces.
> Their central characteristic is that they will not be handled by reference (like classes) but by value (like primitives).
> Or, as Brian Goetz puts it in his introductory article [State of the Values](http://cr.openjdk.java.net/~jrose/values/values-0.html):
>
> > Codes like a class, works like an int!

It is important to add that value types will be immutable - as primitive types are today.

> In Java 8 value types are preceded by *value-based classes*.
> Their precise relation in the future is unclear but it could be similar to that of boxed and unboxed primitives (e.g. `Integer` and `int`).

The relationship of existing types with future value types became apparent when [`Optional` was designed](design-java-optional#value-type).
This was also when the limitations of value-based classes were specified and documented.

### What Value-Based Classes Exist?

These are all the classes I found in the JDK to be marked as value-based:

`java.util`:
[Optional](http://docs.oracle.com/javase/8/docs/api/java/util/Optional.html), [OptionalDouble](https://docs.oracle.com/javase/8/docs/api/java/util/OptionalDouble.html), [OptionalLong](https://docs.oracle.com/javase/8/docs/api/java/util/OptionalLong.html), [OptionalInt](https://docs.oracle.com/javase/8/docs/api/java/util/OptionalInt.html)

`java.time`:
[Duration](http://docs.oracle.com/javase/8/docs/api/java/time/Duration.html), [Instant](http://docs.oracle.com/javase/8/docs/api/java/time/Instant.html), [LocalDate](http://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html), [LocalDateTime](http://docs.oracle.com/javase/8/docs/api/java/time/LocalDateTime.html), [LocalTime](http://docs.oracle.com/javase/8/docs/api/java/time/LocalTime.html), [MonthDay](http://docs.oracle.com/javase/8/docs/api/java/time/MonthDay.html), [OffsetDateTime](https://docs.oracle.com/javase/8/docs/api/java/time/OffsetDateTime.html), [OffsetTime](http://docs.oracle.com/javase/8/docs/api/java/time/OffsetTime.html), [Period](http://docs.oracle.com/javase/8/docs/api/java/time/Period.html), [Year](https://docs.oracle.com/javase/8/docs/api/java/time/Year.html), [YearMonth](http://docs.oracle.com/javase/8/docs/api/java/time/YearMonth.html), [ZonedDateTime](https://docs.oracle.com/javase/8/docs/api/java/time/ZonedDateTime.html), [ZoneId](https://docs.oracle.com/javase/8/docs/api/java/time/ZoneId.html), [ZoneOffset](https://docs.oracle.com/javase/8/docs/api/java/time/ZoneOffset.html)

`java.time.chrono`:
[HijrahDate](https://docs.oracle.com/javase/8/docs/api/java/time/chrono/HijrahDate.html), [JapaneseDate](http://docs.oracle.com/javase/8/docs/api/java/time/chrono/JapaneseDate.html), [MinguaDate](https://docs.oracle.com/javase/8/docs/api/java/time/chrono/MinguoDate.html), [ThaiBuddhistDate](https://docs.oracle.com/javase/8/docs/api/java/time/chrono/ThaiBuddhistDate.html)

I can not guarantee that this list is complete as I found no official source listing them all.

In addition there are non-JDK classes which should be considered value-based but do not say so.
An example is [Guava's `Optional`](http://docs.guava-libraries.googlecode.com/git/javadoc/com/google/common/base/Optional.html).
It is also safe to assume that most code bases will contain classes which are meant to be value-based.

It is interesting to note that the existing boxing classes like `Integer`, `Double` and the like are not marked as being value-based.
While it sounds desirable to do so - after all they are the prototypes for this kind of classes - this would break backwards compatibility because it would retroactively invalidate all uses which contravene the new limitations.

> `Optional` is new, and the disclaimers arrived on day 1.
> `Integer`, on the other hand, is probably hopelessly polluted, and I am sure that it would break gobs of important code if `Integer` ceased to be lockable (despite what we may think of such a practice.)
>
> [Brian Goetz - Jan 6 2015 (formatting mine)](http://mail.openjdk.java.net/pipermail/valhalla-dev/2015-January/000566.html)

Still, they are very similar so let's call them "value-ish".

## Characteristics

At this point, it is unclear how value types will be implemented, what their exact properties will be and how they will interact with value-based classes.
Hence the limitations imposed on the latter are not based on existing requirements but derived from some desired characteristics of value types.
It is by no means clear whether these limitations suffice to establish a relationship with value types in the future.

That being said, let's continue with the quote from above:

> In Java 8 value types are preceded by *value-based classes*.
> Their precise relation in the future is unclear but it could be similar to that of boxed and unboxed primitives (e.g. `Integer` and `int`).
> Additionally, the compiler will likely be free to silently switch between the two to improve performance.
> Exactly that switching back and forth, i.e. removing and later recreating a reference, also forbids identity-based mechanisms to be applied to value-based classes.

Implemented like this the JVM is freed from tracking the identity of value-based instances, which can lead to substantial performance improvements and other benefits.

### Identity

The term [*identity*](https://today.java.net/pub/a/today/2006/07/27/defining-object-identity.html) is important in this context, so let's have a closer look.
Consider a mutable object which constantly changes its state (like a list being modified).
Even though the object always "looks" different we would still say it's the same object.
So we distinguish between an object's state and its identity.
In Java, state equality is determined with `equals` (if appropriately implemented) and identity equality by comparing references.
In other words, an object's identity is defined by its reference.

Now assume the JVM will treat value types and value-based classes as described above.
In that case, neither will have a meaningful identity.
Value types won't have one to begin with, just like an `int` doesn't.
And the corresponding value-based classes are merely boxes for value types, which the JVM is free to destroy and recreate at will.
So while there are of course references to individual boxes, there is no guarantee at all about how they boxes will exist.

This means that even though a programmer might look at the code and follow an instance of a value-based class being passed here and there, the JVM might behave differently.
It might remove the reference (thus destroying the object's identity) and pass it as a value type.
In case of an identity sensitive operation, it might then recreate a new reference.

With regard to identity it is best to think of value-based classes like of integers: talking about different instances of "3" (the `int`) makes no sense and neither does talking about different instances of "11:42 pm" (the `LocalTime`).

### State

If instances of value-based classes have no identity, their equality can only be determined by comparing their state (which is done by implementing `equals`).
This has the important implication that two instances with equal state must be fully interchangeable, meaning replacing one such instance with another must not have any discernible effect.

This indirectly determines what should be considered part of a value-based instance's state.
All fields whose type is a primitive or another value-based class can be part of it because they are also fully interchangeable (all "3"s and "11:42 pm"s behave the same).
Regular classes are trickier.
As operations might depend on their identity, a vale-based instance can not generally be exchanged for another if they both refer to equal but non-identical instances.

As an example, consider locking on a `String` which is then wrapped in an `Optional`.
At some other point another `String` is created with the same character sequence and also wrapped.
Then these two `Optional`s are not interchangeable because even though both wrap equal character sequences, those `String` instances are not identical and one functions as a lock while the other one doesn't.

Strictly interpreted this means that instead of including the state of a reference field in its own state, a value-based class must only consider the reference itself.
In the example above, the `Optional`s should only be considered equal if they actually point to the same string.

This may be overly strict, though, as the given as well as other problematic examples are necessarily somewhat construed.
And it is very counterintuitive to force value-based classes to ignore the state of "value-ish" classes like `String` and `Integer`.

### Value Type Boxes

Being planned as boxes for value types adds some more requirements.
These are difficult to explain without going deeper into value types so I'm not going to do that now.

## Limitations

First, it is important to note, that in Java 8 all the limitations are purely artificial.
The JVM does not know the first thing about this kind of classes and you can ignore all of the rules without anything going wrong - for now.
But this might change dramatically when value types are introduced.

As we have seen above, instances of value-based classes have no guaranteed identity, less leniency in defining equality and should fit the expected requirements of boxes for value types.
This has two implications:

-   The class must be built accordingly.
-   Instances of the class must not be used for identity-based operations.

This is the ground for the [limitations stated in the Javadoc](https://docs.oracle.com/javase/8/docs/api/java/lang/doc-files/ValueBased.html) and they can hence be separated into limitations for the declaration of the class and the use of its instances.

### Declaration-Side

Straight from the documentation (numbering and formatting mine):

> Instances of a value-based class:
>
> 1. are final and immutable (though may contain references to mutable objects);
> 2. have implementations of `equals`, `hashCode`, and `toString` which are computed solely from the instance's state and not from its identity or the state of any other object or variable;
> 3. make no use of identity-sensitive operations such as reference equality (`==`) between instances, identity hash code of instances, or synchronization on an instances's intrinsic lock;
> 4. are considered equal solely based on `equals()`, not based on reference equality (`==`);
> 5. do not have accessible constructors, but are instead instantiated through factory methods which make no committment as to the identity of returned instances;
> 6. are freely substitutable when equal, meaning that interchanging any two instances `x` and `y` that are equal according to `equals()` in any computation or method invocation should produce no visible change in behavior.

With what was discussed above most of these rules are obvious.

**Rule 1** is motivated by value-based classes being boxes for value types.
For technical and design reasons those must be final and immutable and these requirements are transfered to their boxes.

**Rule 2** [murkily](http://mail.openjdk.java.net/pipermail/valhalla-dev/2015-February/001047.html) addresses the concerns about how to define the state of a value-based class.
The rule's precise effect depends on the interpretation of "the instance's state" and "any other variable".
One way to read it is to include "value-ish" classes in the state and regard typical reference types as other variables.

**Number 3 through 6** regard the missing identity.

It is interesting to note, that `Optional` breaks rule 2 because it calls `equals` on the wrapped value.
Similarly, all value-based classes from `java.time` and `java.time.chrono` break rule 3 by being serializable (which is an identity-based operation - see below; [this thread on the Valhalla mailing list](http://mail.openjdk.java.net/pipermail/valhalla-dev/2015-February/001042.html) talks about this).

### Use-side

Again from the documentation:

> A program may produce unpredictable results if it attempts to distinguish two references to equal values of a value-based class, whether directly via reference equality or indirectly via an appeal to synchronization, identity hashing, serialization, or any other identity-sensitive mechanism.

Considering the missing identity it is straight forward that references should not be distinguished.
There is no explanation, though, why the listed examples are violating that rule, so let's have a closer look.
I made a list of all violations I could come up with and included a short explanation and concrete cases for each (*vbi* stands for *instance of value-based class*):

#### Reference Comparison

This obviously distinguishes instances based on their identity.

#### Serialization of vbi

It is desirable to make value types serializable and a meaningful definition for that seems straight-forward.
But as it is today, serialization makes promises about object identity which conflict with the notion of identity-less value-based classes.
In its current implementation, serialization also uses object identity when traversing the object graph.
So for now, it must be regarded as an identity-based operation which should be avoided.

Cases:

-   non-transient field in serializable class
-   direct serialization via `ObjectOutputStream.writeObject`

#### Locking on a vbi

Uses the object header to access the instance's monitor - headers of value-based classes are free to be removed and recreated and primitive/value types have no headers.

Cases:

-   use in synchronized block
-   calls to `Object.wait`, `Object.notify` or `Object.notifyAll`

#### Identity Hash Code

This hash code is required to be constant over an instance's lifetime.
With instances of value-based classes being free to be removed and recreated constancy can not be guaranteed in a sense which is meaningful to developers.

Cases:

-   argument to `System.identityHashCode`
-   key in an `IdentityHashMap`

Comments highlighting other violations or improving upon the explanations are greatly appreciated!

## FindBugs

Of course it is good to know all this but this doesn't mean a tool which keeps you from overstepping the rules wouldn't be really helpful.
Being a heavy user of [FindBugs](http://findbugs.sourceforge.net/) I decided to ask the project to implement this and created [a feature request](http://sourceforge.net/p/findbugs/feature-requests/313/).
This ticket covers the use-site limitations and will help you uphold them for the JDK's as well as your own value-based classes (marked with an annotation).

Being curious about FindBugs and wanting to contribute I decided to set out and try to implement it myself.
So if you're asking why it takes so long to get that feature ready, now you know: It's my fault.
But talk is cheap so why don't you join me and help out?
I put a FindBugs clone up on GitHub (since deleted) and you can see the progress in this pull request (since deleted).

As soon as that is done I plan to implement the declaration-site rules as well, so you can be sure your value-based classes are properly written and ready when value types finally roll around.

## Reflection

We have seen that value-based classes are the precursor of value types.
With the changes coming to Java these instances will have no meaningful identity and limited possibilities to define their state which creates limitations both for their declaration and their use.
These limitations were discussed in detail.
