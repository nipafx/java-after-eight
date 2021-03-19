---
title: "Intention Revealing Code With `Optional`"
tags: [clean-code, java-8, optional]
date: 2014-10-07
slug: intention-revealing-code-java-8-optional
description: "Write intention revealing code with Java 8's new type `Optional` and prevent most NPEs. This is not optional!"
searchKeywords: "Optional"
featuredImage: intention-revealing-optional
---

Java 8 introduced a type `Optional<T>`, which can be used to handle potentially missing values.
It does so by wrapping a reference (which might be null) and providing some nice methods to interact with the value in case it's present.

To some, Optional is not more than that (see [here](http://huguesjohnson.com/programming/java/java8optional.html), [here](http://blog.jooq.org/2013/04/11/on-java-8s-introduction-of-optional/) and for [lots of others here](http://www.reddit.com/r/programming/duplicates/21kzy0/tired_of_null_pointer_exceptions_consider_using/)).
In my opinion, they are missing the crucial point:

**Optional provides a way to get rid of null!** (Well, almost.)

And even though [there are some caveats](http://blog.jooq.org/2014/03/28/java-8-friday-optional-will-remain-an-option-in-java/), I say the benefits are worth dealing with them.
So this post does not discuss in which situations there might be better solutions than using `Optional` - it just claims that `Optional` beats `null`.

## Introduction to Optional

`Optional<T>` is a wrapper class with a very simple functionality.
As the [Javadoc](http://docs.oracle.com/javase/8/docs/api/java/util/Optional.html) puts it:

> A container object which may or may not contain a non-null value.
> If a value is present, `isPresent()` will return true and `get()` will return the value.
>
> Additional methods that depend on the presence or absence of a contained value are provided, such as `orElse()` (return a default value if value not present) and `ifPresent()` (execute a block of code if the value is present).

### Construction

There are three ways to create an instance of Optional:

```java
// an empty 'Optional';
// before Java 8 you would simply use a null reference here
Optional<String> empty = Optional.empty();

// an 'Optional' where you know that it will not contain null;
// (if the parameter for 'of' is null, a 'NullPointerException' is thrown)
Optional<String> full = Optional.of("Some String");

// an 'Optional' where you don't know whether it will contain null or not
Optional<String> halfFull = Optional.ofNullable(someOtherString);
```

To have both `of` and `ofNullable` seems redundnat.
Why would you ever call the first if all you get is the chance of an NPE?
Well, read on to find out.

### Methods

The most basic methods of `Optional<T>` are these two:

-   `boolean isPresent()` returns true if - you guessed it - the value is present, i.e.
if it was not constructed as empty or of a null reference
-   `T get()` returns the value if it is present; otherwise a NoSuchElementException is thrown

This means that you should check with `isPresent()` whether there even is a value before calling `get()` to retrieve it.

Optional provides plenty of other methods, which can be used to do really nice things.
Check out the Javadoc or search the web for [posts like this one](http://www.nurkiewicz.com/2013/08/optional-in-java-8-cheat-sheet.html) to find out about them.
If you really, really like them and want to use them even more, you could check out [EasyBind](https://github.com/TomasMikula/EasyBind), [which makes similar functions available to JavaFX' ObservableValues](http://tomasmikula.github.io/blog/2014/03/26/monadic-operations-on-observablevalue.html).

## Why Even Use Optional?

But none of this is very special.
And nothing is wrong with checking the "old way" whether a reference is null.
The magic of Optional lies neither in its static factory methods, its mundane accessor methods nor in its other, lambda-enabling methods.

**The beauty of Optional is its name.**

Plain and simple.
Before I elaborate on this let's spend some time discussing null.

<contentimage slug="simply-explained-npe" options="narrow"></contentimage>

### Null

Let's start from the end and say you have a NullPointerException.
And not the easy kind which you fix in a second.
No, it's the evil kind which makes you step through the code line by line, trying to answer these questions:

-   Where does the null reference come from?
-   Should it actually reference an instance?
-   Or is null a legal state for that variable?
-   Is it maybe the return value of some [method](http://docs.oracle.com/javase/8/docs/api/java/util/Map.html#get-java.lang.Object-) which behaves in a crazy way?

The reason why this is so complicated and might lead you on an hour long trip down the rabbit hole is that null can mean different things.
It can say "this variable was not initialized" (so it was unintended) as well as "there is nothing here" (making it an intentional value) .
But it's all the same to the code!
The distinction only exists in our head.

So while the author of the lines which produced the null reference might be able to make that distinction, the poor sap who gets the exception (in this story, that's you) will likely not be.
And how could he?
He might be thousands of lines of executed code away from where the reference was first created.
And he has to track it down to find out about the author's intent.

Only when the intent is known, can you decide how to deal with the exception.
Should you have checked for null because it is valid and signifies a missing value or something special?
Or should it not even exist in the first place and you can file a bug with the subsystem which created it?

If only there was a way to make that distinction obvious...

### Reveal Your Intention

Using Optional allows you to express exactly that distinction.
If your intent is to express that an attribute might only be present some of the time, to allow an optional parameter for your method (and overloading is no option for whatever reason) or to tell the caller that there might be no return value, Optional lets you express it.

And if your attributes, arguments or return values are non-optional, you guarantee to the user of your API and reader of your code that they will also be non-null.

Additionally, this intention can be automatically checked with tools like [FindBugs](http://findbugs.sourceforge.net/):

-   [don't return null for `Optional`](http://sourceforge.net/p/findbugs/feature-requests/297/)
-   [don't `get()` without checking `isPresent()`](http://sourceforge.net/p/findbugs/feature-requests/302/)

Do you have any other ideas what could be checked?
Open a ticket on SourceForge or post a comment below!

### Fail Fast

Another good reason to use Optional is that it helps with [failing fast](http://en.wikipedia.org/wiki/Fail-fast).
With it you can express assumptions about your code and have them verified every time it executes.

Here, the two static factory methods `of` and `ofNullable` come in.
When you have a reference and are positive, that it can't be null, use `of` .
If you were wrong and it actually is null, you'll get an exception then and there.
Hence, you should only use `ofNullable` if you can't reason whether the reference is null or not.

Being strict at this point helps a lot in catching potential bugs early.

## The Effects

Imagine a code base, where null is only ever allowed to appear as the parameter or return value of private methods or in local variables.
Everything else, i.e.
attributes and all references going in or coming out of protected, package or public methods must never be null.

What would that be like?

### No More Guessing About The Meaning Of Null

For me, this is the killer argument for Optional.

In case you still end up with a NullPointerException (and don't let me fool you: you will!) it is obvious what to do.
You still have to hunt it down like before but now you already know that its mere existence is a bug.
So you can add more null checks every step of the way until you find the source and fix it.

No further guessing involved!

In turn you might get some NoSuchElementExceptions.
(Remember `Optional.get()` throws them if no value is present.) But those are easy to fix.
Simply check first, whether a value is present.

### No More Thinking About Null

Another important point: You can stop wasting any brain power on whether there could even be a legal null reference.
You do not have to read the docs, look at the code or reason about whether this would even make sense.
If it's not optional, it must be there.

### More Tests Against Null

A corollary of that is that you can more readily sprinkle tests against null.
Whether you use [assertions](http://docs.oracle.com/javase/8/docs/technotes/guides/language/assert.html) or [`Objects.requireNonNull`](http://docs.oracle.com/javase/8/docs/api/java/util/Objects.html#requireNonNull-T-java.lang.String-), you never have to ask yourself, whether this or that argument might actually be allowed to be null.
Because it's not!

## Reflection

This post argues strongly in favor of using Java 8's new type `Optional<T>` .
It stresses that its main feature is its name as it makes obvious which was before often hidden behind other types and expressed with null references: There might not actually be a value.
Finally it paints a picture of how a code base without null would be.

Got any comments on Optional?
Maybe you want to violently disagree?
Then join the discussion below or pingback with an answer post.
I also distilled this into an [answer on the StackOverflow question](http://stackoverflow.com/a/27071576/2525313) mentioned above, so you can comment there as well.
