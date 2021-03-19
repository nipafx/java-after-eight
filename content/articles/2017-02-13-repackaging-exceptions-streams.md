---
title: "Repackaging Exceptions In Streams"
tags: [clean-code, java-8, lambda, streams]
date: 2017-02-13
slug: java-repackaging-exceptions-streams
description: "How to repackage checked exceptions that get thrown in a Java stream pipeline so that they can be thrown without the compiler complaining about it."
intro: "My first stab at exception handling in Java streams. Explores how to repackage checked exceptions so that they can be thrown without the compiler complaining about it."
searchKeywords: "stream exception"
featuredImage: repackage-stream-exceptions
---

Java 8 is a couple of years old but there are still use cases, not even edge cases, that the community did not yet develop a good arsenal of solutions for.
How to handle checked exceptions in stream pipelines is one such problem.
The functional interfaces various Stream operations accept do not allow implementations to throw checked exceptions but many methods we might want to call do.
Obviously, there's a tension here, which many developers have come across.

My main goal is to propose various solutions and, ideally, to establish a common terminology that makes discussions easier.
I will also comment on my suggestions, adding my own assessment of how useful I find them - this is secondary, though, and I hope that it does not distract from the main goal: getting the ideas out there.

This first post will look into repackaging exceptions so that the compiler stops complaining.

## Setting the Scene

The underlying scenario is something every frequent user of streams has encountered in one form or other: A method you would like to use in one of stream's intermediate operations throws a checked exceptions.

In this post, I will assume that you are trying to parse a stream of strings to a stream of users:

```java
Stream<User> parse(Stream<String> strings) {
	return strings.map(User::parse);
}
```

(If you're not down with having streams as parameters or return values, assume the entire stream pipeline would be within the method's scope.
The following techniques apply either way but some of the assessments would be different if you handled the entire stream on the spot.)

Unfortunately, `User::parse` can throw a `ParseException`:

```java
public class User {

	public static User parse(String userString) throws ParseException {
		// ...
	}

}
```

This leads to the compiler complaining about *"Unhandled exception: java.text.ParseException"* for the method reference `User::parse`.
What to do now?

Before we look into solutions for this problem I want to point something out: I do not regard the Stream API's incompatibility with checked exceptions as something that could've been overcome with a different design.
At some point I may write a longer post explaining that, but the short version is this: If the functional interface methods could throw checked exceptions, there would be no pleasant way to combine that with streams' laziness as it is the terminal operation that will eventually throw that exception.

But we can make good use of a function that can throw exceptions, so let's introduce that interface while we're at it:

```java
@FunctionalInterface
interface CheckedFunction<T, R, EX extends Exception> {

	R apply(T element) throws EX;

}
```

This allows us to assign `User::parse` to a `CheckedFunction<String, User, ParseException>`.
Note that the type of the exception is generic, which will come in handy later.

## Repackaging Exceptions In Streams

So do you really have to handle the exceptions?
Could you not just, I don't know, make the problem go away?
The surprising answer is "Yes, you can." Whether you *should* remains to be seen...

### Wrap In Unchecked Exception

Given a function that throws a checked exception, it is pretty easy to transform it into one that throws an unchecked one instead:

```java
Stream<User> parse(Stream<String> strings) {
	return strings
			.map(uncheckException(User::parse))
}

<T, R> Function<T, R> uncheckException(
		CheckedFunction<T, R, Exception> function) {
	return element -> {
		try {
			return function.apply(element);
		} catch (Exception ex) {
			// many astute readers have observed that this
			// catch is too simple; it should special case
			// RuntimeException (to not repackage),
			// InterruptedException (calling Thread::interrupt),
			// and maybe even catch all Throwables
			throw new RuntimeException(ex);
		}
	};
}
```

This is actually not too bad.
And if you prefer unchecked exceptions anyway, then this is all the more enticing.
If, on the other hand, you value the distinction between checked exceptions (for things you expect can go wrong, like bad input for example) and unchecked exceptions (for implementation errors), then this will sent shivers down your spine.

In any case the final consumer of the stream has to be aware that the exception could be thrown, which at this point needs to be communicated with tests or documentation, both easier to ignore than the compiler.
It feels a little like hiding a bomb in the stream.

Finally, note that this aborts the stream as soon as the first error occurs - something that might or might not be ok.
Deciding whether it is ok can be tough if the method returns a stream instead of consuming it because different callers might have different requirements.

### Sneaky-Throw Exception

Another way to fix this whole thing, is to "sneaky-throw" the exception.
This technique uses generics to confuse the compiler and `@SuppressWarnings` to silence its remaining complaints.

```java
Stream<User> parse(Stream<String> strings) {
	return strings
			.map(hideException(User::parse));
}

<T, R> Function<T, R> hideException(
		CheckedFunction<T, R, Exception> function) {
	return element -> {
		try {
			return function.apply(element);
		} catch (Exception ex) {
			return sneakyThrow(ex);
		}
	};
}

@SuppressWarnings("unchecked")
<E extends Throwable, T> T sneakyThrow(Throwable t) throws E {
	throw (E) t;
}
```

Err, what?
As promised, the method `sneakyThrow` uses generics to trick the compiler into throwing a checked exception without declaring it.
Then `hideException` uses that to catch any exception the `CheckedFunction` might throw and rethrows it sneakily.
(In case you're using Lombok, have a look at [its `@SneakyThrows` annotation](https://www.sitepoint.com/beyond-pojos-ten-ways-reduce-boilerplate-lombok/#sneakythrows).)

I consider this a very risky move.
For one, it still hides a bomb in the stream.
It goes much further, though, and makes that bomb extra hard to defuse properly.
Did you ever try to catch a checked exception that is not declared with a `throws` clause?

```java
try {
	userStrings.stream()
			.map(hideException(User::parse));
			.forEach(System.out::println);
// compile error because ParseException
// is not declared as being thrown
} catch (ParseException ex) {
	// handle exception
}
```

Won't work because the compiler operates under the assumption that none of the methods actually throw a `ParseException`.
Instead you'd have to catch `Exception`, filter out `ParseException` and rethrow everything else.

Wow, that sucks!

Unfortunately this technique shows up in [a StackOverflow answer](http://stackoverflow.com/a/19757456/2525313) that ranks extremely well on Google when looking for *Java stream exception handling*.
In all fairness, the answer contains a disclaimer but I am afraid it might get ignored too often:

> Needless to say, this should be handled with care and everybody on the project must be aware that a checked exception may appear where it is not declared.

But as we have seen there is no good way to declare / catch such an exception, so I would have worded that a little stronger:

> It's a nice experiment but never actually do it!
If you really want to throw, wrap in a runtime exception.

### Lift Exception

The problem with sneaky-throw was that it surprises consumers of the stream *and* makes it hard to handle that exception even once they overcame that surprise.
For the latter, at least, there is a way out.
Consider this function:

```java
<T, R, EX extends Exception> Function<T, R> liftException(
		CheckedFunction<T, R, EX> function) throws EX {
	return hideException(function);
}
```

It does exactly the same as `hideException` *but* it declares that it throws `EX`.
Why would that be helpful?
Because this way you can use it to make the compiler understand that a checked exception might get thrown:

```java
Stream<User> parse(Stream<String> strings) {
	return strings
			// does not compile because `liftException`
			// throws ParseException but it is unhandled
			.map(liftException(User::parse));
}
```

The problem is, and the body of `liftException` makes that abundantly clear, that it does of course *not* throw an exception.
So in an example like this, where we see only part of the pipeline, it arguably makes the situation even more confusing.
Now, callers of `parse` might put it into a try-catch block, expecting to have handled the exception well (if they don't think too hard about it), and then still get surprised when the terminal operation throws that very exception (remember it is hidden with `sneakyThrow`).

If you are someone who never returns streams, though, `liftException` can be pretty useful.
With it, some call in your stream pipeline declares to throw a checked exception so you can put it all into a try-catch block:

```java
try {
	userStrings.stream()
			.map(liftException(User::parse));
			.forEach(System.out::println);
} catch (ParseException ex) {
	// handle exception
}
```

Alternatively, the method containing the pipeline could declare that it throws the exception:

```java
List<User> parse(List<String> userStrings) throws ParseException {
	return userStrings.stream()
			.map(liftException(User::parse));
			.collect(toList());
}
```

But as I said before, I think this only works well if you never return streams.
Because if you do, even only occasionally, there is a risk that you or a colleague takes the pipeline apart during a refactoring, arming the bomb that is an undeclared checked exception, hidden in a stream.

There is another drawback that [Sebastian Millies pointed out](java-repackaging-exceptions-streams)<!-- comment-3154058536 -->, namely that the interfaces and methods used so far only allow a single exception.
As soon as a method declares more than one checked exception, things get problematic.
Either you let Java derive a common supertype (likely to be `Exception`) or you declare additional `CheckedFunction` interfaces and `liftException` methods for more than one exception.
Both not exactly great options.

## Reflection

Given a method that throws a checked exception I have shown you two and a half different ways to use them in a stream if the exception needs to be thrown immediately:

-   [wrap the checked exception](#wrapinuncheckedexception) in a runtime exception
-   [sneaky-throw the checked exception](#sneakythrowexception) so that the compiler does not recognize it being thrown
-   still sneaky-throw but let [the utitility function declare the exception](#liftexception) so that the compiler is at least aware that it gets thrown somewhere

Note that all of these approaches mean that the stream pipeline will stop processing then and there, yielding no results unless those achieved by side effects.
I find often that is not what I want to do, though (because I *do* like returning streams).
The next article tackles this by investigating how to handle exceptions on the spot, without aborting the pipeline.
