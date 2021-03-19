---
title: "Getting Rid Of Anonymous Classes"
tags: [java-8, lambda, techniques]
date: 2015-04-17
slug: java-getting-rid-of-anonymous-classes
description: "Anonymous classes are verbose and obfuscating. Functional implementations can oust them from their last strongholds (mainly abstract classes)."
intro: "Anonymous classes are verbose and obfuscating. Functional implementations can oust them from their last strongholds (\"almost-functional\" interfaces and abstract classes)."
searchKeywords: "anonymous classes"
featuredImage: anonymous-classes
---

I really enjoy writing and reading lambda expressions - they're succinct, expressive and fashionable (come on, like that doesn't matter a little!).
Compare that to anonymous classes which are neither of those things.
Which is why I like to get rid of them!

This realization slowly materialized itself over the last months and yesterday my subconscious barfed up an idea of how to achieve that.
I'll present it here and follow up with a post in a couple of weeks after trying it out.

To make sure everybody knows what we're talking about I'll start with a quick recap on anonymous classes.
I will then explain why I'd like to get rid of them before identifying their last stronghold and how to conquer it.

## Quick Recap Of Anonymous Classes

Anonymous classes are used to create an ad-hoc implementation of an interface or an abstract class, like so:

```java
Runnable run = new Runnable() {
	@Override
	public void run() {
		runThisThing(someArgument);
	}
};
```

This does indeed create a separate class (you will find it's .class file next to the one which contains this code) but since it has no name, you guessed it, it's called an anonymous class.

My opinion on the matter was always that these classes should be really short.
One, maybe two methods with a couple of lines.
Everything longer and definitely everything with state seems to deserve a name and a place of its own - either at the bottom of the file as a nested class or even as one of its own.
It always confuses me to read methods which at some point create a 10+ line implementation of who-knows-what which does something totally unrelated.

But for short implementations (as in the example above) anonymous classes were the best choice.

## So What's Wrong With Them?

Nothing's really *wrong* with them.
It's just that after about a year of using lambda expressions and method/constructor references they seem so incredibly clunky.
The more I'm used to one-liners which express their behavior succinctly and precisely, the more I feel repulsed when being confronted with the ceremony and obfuscation of anonymous classes.

Just compare this to the example above:

```java
Runnable run = () -> runThisThing(someArgument);
```

Over the last months I slowly realized that I just don't wanna see them anymore and yesterday a nice little idea of how to get rid of the (up to now) necessary remaining occurrences popped into my head.

## Getting Rid Of Anonymous Classes

As described above, I think everything more complicated than a simple implementation of one or two methods should generally get its own name and place as a nested or stand-alone class.

(By the way, I tend to do the same with classes that override an existing superclass method to change its behavior.
This might be short but spotting the difference and deducing the intend is generally hard if you don't know the now overridden original code.
Giving the class a nice name solves this in most cases.)

Then of course Java 8 came around and thanks to lambda expressions, a huge number of use cases for anonymous classes just disappeared.
This is great!
And it is also the tool to get rid of their last stronghold: implementations of "almost-functional" interfaces and of abstract classes with one or two abstract methods.

So here's my idea:

> When coming across an interface or an abstract class which lends itself to be implemented ad-hoc, we create a **functional implementation**.
> This is a non-abstract class that delegates all method calls to functional interfaces which were specified during construction.

### Example

I guess an example will clarify this:

```java
public interface ValueListener<T> {

	void invalidated(T formerValue);

	void changed(T formerValue, T newValue);

}
```

Since this is no functional interface, you can not use lambda expressions to create an implementation.
Instead you might create an anonymous class whenever you need one:

```java
ValueListener anonymousListener = new ValueListener() {

	@Override
	public void invalidated(String formerValue) {
		valueInvalidated(formerValue);
	}

	@Override
	public void changed(String formerValue, String newValue) {
		valueChanged(formerValue, newValue);
	}
};
```

Instead we can once create a functional implementation of the interface:

```java
public class FunctionalValueListener implements ValueListener {

	private final Consumer invalidated;
	private final BiConsumer changed;

	public FunctionalValueListener(
			Consumer invalidated,
			BiConsumer changed) {
		this.invalidated = invalidated;
		this.changed = changed;
	}

	@Override
	public void invalidated(T formerValue) {
		invalidated.accept(formerValue);
	}

	@Override
	public void changed(T formerValue, T newValue) {
		changed.accept(formerValue, newValue);
	}

}
```

Instances of this class can be created much more succinctly and less obfuscated:

```java
ValueListener functionalListener = new FunctionalValueListener<>(
		this::valueInvalidated,
		this::valueChanged);
```

### Another Example

What actually triggered this idea were the many anonymous implementations of Swing's `AbstractAction` I see in our code base:

```java
Action action = new AbstractAction() {
	@Override
	public void actionPerformed(ActionEvent e) {
		performedAction(e);
	}
};
```

This screams "LAMBDA EXPRESSION!" but you can't use it on abstract classes.
But after creating a functional implementation which only requires a `Consumer<ActionEvent>` you can and it looks like this:

```java
Action action = new FunctionalAction(this::performedAction);
```

Much better, right?

## Follow Up

I will try this out for some weeks and report back how it worked.
I already see some problems (arity of functional interfaces provided by the JDK and exceptions) and at least one way to improve this pattern.

But I think it is worth discussing this approach.
If you think so, too, why don't you share it?

Will you try it as well?
Thought of more problems or an improvement?
Maybe you just think it's stupid?
In any case, leave a comment, write a post, or ping me wherever you find me.

## Reflection

I presented my dislike of the verbosity and obfuscation of anonymous classes.
Long ones should never exist in the first place (make them nested classes or classes in their own right) but short ones were sometimes the best choice.

With functional implementations of short interfaces or abstract classes we can instead use lambda expressions, method references or constructor references and profit from their succinctness and readability.
