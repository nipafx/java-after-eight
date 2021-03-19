---
title: "Why Elvis Should Not Visit Java"
tags: [clean-code, optional, rant]
date: 2017-01-31
slug: why-elvis-should-not-visit-java
description: "The desire for the Elvis operator for easier null-handling echoes through the Java community. But due to Java's type system, it should never be introduced!"
intro: "The desire for the Elvis operator (null-safe member selection) as a killer feature for terse null-handling echoes through the Java community. It should never be introduced, though, and here's why."
searchKeywords: "elvis"
featuredImage: elvis-in-java
---

I was recently involved in [quite a long Twitter discussion](https://twitter.com/struberg/status/824684380937449472) regarding Java's `Optional`, type systems that distinguish nullable and non-nullable types and [the Elvis operator](java-pirate-elvis-operator), which allows null-safe member selection.
The latter was peddled as a killer feature for succinct null-handling, which I strongly disagree with.

My opinion on the matter is that without a type system that allows making every type non-nullable (something that is not going to happen in Java any time soon) the Elvis operator would be detrimental to correctness and readability.

Let me explain why.

<admonition type="note">

Even though I know better, in this post I accidentally call the null-safe member selection operator `?.` the *Elvis operator*.
That's wrong - `?:` as in `streetName ?: "Unknown Street"` is Elvis.
But, as a saving grace, in [a post about how to roll your own Elvis operator](java-pirate-elvis-operator#the-pirate-elvis-operator), I call `?.` (one eyed with a pompadour) *Pirate Elvis*.
With that, it's not quite as wrong anymore that in this post here, I use Elvis for `?.`.

</admonition>

## The Crux With Null

I already [wrote about this before](intention-revealing-code-java-8-optional#why-even-use-optional).
The issue with null is *not* that it causes exceptions - that's just a symptom.
The problem with null is that it says nothing about *why* the value is missing.
Was something tried and failed (like connecting to the database) but for some reason the execution continued?
Is there a number of values (maybe a pair?) where only one could ever be present?
Is the value just optional, like non-mandatory user input?
Or, finally, is it an actual implementation error and the value should really never have been missing?

<pullquote>The issue with null is that it says nothing about why a value is missing</pullquote>

Bad code maps all of these cases to the same thing: ` null`.
So when a ` NullPointerException` or other undesired behavior that relates to missing values ("Why is this field empty?", "Why does the search not find that thing?") pops up, what is the first step in fixing it?
Finding out why the value is missing and whether that is ok or an implementation error.
In fact, answering that question is usually 90% of the solution!

It can be very hard to do that, though, because null can hide in any reference type and unless rigorous checks are in place (like using ` Objects::requireNonNull` on constructor and method parameters) it readily proliferates throughout a code base.
So before answering why null showed up in the place where it caused trouble, it is necessary to track it to its source, which can take quite some time in a sufficiently complex system.

So the underlying problem with null is not the misbehavior it causes but the conflation of various different concerns into a single, particularly sneaky and error-prone concept.

## Elvis Enters The Building

I've recently played around with Kotlin and was as amazed by the null-handling as I assumed I would be from reading about it.
It is not the only language which does it this way but it's one I actually worked with so I picked it as an example.
But it is just that: an example.
This is no "Kotlin is better than Java" argument, it's an "look how other type systems handle this" elaboration.

(I highly recommend [this thorough introduction to Kotlin's type system](http://natpryce.com/articles/000818.html) if you want to learn more about it.)

Anyway, in such type systems default references are not-nullable and the compiler makes sure that no accidents happen.
A ` String` is always a string and not "either a string or null".

```java
// declare a variable of non-nullable type `User`
val user : User = ...
// call properties (if you don't know the syntax,
// just assume these were public fields)
val userStreet : String = user.address.street
// if neither `address` nor `street` return a nullable type,
// `userStreet` can never be null;
// if they would, the code would not compile because `userStreet`
// is of the non-nullable type `String`
```

Of course things can go missing and every type can be made nullable by appending ` ?` to it.
From this point on, member access (e.g. calling methods) is at the risk of failing due to null references.
The awesome part is that the compiler is aware of the risks and forces you to handle them correctly (or be an ass about it and override the complaints).
What's one way to do that?
The Elvis operator!

Elvis, written as `?.`, distinguishes whether the reference on which the member is called is null or not.
If it is null, the member is not called and the entire expression evaluates to null.
If it is present, the member is called as expected.

```java
// declare a variable of the nullable type `User`
val user : User? = ...
// use Elvis to navigate properties null-safely
val userStreet : String? = user?.address?.street
// if `user` is null, so is `userStreet`;
// `address` and `street` might return nullable types
```

In type systems that understand nullability Elvis is a wonderful mechanism!
With it, you can express that you are aware values might be missing and accept that as an outcome for the call.

At the same time, the compiler will force you to use it on potentially null references, thus preventing accidental exceptions.
Furthermore, it will forcefully propagate that ugly nullability-property to the variables you assign the result to.
This forces you to carry the complexity of possibly null values with you and gives you an incentive to get rid of it sooner rather than later.

## Why Shouldn't This Work In Java?

So if I like Elvis so much in Kotlin, why wouldn't I want to see it in Java?
Because Elvis only works with a type system that distinguishes nullable from non-nullable types!
Otherwise it does exactly the opposite of what it was supposed to and makes nulls much more problematic.

Think about it: You get an NPE from calling a member on null.
What is the easiest thing to do?
Squeeze that question mark in there and be done with it!

<pullquote>Elvis only works with non-nullable types</pullquote>

Is that correct?
Null tells you nothing about whether a value is allowed to be missing, so who knows?
Does it affect the calling or the called code negatively?
Well, the compiler can't tell you whether that code can handle null, so, again, who knows?

Type systems like Kotlin's can answer both of these questions, Java's leaves you guessing.
The right choice is to investigate, which requires effort.
The wrong choice is to just proliferate null.
What do you think will happen if the second choice gets even easier than it is today?
Do you expect to see more or less problems with absent values?
Do you expect the paths from the source of a null reference to where it causes problems to become longer or shorter?

Good languages and good APIs make the correct choice the easy one.
Well-designed types in a good static type system rule out what should not happen at run time.
Elvis in Java would fail on both these accounts.

<pullquote>Elvis makes the wrong choice easier</pullquote>

Instead of demanding an easier way to handle null, we would do better to [eradicate it from our code base](stephen-colebourne-java-optional-strict-approach) or at least [each type's public API](http://blog.joda.org/2015/08/java-se-8-optional-pragmatic-approach.html).

## A Word On Optional

Most of the Twitter discussion actually revolved around ` Optional` but I'm not going to repeat it here because that's a different post ([one I already wrote](intention-revealing-code-java-8-optional) - [twice actually](stephen-colebourne-java-optional-strict-approach)).
Instead I want to highlight a specific argument and put it into the context of Elvis.

It was repeatedly remarked as a weakness of ` Optional` that it was so easy to mishandle and that imprudent use was a likely or even common scenario.
Personally, I didn't have that problem yet but it sounds reasonable.
I would argue that handling ` Optional` can be taught with moderate effort (surely more easily than proper null handling) but unless that happens I can see how misusing it could make a code base suck.

But to those who feel that way, I want to pose the question: What the hell makes you think that this would not be so much worse with Elvis?
As I pointed out above, it makes a terrible choice damnably easy!
Arguably more so than `Optional` ever could.

<pullquote>What the hell makes you think Elvis would not be so much worse?</pullquote>

## Summary

Absent values necessary evil.
Encoding as null bad.
Proliferation terrible.

If Java had a type system that would help handling null and incentivize moving away from it, Elvis would be great.
Alas, it doesn't.
So making it even easier to spread null around the code base instead of creating a proper design for missing values moves the needle in the wrong direction.

To end on a bellicose note: If you've read all this with the thought that you still want Elvis because it would make your life *so much easier*, chances are your APIs are badly designed because they overuse null.
In that case your desire to get your hands on Elvis is precisely the reason why I think Java should not have it.
