---
title: "Instances of Non-Capturing Lambdas"
tags: [default-methods, java-8, lambda]
date: 2014-12-08
slug: java-non-capturing-lambdas
description: "See how Java's creation of instances of non-capturing lambda expressions can lead to unexpected and possibly bug-inducing behavior."
searchKeywords: "Instances non capturing lambda"
featuredImage: instances-non-capturing-lambdas
repo: lambda-instances
---

Roughly a month ago, I summarized [Brian Goetz' peek under the hood of lambda expressions in Java 8](lambdas-java-peek-hood).
Currently I'm researching for a post about default methods and to my mild surprise came back to how Java handles lambda expressions.
The intersection of these two features can have a subtle but surprising effect, which I want to discuss.

To make this more interesting I'll start with an example, which will culminate in my personal *WTF?!* moment.
The full example can be found in a dedicated [GitHub project](https://github.com/nipafx/demo-lambda-instances).
We will then see the explanation for this somewhat unexpected behavior and finally draw some conclusions to prevent bugs.

## Example

Here goes the example... It's not as trivial or abstract as it could be because I wanted it to show the relevance of this scenario.
But it is still an example in the sense that it only alludes to code which might actually do something useful.

### A Functional Interface

Assume we need a specialization of [the interface `Future`](http://docs.oracle.com/javase/8/docs/api/java/util/concurrent/Future.html) for a scenario where the result already exists during construction.

We decide to implement this by creating an interface `ImmediateFuture` which implements all functionality except `get()` with default methods.
This results in a [functional interface](http://docs.oracle.com/javase/specs/jls/se8/html/jls-9.html#jls-9.8).

You can see the source [here](https://github.com/nipafx/demo-lambda-instances/blob/master/src/org/codefx/lab/lambda/instances/ImmediateFuture.java).

### A Factory

Next, we implement a `FutureFactory` .
It might create all kinds of Futures but it definitely creates our new subtype.
It does so like this:

```java
/**
 * Creates a new future with the default result.
 */
public static Future createWithDefaultResult() {
	ImmediateFuture immediateFuture = () -> 0;
	return immediateFuture;
}

/**
 * Creates a new future with the specified result.
 */
public static Future createWithResult(Integer result) {
	ImmediateFuture immediateFuture = () -> result;
	return immediateFuture;
}
```

### Creating The Futures

Finally we use the factory to create some futures and gather them in a set:

```java
public static void main(String[] args) {
	Set<Future<?>> futures = new HashSet<>();

	futures.add(FutureFactory.createWithDefaultResult());
	futures.add(FutureFactory.createWithDefaultResult());
	futures.add(FutureFactory.createWithResult(42));
	futures.add(FutureFactory.createWithResult(63));

	System.out.println(futures.size());
}
```

### WTF?!

Run the program.
The console will say...

4?

Nope.
3.

WTF?!

## Evaluation of Lambda Expressions

So what's going on here?
Well, with some background knowledge about the evaluation of lambda expressions it's actually not *that* surprising.
If you're not too familiar with how Java does this, now is a good time to catch up.
One way to do so is to watch Brian Goetz' talk ["Lambdas in Java: A peek under the hood"](https://www.youtube.com/watch?v=MLksirK9nnE) or read [my summary](lambdas-java-peek-hood) of it.

### Instances of Lambda Expressions

The key point to understanding this behavior is the fact that the JRE makes no promise about how it turns a lambda expression into an instance of the respective interface.
Let's look at what the Java Language Specification has to say about the matter:

> **15.27.4.
Run-time Evaluation of Lambda Expressions**
>
> \[...\]
>
> Either a new instance of a class with the properties below is allocated and initialized, or an existing instance of a class with the properties below is referenced.
>
> \[... properties of the class - nothing surprising here ...\]
>
> These rules are meant to offer flexibility to implementations of the Java programming language, in that:
>
> -   A new object need not be allocated on every evaluation.
> -   Objects produced by different lambda expressions need not belong to different classes (if the bodies are identical, for example).
> -   Every object produced by evaluation need not belong to the same class (captured local variables might be inlined, for example).
> -   If an "existing instance" is available, it need not have been created at a previous lambda evaluation (it might have been allocated during the enclosing class's initialization, for example).
>
> \[...\]
>
> [JLS, Java SE 8 Edition, §15.27.4](http://docs.oracle.com/javase/specs/jls/se8/html/jls-15.html#jls-15.27.4)

Amongst other optimizations, this clearly enables the JRE to return the same instance for repeated evaluations of a lambda expression.

### Instances of Non-Capturing Lambda Expressions

Note that in the example above the expression does not capture any variables.
It can hence never change from evaluation to evaluation.
And since lambdas are not designed to have state, different evaluations can also not "drift apart" during their lifetime.
So in general, there is no good reason to create several instances of non-capturing lambdas as they would all be exactly the same over their whole lifetime.
This enables the optimization to always return the same instance.

(Contrast this with a lambda expression which captures some variables.
A straight forward evaluation of such an expression is to create a class which has the captured variables as fields.
Each single evaluation must then create a new instance which stores the captured variables in its fields.
These instances are obviously not generally equal.)

So that's exactly what happens in the code above.
`() -> 0` is a non-capturing lambda expression so each evaluation returns the same instance.
Hence the same is true for each call to `createWithDefaultResult()`.

Remember, though, that this might only be true for the JRE version currently installed on my machine (Oracle 1.8.0\_25-b18 for Win 64).
Yours can differ and so can the next gal's and so on.

## Lessons Learned

So we saw why this happens.
And while it makes sense, I'd still say that this behavior is not obvious and will hence not be expected by every developer.
This is the breeding ground for bugs so let's try to analyze the situation and learn something from it.

### Subtyping with Default Methods

Arguably the root cause of the unexpected behavior was the decision of how to refine `Future`.
We did this by extending it with another interface and implementing parts of its functionality with default methods.
With just one remaining unimplemented method `ImmediateFuture` became a functional interface which enables lambda expressions.

Alternatively `ImmediateFuture` could have been an abstract class.
This would have prevented the factory from accidentally returning the same instance because it could not have used lambda expressions.

The discussion of abstract classes vs.
default methods is not easily resolved so I'm not trying to do it here.
But I'll soon publish a post about default methods and I plan to come back to this.
Suffice it to say that the case presented here should be considered when making the decision.

### Lambdas in Factories

Because of the unpredictability of a lambda's reference equality, a factory method should carefully consider using them to create instances.
Unless the method's contract clearly allows for different calls to return the same instance, they should be avoided altogether.

I recommend to include capturing lambdas in this ban.
It is not at all clear (to me), under which circumstances the same instance could or will be reused in future JRE versions.
One possible scenario would be that the JIT discovers that a tight loop creates suppliers which always (or at least often) return the same instance.
By the logic used for non-capturing lambdas, reusing the same supplier instance would be a valid optimization.

### Anonymous Classes vs Lambda Expressions

Note the different semantics of an anonymous class and a lambda expression.
The former guarantees the creation of new instances while the latter does not.
To continue the example, the following implementation of `createWithDefaultResult()` would lead to the `futures`- set having a size of four:

```java
public static Future<Integer> createWithDefaultResult() {
	ImmediateFuture<Integer> immediateFuture = new ImmediateFuture<Integer>() {
		@Override
		public Integer get() throws InterruptedException, ExecutionException {
			return 0;
		}
	};
	return immediateFuture;
}
```

This is especially unsettling because many IDEs allow the automatic conversion from anonymous interface implementations to lambda expressions and vice versa.
With the subtle differences between the two this seemingly purely syntactic conversion can introduce subtle behavior changes.
(Something I was not initially aware of.)

In case you end up in a situation where this becomes relevant and chose to use an anonymous class, make sure to visibly document your decision!
Unfortunately there seems to be no way to keep Eclipse from converting it anyway (e.g. if conversion is enabled as a save action), which also removes any comment inside the anonymous class.

The ultimate alternative seems to be a (static) nested class.
No IDE I know would dare to transform it into a lambda expression so it's the safest way.
Still, it needs to be documented to prevent the next Java-8-fanboy (like yours truly) to come along and screw up your careful consideration.

### Functional Interface Identity

Be careful when you rely on the identity of functional interfaces.
Always consider the possibility that wherever you're getting those instances might repeatedly hand you the same one.

But this is of course pretty vague and of little concrete consequence.
First, all other interfaces can be reduces to a functional one.
This is actually the reason why I picked `Future` - I wanted to have an example which does not immediately scream *CRAZY LAMBDA SHIT GOING ON!* Second, this can make you paranoid pretty quickly.

So don't overthink it - just keep it in mind.

### Guaranteed Behavior

Last but not least (and this is always true but deserves being repeated here):

**Do not rely on undocumented behavior!**

The JLS does not guarantee that each lambda evaluation returns a new instance (as the code above demonstrates).
But it neither guarantees the observed behavior, i.e.
that non-capturing lambdas are always represented by the same instance.
Hence don't write code which depends on either.

I have to admit, though, that this is a tough one.
Seriously, who looks at the JLS of some feature before using it?
I surely don't.

## Reflection

We have seen that Java does not make any guarantees about the identity of evaluated lambda expressions.
While this is a valid optimization, it can have surprising effects.
To prevent this from introducing subtle bugs, we derived guidelines:

-   Be careful when partly implementing an interface with default methods.
-   Do not use lambda expressions in factory methods.
-   Use anonymous or, better yet, inner classes when identity matters.
-   Be careful when relying on the identity of functional interfaces.
-   Finally, **do not rely on undocumented behavior!**

