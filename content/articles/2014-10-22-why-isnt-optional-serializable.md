---
title: "Why Isn't `Optional` Serializable?"
tags: [java-next, java-8, optional, serialization, primitive-classes]
date: 2014-10-22
slug: why-isnt-java-optional-serializable
description: "Discussing the reasons for not making Java 8's new type `Optional` serializable."
searchKeywords: "Optional Serializable"
featuredImage: optional-serializable
---

Java 8's new type Optional was met with different reactions and one point of criticism is that it isn't serializable.
Let's have a look at the reasons for that.
A future post will then show how to overcome that fact if it is really necessary.

## Shouldn't `Optional` Be Serializable?

This [question](http://mail.openjdk.java.net/pipermail/jdk8-dev/2013-September/003186.html) was asked back in September 2013 on the [jdk8-dev mailing list](http://mail.openjdk.java.net/pipermail/jdk8-dev/).
In July 2014 [a similar question was posted on StackOverflow](http://stackoverflow.com/q/24547673).

That Optional is not serializable is also noted as a disadvantage of the new type [here (especially in the comments)](http://java.dzone.com/articles/java-8-optional-whats-point) and [here](http://blog.jooq.org/2014/03/28/java-8-friday-optional-will-remain-an-option-in-java/).

To establish the facts: Optional does not implement Serializable.
And it is final, which prevents users from creating a serializable subclass.

So why isn't Optional serializable?

## Return Type

As I described in [my summary of the process which introduced Optional into Java](design-java-optional), it was designed as a return type for methods.
The caller of such a method is expected to immediately check the returned instance.
If the value is present, it should be retrieved; if it is not, a default value should be used or an exception should be thrown.

Used like that, instances of Optional have an extremely short life expectancy.
Put somewhat simplified, they are created at the end of some method's call and discarded a couple of lines later in the calling method.
Serializing it seems to offer little in this scenario.

The discussion which followed the above question to the mailing list contains a number of answers by those involved in creating Optional.
An indeed, their replies follow this argumentation:

> There is a good reason to not allow Optional to implement Serializable, it promotes a bad way to use Optional \[...\]
>
> [Remi Forax](http://mail.openjdk.java.net/pipermail/jdk8-dev/2013-September/003199.html)

> Optional is nice from the API point of view, but not if you store it in a field.
>
> If it's not something that should be stored in field, there is no point to make it serializable.
>
> [Remi Forax](http://mail.openjdk.java.net/pipermail/jdk8-dev/2013-September/003203.html)

> Using Optional as a field type doesn't seem to offer much.
>
> \[...\]
>
> Concern that Optional would be misused in other use cases threatened to derail it's inclusion in Java entirely!
> Optional is being added for the value it offers in "fluent" sequences of statements.
> In this context use of Optional as a visible type or for serialization isn't relevant.
>
> [Mike Diugou](http://mail.openjdk.java.net/pipermail/jdk8-dev/2013-September/003273.html)

> The JSR-335 EG felt fairly strongly that Optional should not be on any more than needed to support the optional-return idiom only.
> (Someone suggested maybe even renaming it to OptionalReturn to beat users over the head with this design orientation; perhaps we should have taken that suggestion.) I get that lots of people want Optional to be something else.
> But, its not simply the case that the EG "forgot" to make it serializable; they explicitly chose not to.
>
> [Brian Goetz](http://mail.openjdk.java.net/pipermail/jdk8-dev/2013-September/003274.html)

(Sidenote: The [proposal to change the name](http://mail.openjdk.java.net/pipermail/lambda-dev/2013-June/010024.html) was made by Stephen Colebourne on the Open JDK mailing list.)

These opinions are often reproduced when others answer questions about Optional.
A good example for that is the [answer given on StackOverflow](http://stackoverflow.com/a/24564612) by [Stuart Marks](http://stackoverflow.com/users/1441122/stuart-marks).
It's an excellent summary of the expert group's intentions about the use of Optional.

But I'm not sure whether that's the whole picture.
First of all, the same arguments apply to `equals` and `hashCode`.
Arguably those methods are even worse, [because they allow to effectively use Optional in collections](http://mail.openjdk.java.net/pipermail/lambda-libs-spec-experts/2013-January/001044.html), something which the EG [wanted to avoid](http://mail.openjdk.java.net/pipermail/lambda-libs-spec-experts/2013-February/001409.html).
Still, they were added to Optional without much ado.

It also stands to reason that not supporting serialization does not help very much in preventing misuse (as seen by the EG):

> \[...\] Keeping Optional non-serializable doesn't do much to prevent that from happening.
> In the vast majority of cases, Optional will be used in a non-serialized context.
> So, as preventative measures go, this isn't a very effective one.
>
> [Joseph Unruh](http://mail.openjdk.java.net/pipermail/jdk8-dev/2013-September/003206.html)

Finally, I couldn't find any discussion on whether Optional should be serializable on the [lambda-libs-spec-experts mailing list](http://mail.openjdk.java.net/pipermail/lambda-libs-spec-experts/).
Something you would expect if it were decided for preventive purposes.

## Lock-In by Serialization

There exists a general argument against serialization from the JDK-developers' point of view:

> Making something in the JDK serializable makes a dramatic increase in our maintenance costs, because it means that the representation is frozen for all time.
> This constrains our ability to evolve implementations in the future, and the number of cases where we are unable to easily fix a bug or provide an enhancement, which would otherwise be simple, is enormous.
> So, while it may look like a simple matter of "implements Serializable" to you, it is more than that.
> The amount of effort consumed by working around an earlier choice to make something serializable is staggering.
>
> [Brian Goetz](http://mail.openjdk.java.net/pipermail/jdk8-dev/2013-September/003276.html)

This certainly makes sense.
Joshua Bloch's excellent book [*Effective Java* (2nd Edition)](https://www.amazon.com/Effective-Java-Edition-Joshua-Bloch/dp/0321356683) contains [a whole chapter about serialization](http://books.google.de/books?id=ka2VUBqHiWkC&pg=PA289&source=gbs_toc_r&cad=3#v=onepage&q&f=false).
Therein he describes the commitment a developer makes when she declares a class serializable.
To make a long story short: it's a big one!

I have no overview over the percentage of serializable classes in the JDK and how this quota changed with Java 8.
But, to pick an example, it looks like most of the classes from the [new date/time API](http://docs.oracle.com/javase/8/docs/api/java/time/package-summary.html) are serializable.

## Value Types

The next big change to the language casts its shadow (and it can already be seen in Java 8): *value types*.
To repeat [what little I already wrote about them](design-java-optional#value-type):

> The gross simplification of that idea is that the user can define a new kind of type, different from classes and interfaces.
> Their central characteristic is that they will not be handled by reference (like classes) but by value (like primitives).
> Or, as Brian Goetz puts it in his introductory article [State of the Values](http://cr.openjdk.java.net/~jrose/values/values-0.html):
>
> > Codes like a class, works like an int!

As described above, value types are not handled by reference, which means they have no [*identitiy*](http://stackoverflow.com/questions/1692863/what-is-the-difference-between-identity-and-equality-in-oop).
This implies that no identity based mechanism can be applied to them.
Some such mechanisms are locking (the lock has to be acquired and released on the same instance), identity comparison (with `==` by checking whether the references point to the same adress) and - [as Brian Goetz and Marko Topolnik were kind enough to explain to me](http://stackoverflow.com/q/26451590/2525313 "Why should Java's value-based classes not be serialized?
on StackOverflow") - serialization.

In Java 8 value types are preceded by *value-based classes*.
Their precise relation in the future is unclear but it could be similar to that of boxed and unboxed primitives (e.g. `Integer` and `int`).
Additionally, the compiler will likely be free to silently switch between the two to improve performance.
Exactly that switching back and forth, i.e.
removing and later recreating a reference, also forbids identity based mechanisms to be applied to value-based classes.
(Imagine locking on a reference which will be removed by the compiler.
This might either make the lock meaningless or lead to a deadlock.)

To allow that change in the future, value-based classes already have similar limitations to those of value types.
As their [documentation](http://docs.oracle.com/javase/8/docs/api/java/lang/doc-files/ValueBased.html) says:

> A program may produce unpredictable results if it attempts to distinguish two references to equal values of a value-based class, whether directly via reference equality or indirectly via an appeal to synchronization, identity hashing, serialization, or any other identity-sensitive mechanism.

You see serialization in there?
Now, guess what!
[Optional](http://docs.oracle.com/javase/8/docs/api/java/util/Optional.html) is a value-based class!

And it's a good thing, too, because it might lead to the compiler being allowed to optimize code using Optional to a degree that makes its impact negligible even in high performance areas.
This also explains the implementation of `equals` and `hashCode`.
Both are central to the definition of value-based classes and are implemented according to that definition.

(I assume that the limitation about serialization is a safety net.
The current concept of value types already alludes to a way to serialize them.
Something which is clearly necessary as not being able to do so would be equivalent to not being able to serialize an `int` , which is just crazy.)

So this might be the final nail that made Optional unserializable.
And even though I like to think that it is The Real Reason^TM^ for that decision, this theory has some holes:

* Why are some other value-based classes, like [LocalDate](http://docs.oracle.com/javase/8/docs/api/java/time/LocalDate.html) and [LocalTime](http://docs.oracle.com/javase/8/docs/api/java/time/LocalTime.html), serializable?
  (That's actually a good question regardless of Optional.
  I'll follow up on that.)
* The timing is not perfect.
  The above discussion on the mailing list (where the EG was adamant in not making it serializable) happened in September 2013, the discussion about Optional's special status in the face of future language changes [started in October 2013](http://mail.openjdk.java.net/pipermail/lambda-libs-spec-experts/2013-October/002329.html).
* Why wouldn't the EG come out and say it?

I guess it's up to you to decide whether those holes sink the theory.

## Reflection

We saw that there are different reasons to not make Optional serializable: The design goal as a type for return values, the technical lock-in produced by having to support serialized forms forever and the limitations of value-based classes which might allow future optimizations.

It is hard to say whether any single one is already a show stopper but together they way heavily against serialization.
But for those undeterred, I will explore how to serialize Optional in another post in the next couple of days.
To stay up to date, subscribe via [RSS](/feed.xml) or [Newsletter](news)!
