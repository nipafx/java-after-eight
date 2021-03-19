---
title: "Definitive Guide To Switch Expressions In Java 13"
tags: [java-13, java-basics, switch]
date: 2019-08-16
slug: java-13-switch-expressions
description: "Java 13 finalized switch expressions. Together with a new lambda-style arrow syntax, this makes switch more expressive and less error-prone."
searchKeywords: "switch expression"
featuredImage: java-12-switch
repo: java-x-demo
---

Good old `switch` has been with Java from day one.
We all use it and we all got used to it - particularly its quirks.
(Anybody else annoyed by `break`?) But things change!
Java 12 introduces **switch expressions** and Java 13 refines them:

```java
boolean result = switch (ternaryBool) {
	case TRUE -> true;
	case FALSE -> false;
	case FILE_NOT_FOUND -> throw new UncheckedIOException(
		"This is ridiculous!",
		new FileNotFoundException());
	// as we'll see in "Exhaustiveness", `default` is not necessary
	default -> throw new IllegalArgumentException("Seriously?! 🤬");
};
```

With switch expressions, the entire switch block "gets a value" that can then be assigned; you can use a lambda-style syntax and enjoy straightforward control flow, free of fall-through.
Beyond the obvious, there are a few details to consider - in this guide I'll cover everything you need to know about switch expressions in Java 13.

While Java 12 introduces and 13 refines [switch expressions](http://openjdk.java.net/jeps/325), they do so as a [preview language feature](enable-preview-language-features).
That means (a) it can still change over the next few releases (as it did between 12 and 13) and (b) it needs to be unlocked, at compile time and run time, with the new command line option `--enable-preview`.
Then keep in mind that this isn't the endgame for `switch` - it's just a step on the way to full [pattern matching](http://openjdk.java.net/jeps/305).

## Trouble With Switch Statements

Before we get into the new stuff, lets quickly assess where we are.
Say we're facing [the dreaded ternary Boolean](https://thedailywtf.com/articles/What_Is_Truth_0x3f_) and want to convert it to a regular `Boolean`.
Here's one way to do that:

```java
boolean result;
switch (ternaryBool) {
	case TRUE:
		result = true;
		// don't forget to `break` or you're screwed!
		break;
	case FALSE:
		result = false;
		break;
	case FILE_NOT_FOUND:
		// intermediate variable for demo purposes;
		// wait for it...
		var ex = new UncheckedIOException(
				"This is ridiculous!",
				new FileNotFoundException());
		throw ex;
	default:
		// ... here we go:
		// can't declare another variable with the same name
		var ex2 = new IllegalArgumentException(
			"Seriously?! 🤬");
		throw ex2;
}
```

This is very painful.
As many other `switch` occurrences out in the wild, this one just wants to compute a value and assign it, but the implementation is roundabout (declare `result` to use it later), repetitive (my `break`s are always copy-pasta), and error-prone (forgot a branch?
Oops!).
There's clearly room for improvement.

<pullquote>Switch statements are often roundabout, repetetive, and error-prone</pullquote>

One way around some of the trouble is to push the entire switch statement into its own method:

```java
private static boolean toBoolean(Bool ternaryBool) {
	switch (ternaryBool) {
		case TRUE: return true;
		case FALSE: return false;
		case FILE_NOT_FOUND:
			throw new UncheckedIOException(
					"This is ridiculous!",
					new FileNotFoundException());
		// without default branch, the method wouldn't compile
		default:
			throw new IllegalArgumentException("Seriously?! 🤬");
	}
}
```

This is much better: No spurious variable, no `break`s cluttering the code and the compiler complains if there's no `default` (even though that seems unnecessary in this instance).

But we shouldn't have to create methods just to work around a cumbersome language feature.
And that's not even mentioning that such a refactoring is not always possible.
No, we need a better solution!

## Enter Switch Expressions!

As I've shown you in the introduction, from Java 12 onward you can solve the problem above as follows:

```java
boolean result = switch(ternaryBool) {
	case TRUE -> true;
	case FALSE -> false;
	case FILE_NOT_FOUND -> throw new UncheckedIOException(
		"This is ridiculous!",
		new FileNotFoundException());
	// as we'll see in "Exhaustiveness", `default` is not necessary
	default -> throw new IllegalArgumentException("Seriously?! 🤬");
};
```

I think this is fairly straightforward to understand: If `ternaryBool` is `TRUE`, `result` ends up being `true` (in other words, `TRUE` maps to `true`).
For `FALSE` it's `false`.
`FILE_NOT_FOUND` as well as possible additional values lead to increasingly incredulous exceptions.

Two things jump out immediately:

-   `switch` can have a result
-   what's with the arrows?

I'll discuss these two central aspects of the new feature before going into further details.

### Expression vs Statement

You may be wondering what it means that `switch` is now an expression.
What was it before that?

Before Java 12, `switch` was a *statement* - an imperative construct that directs control flow.
It shows the way, but - so to speak - can never be the destination.
Because the ultimate goal of any computation is a result, a value.
An *expression*, on the other hand, gets evaluated to exactly that: a value.

Think of it as the difference between Java's `if` and the conditional operator `?
:`.
Both check a Boolean condition and branch execution according to that.
The difference is that `if` merely executes the respective block, whereas `?
:` is evaluated to the respective result:

```java
if (condition)
	result = doThis();
else
	result = doThat();

result = condition ? doThis() : doThat();
```

Same for `switch`: Before Java 12, if you wanted to compute a value, you had to either assign the result to a variable (and then `break`) or `return` from a method dedicated to the switch statement.
Now, the entire switch ~~statement~~ expression is evaluated (by picking the respective switch branch and executing it) and the result can be assigned to a variable.

<pullquote>The entire switch expression is evaluated; it "gets a value"</pullquote>

One consequence of the distinction between expression and statement is that a switch expression, since it's part of a statement, needs to end with a semicolon, where as the classic switch statement doesn't.

### Arrow vs Colon

The introductory example used the new lambda-style syntax with the arrow between label and execution.
It is important to understand that this not required to use `switch` as an expression.
In fact, this is equivalent to the example above:

```java
boolean result = switch (ternaryBool) {
	case TRUE:
		yield true;
	case FALSE:
		yield false;
	case FILE_NOT_FOUND:
		throw new UncheckedIOException(
			"This is ridiculous!",
			new FileNotFoundException());
	default:
		throw new IllegalArgumentException("Seriously?! 🤬");
};
```

Note that you need to use the new contextual keyword `yield` to express which value each branch results in.
(This is new in Java 13.
In Java 12, you'd use `break` for that, i.e.
`break true;` instead of `yield true;`).

So when the arrow does not signify an expression instead of a statement, what is it there for?
Just hipster syntax?
Historically, labels with a colon merely mark an entry point into an execution.
From there it continues, even when it passes another label.
In switch we know this as fall-through: A case label determines where the control flow jumps to, but it needs a `break` or `return` to quit flowing through the switch.

The arrow-form, on the other hand, signifies that only the block to its right will be executed.
That's right, no fall-through!
🎉 I'll give you an example further below after covering a few other details.

<pullquote>The arrow-form prevents fall-through</pullquote>

## Switch Evolution In Depth

In Java 12/13, `switch` evolves considerably.
This happens in different areas: `switch` in general, specifics of the arrow-form, and characteristics of using switch as an expression.
Each of the three areas has its own section - this one covers general properties that hold for statements *and* expressions, for arrow *and* colon-form:

-   multiple case labels
-   switchable types

### Multiple Case Labels

So far, each `case` contained a single label, but that is no longer required.
Instead, a `case` can match against multiple labels:

```java
String result = switch (ternaryBool) {
	case TRUE, FALSE -> "sane";
	// `default, case FILE_NOT_FOUND -> ...` does not work
	// (neither does other way around), but that makes
	// sense because using only `default` suffices
	default -> "insane";
};
```

The behavior should be obvious: Both `TRUE` and `FALSE` lead to the same result, in this case an evaluation of the switch expression to `"sane"`.

This is a pretty neat addition!
It also covers a lot of use cases where we may have used fall-through in the past.

### Types Beyond Enums

All examples in this post switch over an enum.
What about other types?
Switch expressions and statements alike can also switch over a `String`, `int` (checks [the docs](https://docs.oracle.com/javase/tutorial/java/nutsandbolts/switch.html)) `short`, `byte`, `char`, and their wrapper types.
So far nothing changed here, although extending this with `float` and `long` [is still on the table](http://openjdk.java.net/jeps/325) (second to last paragraph).

## Arrow-Form In Depth

Let's have a look at the two properties specific to the arrow-form:

-   no fall-through
-   statement blocks

### No Fall-Through

Here's what [JEP 325](http://openjdk.java.net/jeps/325) has to say about fall-through:

> The current design of Java's `switch` statement follows closely languages such as C and C++, and supports fall-through semantics by default.
Whilst this traditional control flow is often useful for writing low-level code (such as parsers for binary encodings), as switch is used in higher-level contexts, its error-prone nature starts to outweigh its flexibility.

I completely agree and welcome the option to use `switch` without that default behavior:

```java
switch (ternaryBool) {
	case TRUE, FALSE -> System.out.println("Bool was sane");
	// in colon-form, if `ternaryBool` is `TRUE` or `FALSE`,
	// we would see both messages; in arrow-form, only one
	// branch is executed
	default -> System.out.println("Bool was insane");
}
```

It's important to internalize that this has nothing to do with whether you use `switch` as an expression or statement.
Arrow versus colon is the deciding factor here.

### Statement Blocks

Much like with lambdas, a label's arrow can either point to a single statement (like above) or to a curly-braced block:

```java
boolean result = switch (Bool.random()) {
	case TRUE -> {
		System.out.println("Bool true");
		// define result with `yield`
		yield true;
	}
	case FALSE -> {
		System.out.println("Bool false");
		yield false;
	}
	case FILE_NOT_FOUND -> {
		var ex = new UncheckedIOException(
			"This is ridiculous!",
			new FileNotFoundException());
		throw ex;
	}
	default -> {
		var ex = new IllegalArgumentException(
			"Seriously?! 🤬");
		throw ex;
	}
};
```

Forcing blocks for multi-line statements, something that the colon-form does not require, has the added advantage that no special work is required to be able to use the same variable names in different switch branches; see `ex` above.

In case you wonder about the decision to exit those lambda-style blocks with `yield` as opposed to `return`, that is necessary to avoid confusion: `return` could easily be misunderstood to mean "return from the surrounding method".

Java 13 replaced `break $VALUE` with `yield $VALUE`, which I slightly prefer.
While `break` was easy to adopt for developers already familiar with Java, it was pretty odd.
I mean, what is `break true` trying to tell me?

## Switch Expressions In Depth

Last but not least, here are the characteristics of using `switch` as an expression as opposed to a statement:

-   poly expression
-   returning early
-   exhaustiveness

Note that it doesn't matter which form is used!

### Poly Expressions

Switch expressions are *poly expressions*.
That means they don't have a definitive type of their own, but can be one of several types.
The poly expressions you use the most are lambdas: `s -> s + " "` can be a `Function<String, String>`, but it can also be a `Function<Serializable, Object>` or a `UnaryOperator<String>`.

With switch expressions, the type is determined in an interplay between where the `switch` is used and what types its branches produce.
If a switch expression is assigned to an explicitly typed variable, passed as an argument, or otherwise used in a context where the exact type is known (this is called the *target type*), all branches must conform to that type.
That's what we did so far:

```java
String result = switch (ternaryBool) {
	case TRUE, FALSE -> "sane";
	default -> "insane";
};
```

The evaluation of `switch` is assigned to `result`, which is of type `String`.
Hence, `String` is the target type and all branches must produce a result that can be assigned to a `String`.
That's the case, so this works.

The same happens here:

```java
Serializable serializableMessage = switch (bool) {
	case TRUE, FALSE -> "sane";
	// note that we don't throw the exception!
	// but it's `Serializable`, so it matches the target type
	default -> new IllegalArgumentException("insane");
};
```

What about now?

```java
// compiler infers super type of `String` and
// `IllegalArgumentException` ~> `Serializable`
var serializableMessage = switch (bool) {
	case TRUE, FALSE -> "sane";
	// note that we don't throw the exception!
	default -> new IllegalArgumentException("insane");
};
```

If the target type is not known, as is the case here because [we use `var`](java-10-var-type-inference), a type is computed by finding the most specific supertype of the types that the branches produce.

### Returning Early

A consequence of the distinction between switch as expression and statement is that while you can `return` from inside a switch statement ...

```java
public String sanity(Bool ternaryBool) {
	switch (ternaryBool) {
		// `return` is only possible from block
		case TRUE, FALSE -> { return "sane"; }
		default -> { return "This is ridiculous!"; }
	};
}
```

... you can't `return` from within an expression ...

```java
public String sanity(Bool ternaryBool) {
	String result = switch (ternaryBool) {
		// this does not compile - error:
		//     "return outside of enclosing switch expression"
		case TRUE, FALSE -> { return "sane"; }
		default -> { return "This is ridiculous!"; }
	};
}
```

This makes perfect sense and is the case regardless of whether you use arrow or colon-form.

### Exhaustiveness

If you use `switch` as a statement, it doesn't really matter whether all cases are covered.
Sure, you may accidentally miss a case and the code will silently misbehave, but the compiler doesn't care - you, your IDE, and your code analysis tools are left alone with this.

That problem is compounded with switch expressions.
What should `switch` evaluate to if a case is not covered?
The only answer Java can give is `null` for reference types and the default value for primitives.
That would be very error-prone and guaranteed to lead to the occasional wild goose chases through the code base.

To prevent that, the compiler is here to help.
For switch expressions it insists that all possible cases are covered.
The following hence leads to a compile error:

```java
// compile error:
//     "the switch expression does not cover all possible input values"
boolean result = switch (ternaryBool) {
	case TRUE -> true;
	// no case for `FALSE`
	case FILE_NOT_FOUND -> throw new UncheckedIOException(
		"This is ridiculous!",
		new FileNotFoundException());
};
```

The interesting bit is the solution: While adding a `default` branch would of course fix the error, it's not the only way to do that - a case for `FALSE` suffices:

```java
// compiles without `default` branch because
// all cases for `ternaryBool` are covered
boolean result = switch (ternaryBool) {
	case TRUE -> true;
	case FALSE -> false;
	case FILE_NOT_FOUND -> throw new UncheckedIOException(
		"This is ridiculous!",
		new FileNotFoundException());
};
```

Yes, the compiler is finally able to detect whether all enum values are covered (whether the cases *exhaust* all options) and doesn't force a useless `default` if they aren't!
Let's sit a moment in silent gratitude.
🙏

<pullquote>Switch expressions need to be exhaustive, but, for enums, a default branch is not required</pullquote>

That begs one question, though.
What if somebody goes overboard and turns the crazy ternary `Bool` into a quaternion Boolean by adding a fourth value?
If you recompile the switch expression against the extended `Bool`, you get a compile error (the expression is no longer exhaustive).
Without recompilation, this turns into a run-time problem.
To catch that early and loudly, the compiler slips in a `default` branch (if you didn't provide one) that behaves much like the one we used so far by throwing an informative exception.

Currently, exhaustiveness without `default` branch only works for enums, but when `switch` becomes more powerful in future Java versions, it may also work for more arbitrary types.
If case labels can't only check for equality, but also make comparisons (e.g. `_ < 5 -> ...`) it will be possible to exhaust all options for number types, too.
Another situations where exhaustiveness can be checked are so-called [*sealed types*](https://kotlinlang.org/docs/reference/sealed-classes.html), but I won't go into them here.

## Reflection

We've seen that Java 12/13, as a preview feature, turns `switch` into an expression:

-   as a general improvement, in all uses of `switch`, a single case can match multiple labels
-   the new arrow-form `case ... -> ...` follows the lambda-syntax:
	-   it allows single-line statements or curly-braced blocks
	-   it prevents fall-through into the next case
-   regardless of form, `switch` can now be used as an expression:
	-   it is evaluated to a value that can then be assigned or passed on as part of a larger statement
	-   this is a poly expression: if the target type is known, all branches must conform to it; otherwise the most specific type that matches all branches is determined
	-   `yield` returns a value from a block
	-   for a switch expression over an enum, the compiler checks exhaustiveness; if no `default` branch is present it adds one that throws an exception

Where does this leave us?
First, since this is a preview feature, you still have some time to give feedback on [the Amber mailing list](http://mail.openjdk.java.net/mailman/listinfo/amber-dev) - use it if you disagree with anything.

Then, assuming switch remains the way it is at the moment, I think the arrow-form will become the new default.
Without fall-through and with lambda'esque succinctness (very natural to have `case` and single statement on the same line) it is much denser without impairing readability.
I'm sure I will only use the colon-form if I want to opt-in to fall-through.

What do you think?
Happy with the way this turned out?
