---
title: "The Design of `Optional`"
tags: [java-next, java-8, optional]
date: 2014-10-18
slug: design-java-optional
description: "A digest of how `Optional` was introduced in Java 8, summarizing the many discussions about it and their key points based on the mail archive of JSR-335."
searchKeywords: "Optional"
featuredImage: optional-design
---

In [my last post](intention-revealing-code-java-8-optional) I promoted using Java 8's new type [`Optional`](http://docs.oracle.com/javase/8/docs/api/java/util/Optional.html) nearly everywhere as a replacement for null.

As it turns out, this puts me at odds with with the expert group which introduced the type.
This made me curious so I read up on its creation and decided to share my findings here.

The main part of this post is trying to give a summary of the process which lead to the introduction of `Optional` in Java 8.
I will let the experts speak for themselves by quoting them wherever possible.
I hope to properly convey the discourse but as the discussions were frequent, lengthy and sometimes controversial and heated, this is no trivial task.
At the end I will contrast the expert group's reasoning with my own.

## JSR 335

The [Java Specification Request 335](https://www.jcp.org/en/jsr/detail?id=335) dealt with *Lambda Expressions for the Java^TM^ Programming Language*.
Its goal was:

> Extend the Java language to support compact lambda expressions (closures), as well as related language and library features to enable the Java SE APIs to use lambda expressions effectively.

It was this context which lead to the inclusion of `Optional` in Java 8.

Members of the expert group for JSR-335 and strongly involved in the multiple discussions about `Optional` were people like Brian Goetz, Doug Lea and Rémi Forax.
Chiming in were known experts like Joshua Bloch, Tim Peierls and others.

The archive of the mailing list *lambda-libs-spec-experts* is the source for this post.
It can be found [here](http://mail.openjdk.java.net/pipermail/lambda-libs-spec-experts/).
As it is plain text, all layout details like bold face or links were added by me.

## The Road to `Optional`

### First Blood

The prize for first mentioning `Optional` (back in September 2012) seems to go to Rémi Forax, although it was Doug Lea who CC'ed the group's mailing list.
In this mail he gave a quick overview over the reasoning behind the possible need for a new type:

> \[...\] There has been a lot of discussion about \[Optional\] here and there over the years.
> I think they mainly amount to two technical problems, plus at least one style/usage issue:
>
> 1. Some collections allow null elements, which means that you cannot unambiguously use null in its otherwise only reasonable sense of "there's nothing there".
> 2. If/when some of these APIs are extended to primitives, there is no value to return in the case of nothing there.
>    The alternative to Optional is to return boxed types, which some people would prefer not to do.
> 3. Some people like the idea of using Optional to allow more fluent APIs.
>    As in
>
>    `x = s.findFirst().or(valueIfEmpty)`
>
>    vs
>
>    `if ((x = s.findFirst()) == null) x = valueIfEmpty;`
>
>    Some people are happy to create an object for the sake of being able to do this.
>    Although sometimes less happy when they realize that Optionalism then starts propagating through their designs, leading to `Set<Optional<T>>`'s and so on.
>
> It's hard to win here.
>
> [Doug Lea - Sep 14 2012](http://mail.openjdk.java.net/pipermail/lambda-libs-spec-experts/2012-September/000008.html)

(By the way, if there were a motto for the discussions about `Optional`, the last sentence would be it.)

Note that `Optional` is solely described as a return type for queries to a collection, which was discussed in the context of [streams](http://docs.oracle.com/javase/8/docs/api/java/util/stream/Stream.html).
More precisely, it was needed for those [terminal operations](http://docs.oracle.com/javase/8/docs/api/java/util/stream/package-summary.html#StreamOps) which can not return a value if the stream is empty.
(Currently those are [reduce](http://docs.oracle.com/javase/8/docs/api/java/util/stream/Stream.html#reduce-java.util.function.BinaryOperator-), [min](http://docs.oracle.com/javase/8/docs/api/java/util/stream/Stream.html#min-java.util.Comparator-), [max](http://docs.oracle.com/javase/8/docs/api/java/util/stream/Stream.html#max-java.util.Comparator-), [findFirst](http://docs.oracle.com/javase/8/docs/api/java/util/stream/Stream.html#findFirst--) and [findAny](http://docs.oracle.com/javase/8/docs/api/java/util/stream/Stream.html#findAny--).)

It is hard to say whether that shaped the future discourse or just reflected the opinions already held.
But it came to be the sole context in which `Optional` was discussed: as a type for return values.

### Endless Discussions

From then on `Optional` created long exchanges and opposing sides every time it was mentioned.
And not just two sides, either:

> Boy, it seems that one can't discuss Optional in any context without it generating hundreds of messages.
>
> Whatever we do here is a compromise between several poles whose proponents hold very strong opinions.
> There are those that really want [Elvis](https://en.wikipedia.org/wiki/Elvis_operator) instead; there are others who feel that a box-like class for Optional is a hack when it really should be part of the type system.
> Neither group is going to get what they want here; we can compromise and make everyone a little unhappy, or we can do nothing and make everyone unhappy (except that they will still hold out vain hope for their pet feature in the future.)
>
> [Brian Goetz - Jun 5 2013](http://mail.openjdk.java.net/pipermail/lambda-libs-spec-experts/2013-June/001886.html)

An often voiced opinion not mentioned in that particular quote was to forgo `Optional` completely.

> In fact, we don't need Optional at all, because we don't need to return a value that can represent a value or no value, the idea is that methods like findFirst should take a lambda as parameter letting the user to decide what value should be returned by findFirst if there is a value and if there is no value.
>
> [Remi Forax - Mar 6 2013](http://mail.openjdk.java.net/pipermail/lambda-libs-spec-experts/2013-March/001428.html)

So terminal operations which can not return a value if the stream is empty should in that case return a user provided value.
So instead of this:

```java
Optional<T> findFirst();
```

it would be one (or both) of these:

```java
// return a fixed default value if necessary
T findFirst(T defaultValue);

// create a default value if necessary
T findFirst(Supplier defaultValue);
```

Some agreed...

> I am for removing \[Optional\] \[...\] if it doesn't have nearly the same functionality as the Scala Option.
> The way Optional is written right now I would tell people not to use it anyway and it would just be a wart on this API.
>
> [Sam Pullara, Mar 6 2013](http://mail.openjdk.java.net/pipermail/lambda-libs-spec-experts/2013-March/001433.html)

... some didn't ...

> \[Returning the user provided default value\] prevents people from distinguishing between a stream that is empty and a stream containing only the "orElse" value.
Just like Map.get() prevents distinguishing between "not there" and "mapped to null."
>
> [Brian Goetz, Mar 6 2013](http://mail.openjdk.java.net/pipermail/lambda-libs-spec-experts/2013-March/001437.html)

The last sentence hints at an often cited case: The fact that [Map.get(Object key)](http://docs.oracle.com/javase/8/docs/api/java/util/Map.html#get-java.lang.Object-) can return null, which can either mean that the map contains the pair (key, null) or that it does not contain the key.
Both cases are not easily distinguished by the caller.
Everyone on the list agreed that this was a serious shortcoming of the Map API.
Most noted that they would have liked all collections to forbid null as a value (like many [Guava collections](https://code.google.com/p/guava-libraries/wiki/NewCollectionTypesExplained) do) so returning null could always signal "nothing there".

Another opinion about whether to return `Optional` or not was to have both variants.
Then the users would be able to decide whether they want to use `Optional` or not.

> People wanting to avoid Optional can then then get all of the derived versions (allMatch, plain findAny, etc) easily enough.
>
> Surprisingly enough, that's the only missing feature that would otherwise enable a completely Optional-free usage style of the Stream API.
>
> [Doug Lea, Mar 6 2013](http://mail.openjdk.java.net/pipermail/lambda-libs-spec-experts/2013-March/001430.html)

But not everyone aggreed:

> \[...\] the foremost reason I see for not allowing an Optional-free usage style is that people will adopt it rather than use Optional.
They will see it as a license to put null everywhere, and they'll get NPEs way downstream and blame it on Java.
>
> [Tim Peierls, Mar 6 2013](http://mail.openjdk.java.net/pipermail/lambda-libs-spec-experts/2013-March/001432.html)

A [survey](https://www.surveymonkey.com/sr.aspx?sm=c2NqWp6wXUxCUlr6SY05nYEyYIr7ShzH3IgL4OXPIYM_3d) about whether the not-`Optional`-bearing-variants should be added came to a tie of 3 in favor, 3 opposed and 1 abstained.
But it seemed that some voters had the misconception that they could still get rid of `Optional` which made the result unreliable.
Strangely enough, the survey was neither mentioned again nor repeated (or did I overlook something?).

### Convergence

But the discussion slowly converged.
`Optional` would be the return value of those stream operations which needed it (and there would be no `Optional`-free variant).
It would contain some methods for fluent usage at the tail end of stream operations (like [ifPresent](http://docs.oracle.com/javase/8/docs/api/java/util/Optional.html#ifPresent-java.util.function.Consumer-), [orElse](http://docs.oracle.com/javase/8/docs/api/java/util/Optional.html#orElse-T-), [filter](http://docs.oracle.com/javase/8/docs/api/java/util/Optional.html#filter-java.util.function.Predicate-) and [map](http://docs.oracle.com/javase/8/docs/api/java/util/Optional.html#map-java.util.function.Function-)) but not much more.
For example would it not be embedded into the Collection system (by implementing [Iterable](http://docs.oracle.com/javase/8/docs/api/java/lang/Iterable.html)) like [Scala's Option](http://www.scala-lang.org/api/current/index.html#scala.Option).

#### Simplicity

The reason for not adding more functionality was a broad consensus that `Optional` should be kept simple and not support too many different use cases.
Especially its use in collections should be discouraged:

> Optional should be (and currently is) a very limited abstraction, one that is only good for holding a potential result, testing for its presence, retrieving it if it is present, and providing an alternative if not.
> We should resist the temptation to make it into something more or make it into a knock-off of the similar Scala type.
>
> [Tim Peierls, Mar 6 2013](http://mail.openjdk.java.net/pipermail/lambda-libs-spec-experts/2013-March/001432.html)

Others feared that any discouragement would be ignored:

> I don't like it; I think it's going to result in things like: `Map<String,Optional<List<Optional<String>>>>`
>
> [David M.
Lloyd, Sep 14 2012](http://mail.openjdk.java.net/pipermail/lambda-libs-spec-experts/2012-September/000013.html)

Which was answered:

> Only if you really work hard at obfuscating your code.
> I've been using a version of Optional for about a year, and the only time I had reason to use Optional as a type parameter was `Callable<Optional<Result>>`, which conveys exactly what I mean: "Might have a result when it returns."
>
> [Tim Peierls, Sep 14 2012](http://mail.openjdk.java.net/pipermail/lambda-libs-spec-experts/2012-September/000014.html)

Even equals/hashCode were only added to prevent user rage:

> We talked to Kevin \[Kevin Bourrillion from Google - member of the expert group\] about their experiences with Guava's Optional.
> His response was that they felt reasonable hashCode/equals methods were obligatory and without them users would, if not immediately then eventually, curse us for not providing them.
> The implementations are added with grudging reluctance.
>
> [Mike Duigou, Mar 8 2013](http://mail.openjdk.java.net/pipermail/lambda-libs-spec-experts/2013-March/001450.html)

#### Value Type

Besides the goal to limit `Optional`s use, there was another reason to keep the class simple:

> Here's another reason to stay lean: The more limited Optional is, the easier it will be some day to optimize away the extra object.
> Make it a first class participant and you can kiss those optimizations goodbye.
>
> [Tim Peierls, Feb 26 2013](http://mail.openjdk.java.net/pipermail/lambda-libs-spec-experts/2013-February/001409.html)

What Tim Peierls is referring to is the concept of *value types*, which will very likely be introduced in some future version of Java.
The gross simplification of that idea is that the user can define a new kind of type, different from classes and interfaces.
Their central characteristic is that they will not be handled by reference (like classes) but by value (like primitives).
Or, as Brian Goetz puts it in his introductory article [State of the Values](http://cr.openjdk.java.net/~jrose/values/values-0.html):

> Codes like a class, works like an int!

That Java would likely evolve that way led Doug Lea to write this:

> Note that Optional is itself a value-like class, without a public constructor, just factory methods.
>
> The factory methods do not even guarantee to return unique objects.
> For all that the spec does and should say, every call to Optional.of could return the same Optional object.
> (This would require a magical implementation, but still not disallowed, and variants that sometimes return the same one are very much possible.)
>
> This means that there are no object-identity-related guarantees for Optionals.
> `myOptional1 == myOptional2` tells you nothing, and `synchronized(myOptional)` has unpredictable effects -- it might block forever.
>
> [Doug Lea - Oct 19 2013](http://mail.openjdk.java.net/pipermail/lambda-libs-spec-experts/2013-October/002329.html)

This led to another lengthy discussion about how to inform the user about that.
At the end, `Optional`s (and other class') Javadoc contained a small remark, that it is a *value-based* class, which includes a link to [the term's definition](http://docs.oracle.com/javase/8/docs/api/java/lang/doc-files/ValueBased.html).
That definition contains this warning:

> A program may produce unpredictable results if it attempts to distinguish two references to equal values of a value-based class, whether directly via reference equality or indirectly via an appeal to synchronization, identity hashing, serialization, or any other identity-sensitive mechanism.
Use of such identity-sensitive operations on instances of value-based classes may have unpredictable effects and should be avoided.

This defines a small battery of things which must not be done on those classes.
They would most likely work for now, but might break in future versions.

> \[The users\] are more likely to behave, but the special pleading has two motivations \[...\]:
>
> -   discourage users from doing wrong things
> -   provide cover so that when we break code that does wrong things, they were adequately warned
>
> [Brian Goetz - Oct 23 2013](http://mail.openjdk.java.net/pipermail/lambda-libs-spec-experts/2013-October/002361.html)

And with that lookout ended the discussions about `Optional`.
At least for the JSR but judging from the opinions out there, I'd say it just broke free from that mailing list...

## Reflection

In short, the expert group clearly wishes us to only use `Optional` as a type for return values whereas I [recommend to also use it in other situations](intention-revealing-code-java-8-optional).

But I think we share some common ground.
I only compared `Optional` to null and deliberately ignored the design decisions which led to "something not being there" even having a representation (either null or an empty `Optional`).
In many cases the necessity to represent such a thing can be avoided with a different, often clearer design.
A path which should definitely be taken!
And I think this is what the expert group is trying to accomplish: have the programmer look for a better solution than sprinkling `Optional` everywhere.

There might be situations though, were such a design is not feasible for whatever reason.
And in those and *only those*, I recommend to use `Optional` instead of null.

Following this principle will lead to `Optional`s being mostly created as return values.
But I see no reason to reflexively and immediately extract the actual value (or use the default value).
Especially not if the absence of a value might change the logical flow at some point in the future.
The `Optional` box should then be handed over as is (again: if no other way exists).
Another reason would be that the final use of the value *does* allow null (e.g. an argument to a library call).
In that case the `Optional` should be handed around until the very last moment to avoid dealing with null.

But I share the expert group's opinion about collections over `Optional`s: don't do it!
Extract the values and deal with missing ones separately (sure you can't just ignore them?).
Google has a quick guide on [how to handle null in specific collections](https://code.google.com/p/guava-libraries/wiki/UsingAndAvoidingNullExplained#Specific_Cases) and the same concepts apply here.

**So be careful with `Optional`, don't let it be your Bolivian Tree Lizard, but use it if you must!**

Want to join the discussion about `Optional`?
Comment below or answer with a post and ping back.

> Sigh, why does everything related to Optional have to take 300 messages?
>
> [Brian Goetz - Oct 23 2013](http://mail.openjdk.java.net/pipermail/lambda-libs-spec-experts/2013-October/002363.html)
